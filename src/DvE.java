import MCNP_API.MCNP_API_Utilities;
import PlottingAPI.Figure;
import PlottingAPI.LineProperties;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
        import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
        import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.awt.*;

public class DvE {

    private static final double[][] cScaling = {
            {0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.30, 1.40, 1.50, 1.60, 1.70, 1.80, 1.90, 2.00},
            {0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.35, 1.55, 1.81, 2.12, 2.50, 3.05, 3.97, 6.00}};

    private static final double minEnergy = 0.8;
    private static final double maxEnergy = 7.0;

    private static final PolynomialSplineFunction cParameterScaling =
            new SplineInterpolator().interpolate(cScaling[0], cScaling[1]);

    public static void main(String ... args){

        Figure figure = new Figure("DvE Verification", "Energy (MeV)", "Diameter (um)");

        double[] testCs = MCNP_API_Utilities.linspace(0.4, 1.99, 20);
        for (double c : testCs) {

            PolynomialSplineFunction DvE = getDveFunction(c);
            double[] E = DvE.getKnots();
            double[] D = new double[E.length];
            for (int i = 0; i < E.length; i++){
                D[i] = DvE.value(E[i]);
            }

            System.out.println(c);
            figure.plot(D, E, new LineProperties(LineProperties.Style.Solid, 2.0, Color.RED));

        }
    }

    // Default DvE (c == 1)
    public static double getDiameter(double E){

        final double[] a = {1.2, 11.3, 4.8};
        final double[] B = {0.3, 3.0, 8.0};

        double D = 0.0;
        for (int i = 0; i < a.length; i++){
            D += a[i] * Math.exp( -(E-1)/B[i] );
        }

        return D;

    }

    public static double getDiameter(double E, double c){

        // Calculate the (c == 1)  diameter
        double D = getDiameter(E);

        // If c == 1.0, we don't need to do anything
        if (c == 1.0)   return D;

        if (c < 1) {
            double v = 20.0 * Math.exp(-c * Math.abs(Math.log(D / 20.0)));
            if (D <= 20.0)  return v;
            else            return 40.0 - v;
        }

        else {
            if (D <= 10.0) {
                double A = Math.pow(20.0 - D, 2);
                double B = 20.0 - 2*D;
                return (A/B) * (Math.exp( 0.5 * c * Math.log(D*D/A)) - 1) + 20.0;
            }

            else return 20 - c*(20 - D);
        }
    }

    public static double[] getDiameters(double[] E){
        double[] D = new double[E.length];
        for (int i = 0; i < D.length; i++){
            D[i] = getDiameter(E[i]);
        }
        return D;
    }

    public static double[] getDiameters(double[] E, double c){
        double[] D = new double[E.length];
        for (int i = 0; i < D.length; i++){
            D[i] = getDiameter(E[i], c);
        }
        return D;
    }

    public static double scaleDiameter(double D, double maxD){

        double f;
        if (maxD > 20.0)        f = 1.0;
        else if (maxD > 12.5)   f = (maxD - 12.5) / (20.0 - 12.5);
        else                    f = 0.0;

        double A = (1.0 - maxD/23.0) * (7.0/10.0);
        double B = 0.25;

        double M = (A*(1-f) + B*f)*(20 - maxD) / (20 * maxD);

        return (D / maxD) * (20.0 / (1.0 - M*D));

    }

    public static double[] scaleDiameters(double[] D, double maxD){
        double[] scaledD = new double[D.length];
        for (int i = 0; i < D.length; i++){
            scaledD[i] = scaleDiameter(D[i], maxD);
        }
        return scaledD;
    }

    public static PolynomialSplineFunction getDveFunction(double c){
        final double[] E = MCNP_API_Utilities.linspace(20.0, 0.8, 1000);
        final double[] D = getDiameters(E, c);
        return new SplineInterpolator().interpolate(D, E);
    }

    public static double[] getEnergies(double[] D, double c, double maxD) {

        // Initialize
        double[] E = new double[D.length];

        // Scale the diameters
        D = scaleDiameters(D, maxD);

        // Get the DvE function
        PolynomialSplineFunction DvE = getDveFunction(c);

        // Convert!
        for (int i = 0; i < D.length; i++){
            try                 { E[i] = DvE.value(D[i]);   }
            catch(Exception e)  { E[i] = Double.NaN;        }
        }

        return E;
    }














}
