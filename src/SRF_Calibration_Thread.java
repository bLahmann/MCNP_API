import MCNP_API.MCNP_API_Utilities;
import PlottingAPI.Figure;
import PlottingAPI.LineProperties;
import cStopPow.StopPow;
import cStopPow.StopPow_SRIM;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.OutOfRangeException;

import java.awt.*;

public class SRF_Calibration_Thread extends Thread {

    private double[][][] data;
    private double[] thicknesses;
    private double realC;
    private StopPow_SRIM stopPow;

    private double result;

    public SRF_Calibration_Thread(double[][][] data, double[] thicknesses, double realC, StopPow_SRIM stopPow) {
        this.data = data;
        this.thicknesses = thicknesses;
        this.realC = realC;
        this.stopPow = stopPow;
    }


    public double getResult() {
        return result;
    }

    @Override
    public void run() {

        final double[] cTest            = MCNP_API_Utilities.linspace(realC - 0.3, realC + 0.3, 0.005);
        final double[] finalEnergyNodes = MCNP_API_Utilities.linspace(1.0, 16.0, 0.01);

        final double minDiameter = 5.0;
        final double maxDiameter = 17.0;

        StopPow_SRIM stopPow = null;
        try {
            stopPow = new StopPow_SRIM("lib/Hydrogen in Tantalum.txt");
            stopPow.set_mode(StopPow.getMODE_LENGTH());
        }catch (Exception e){
            e.printStackTrace();
        }


        assert (data.length == thicknesses.length+1);


        // *************************************************
        // Grab a select amount of data from the data array
        // *************************************************

        // Convert the energy bin edges to diameters using the REAL c parameter
        double[] energyEdges      = data[0][0];
        double[] allDiameterEdges = DvE.getDiameters(energyEdges, realC);

        // We're only interested in data between certain diameter bounds
        int[] diameterIndexes = findBoundIndexes(allDiameterEdges, minDiameter, maxDiameter);

        double[] diameterEdges      = new double[diameterIndexes[1] - diameterIndexes[0] + 1];
        double[][] values           = new double[thicknesses.length][diameterEdges.length - 1];
        double[][] uncertainties    = new double[thicknesses.length][diameterEdges.length - 1];
        for (int i = 0; i < diameterEdges.length; i++) {

            diameterEdges[i] = allDiameterEdges[diameterIndexes[0] + i];
            if (i == 0)    continue;

            for (int j = 0; j < thicknesses.length; j++) {
                values[j][i-1]          = data[j][1][diameterIndexes[0] + i];
                uncertainties[j][i-1]   = data[j][2][diameterIndexes[0] + i];
            }
        }

        double[] sourceEnergyEdges  = new double[data[0][0].length];
        double[] sourceEnergyNodes  = new double[data[0][0].length-1];
        double[] sourceValues       = new double[data[0][0].length-1];
        double[] sourceUncertainty  = new double[data[0][0].length-1];
        sourceEnergyEdges = data[data.length-1][0];
        for (int j = 1; j < sourceEnergyEdges.length; j++){
            sourceEnergyNodes[j-1]    = 0.5 * ( sourceEnergyEdges[j] + sourceEnergyEdges[j-1] );
            sourceValues[j-1]         = data[data.length-1][1][j-1] / Math.abs( sourceEnergyEdges[j] - sourceEnergyEdges[j-1] ) / 0.000449395;
            sourceUncertainty[j-1]    = data[data.length-1][2][j-1] / Math.abs( sourceEnergyEdges[j] - sourceEnergyEdges[j-1] );
        }

        /*
        Figure figure = new Figure();
        for (int i = 0; i < thicknesses.length; i++) {
            figure.stairs(diameterEdges, values[i], LineProperties.blackLine(2));
        }
        */



        // ****************
        // Do the analysis
        // ****************

        // For each c parameter
        double[] goodness = new double[cTest.length];
        int minIndex = -1;
        double minGoodness = Double.MAX_VALUE;

        for (int k = 0; k < cTest.length; k++) {

            goodness[k] = getGoodness(getFilterSpectra(
                    thicknesses, diameterEdges, values,
                    cTest[k], finalEnergyNodes, stopPow
            ));

            if (goodness[k] < minGoodness){
                minIndex = k;
                minGoodness = goodness[k];
            }

        }

        /*
        Figure cParamFitFigure = new Figure("Fit", "C Parameter", "Goodness");
        cParamFitFigure.plot(cTest, goodness);
        cParamFitFigure.pack();

         */


        double[][] filterSpectra = getFilterSpectra(
                thicknesses, diameterEdges, values,
                cTest[minIndex], finalEnergyNodes, stopPow
        );

        double[] finalSpectra = new double[finalEnergyNodes.length];
        for (int j = 0; j < finalSpectra.length; j++){
            int numFilters = 0;
            for (int i = 0; i < thicknesses.length; i++){
                if (!Double.isNaN(filterSpectra[i][j])) {
                    finalSpectra[j] += filterSpectra[i][j];
                    numFilters++;
                }
            }

            if (numFilters > 0)     finalSpectra[j] /= numFilters;
            else                    finalSpectra[j] = Double.NaN;
        }

        /*
        Figure spectraFigure = new Figure("Source Comparison", "Energy (MeV)", "Counts per Bin per Yield");
        spectraFigure.plot(sourceEnergyNodes, sourceValues, new LineProperties(LineProperties.Style.Solid, 2, Color.black));
        spectraFigure.plot(finalEnergyNodes, finalSpectra, new LineProperties(LineProperties.Style.Dashed, 2, Color.red));
        spectraFigure.setXLimits(1, 5);
        */


        // **************
        // Post analysis
        // **************

        // Identify the energy bounds we resolved
        double minEnergy = Double.MAX_VALUE, maxEnergy = Double.MIN_VALUE;
        for (int i = 0; i < finalSpectra.length; i++) {
            if (!Double.isNaN(finalSpectra[i])){
                if (finalEnergyNodes[i] < minEnergy)    minEnergy = finalEnergyNodes[i];
                if (finalEnergyNodes[i] > maxEnergy)    maxEnergy = finalEnergyNodes[i];
            }
        }

        int[] sourceBounds = findBoundIndexes(sourceEnergyNodes, minEnergy, maxEnergy);
        double[] sourceMoments = new double[3];
        for (int i = sourceBounds[0]; i < sourceBounds[1]; i++){
            sourceMoments[0] += 0.5 * (Math.pow(sourceEnergyNodes[i], 0) * sourceValues[i] +
                    Math.pow(sourceEnergyNodes[i+1], 0) * sourceValues[i+1]) *
                    (sourceEnergyNodes[i+1] - sourceEnergyNodes[i]);

            sourceMoments[1] += 0.5 * (Math.pow(sourceEnergyNodes[i], 1) * sourceValues[i] +
                    Math.pow(sourceEnergyNodes[i+1], 1) * sourceValues[i+1]) *
                    (sourceEnergyNodes[i+1] - sourceEnergyNodes[i]);

            sourceMoments[2] += 0.5 * (Math.pow(sourceEnergyNodes[i], 2) * sourceValues[i] +
                    Math.pow(sourceEnergyNodes[i+1], 2) * sourceValues[i+1]) *
                    (sourceEnergyNodes[i+1] - sourceEnergyNodes[i]);
        }
        sourceMoments[1] /= sourceMoments[0];
        sourceMoments[2] /= sourceMoments[0];
        sourceMoments[2] -= Math.pow(sourceMoments[1], 2);


        int[] dataBounds = findBoundIndexes(finalEnergyNodes, minEnergy, maxEnergy);
        double[] dataMoments = new double[3];
        for (int i = dataBounds[0]; i < dataBounds[1]; i++){
            dataMoments[0] += 0.5 * (Math.pow(finalEnergyNodes[i], 0) * finalSpectra[i] +
                    Math.pow(finalEnergyNodes[i+1], 0) * finalSpectra[i+1]) *
                    (finalEnergyNodes[i+1] - finalEnergyNodes[i]);

            dataMoments[1] += 0.5 * (Math.pow(finalEnergyNodes[i], 1) * finalSpectra[i] +
                    Math.pow(finalEnergyNodes[i+1], 1) * finalSpectra[i+1]) *
                    (finalEnergyNodes[i+1] - finalEnergyNodes[i]);

            dataMoments[2] += 0.5 * (Math.pow(finalEnergyNodes[i], 2) * finalSpectra[i] +
                    Math.pow(finalEnergyNodes[i+1], 2) * finalSpectra[i+1]) *
                    (finalEnergyNodes[i+1] - finalEnergyNodes[i]);
        }
        dataMoments[1] /= dataMoments[0];
        dataMoments[2] /= dataMoments[0];
        dataMoments[2] -= Math.pow(dataMoments[1], 2);

        /*
        System.out.printf("%.4e, %.4e, %.4e, %.4e, %.4e, %.4e, %.4e, %.4e\n",
                realC, cTest[minIndex],
                sourceMoments[0], dataMoments[0],
                sourceMoments[1], dataMoments[1],
                sourceMoments[2], dataMoments[2]);

         */

        for (int i = 1; i < 2; i++) {
            this.result += Math.pow( (sourceMoments[i] - dataMoments[i])/sourceMoments[i] , 2);
        }

        this.result = Math.sqrt(this.result);

    }

    private double getGoodness(double[][] filterSpectra) {

        // Loop through each node
        double goodness = 0.0;
        for (int i = 0; i < filterSpectra[0].length; i++) {

            // Loop through each spectra
            double mu0 = 0, mu1 = 1, mu2 = 2;
            for (int j = 0; j < filterSpectra.length; j++) {

                double w = 1.0;
                double x = filterSpectra[j][i];

                if (filterSpectra[j][i] != 0 && !Double.isNaN(filterSpectra[j][i])) {
                    mu0 += w;
                    mu1 += w * x;
                    mu2 += w * x * x;
                }
            }

            if (mu0 * mu1 * mu2 != 0) {
                mu1 /= mu0;
                mu2 /= mu0;

                double value = Math.sqrt(mu2 - mu1*mu1) / mu0;
                if (!Double.isNaN(value)){
                    goodness += value;
                }
            }
        }

        return goodness;
    }

    private int[] findBoundIndexes(double[] values, double minBound, double maxBound) {

        int minIndex = 0, maxIndex = 0;
        double minValue = Double.MAX_VALUE, maxValue = Double.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < minBound || values[i] > maxBound){
                continue;
            }
            if (values[i] < minValue) {
                minValue = values[i];
                minIndex = i;
            }
            if (values[i] > maxValue) {
                maxValue = values[i];
                maxIndex = i;
            }
        }

        if (minIndex > maxIndex)    return new int[] {maxIndex, minIndex};
        return new int[] {minIndex, maxIndex};

    }

    private double[][] getFilterSpectra(double[]thicknesses, double[] diameterEdges, double[][] counts,
                                       double c, double[] energyNodes, StopPow_SRIM stopPow) {

        //Figure eInFigure = new Figure("c = " + c, "Yield per MeV", "Energy (MeV)");
        //Figure eOutFigure = new Figure("c = " + c, "Yield per MeV", "Energy (MeV)");


        // Get out Eout bins associated with this c
        double[] inferredEoutEdges = DvE.getEnergies(diameterEdges, c, 20.0);
        double[][] filterSpectra = new double[thicknesses.length][energyNodes.length];

        // For each filter, generate an independent inferred Ein spectra
        for (int i = 0; i < thicknesses.length; i++) {

            double[] tempEoutNodes = new double[inferredEoutEdges.length-1];        // TODO Not needed in final analysis
            double[] deltaEs       = new double[inferredEoutEdges.length-1];        // TODO not needed

            double[] tempSpectrum   = new double[inferredEoutEdges.length-1];
            double[] tempEinNodes   = new double[inferredEoutEdges.length-1];
            double EinLeftEdge = stopPow.Ein(inferredEoutEdges[0], thicknesses[i]);

            // Calculate the Ein nodes and spectra in yield per MeV
            for (int j = 1; j < inferredEoutEdges.length; j++) {
                double EinRightNode = stopPow.Ein(inferredEoutEdges[j], thicknesses[i]);
                tempEoutNodes[j-1] = 0.5 * (inferredEoutEdges[j] + inferredEoutEdges[j-1]);
                deltaEs[j-1] = Math.abs(EinRightNode - EinLeftEdge);

                tempEinNodes[j-1]   = 0.5 * (EinRightNode + EinLeftEdge);
                tempSpectrum[j-1] = counts[i][j-1] / Math.abs(EinRightNode - EinLeftEdge);
                EinLeftEdge = EinRightNode;
            }

            // Interpolate these spectra onto the same energy axis for proper comparison
            PolynomialSplineFunction tempFunction = new LinearInterpolator().interpolate(tempEinNodes, tempSpectrum);
            for (int j = 0; j < energyNodes.length; j++) {
                try {
                    filterSpectra[i][j] = tempFunction.value(energyNodes[j]);
                    filterSpectra[i][j] *= 4*Math.PI*50*50;       // TODO hard coded
                }
                catch (OutOfRangeException e){
                    filterSpectra[i][j] = Double.NaN;
                }
            }

            Color color = null;
            if (i%2==0) color = Color.RED;
            else        color = Color.BLACK;

            //eOutFigure.plot(tempEoutNodes, deltaEs, new LineProperties(LineProperties.Style.Solid, 2, color));
            //eInFigure.plot(energyNodes, filterSpectra[i], new LineProperties(LineProperties.Style.Solid, 2, color));
        }

        //eOutFigure.pack();
        //eInFigure.pack();
        return filterSpectra;
    }
}
