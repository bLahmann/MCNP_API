package apiDecks;

import MCNP_API.*;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;


public class StepRangeFilter extends MCNP_Deck{

    // **************************
    // DIMENSIONS AND PARAMETERS
    // **************************


    public Double fieldingDistance  = 50.0;                                                 // Distance of the front of the tube to TCC (cm)

    public Double detectorDiameter  = 5.0;                                                  // Diameter of the detector (cm)
    public Double detectorThickness = 10.0;                                                 // Thickness of the detector (um)
    public Double filterSideLength  = 3.0;                                                  // Side length of the filtered region (cm)
    public Double tallyStandoff  = 0.05;                                                    // Standoff distance from the edges of the filters for the analysis

    public MCNP_Material filterMaterial = MCNP_Material.tantalum("70c");                    // Filter material
    public double[] filterThicknesses = new double[] {                                      // Thicknesses (um) of the overlay filters
            10.0,                                                                           // 1x1 Filter
            25.0,                                                                           // (2/3) x 1 Filter
            25.0,                                                                           // (1/3) x 1 Filter
            75.0,                                                                           // 1 x (2/3) Filter
            75.0                                                                            // 1 x (1/3) Filter
    };

    public MCNP_Material shieldMaterial = MCNP_Material.aluminum("70c");                    // Shield material
    public Double shieldThickness = 0.3;                                                    // Thickness (cm) of the shield

    public double[] energyBins = MCNP_API_Utilities.linspace(0, 16, 0.025);
    public int numParticles = (int) 1e7;                                                    // Expected 3He3He yield

    public double sourceMeanEnergy = 3.0;                                                   // Mean energy of the source (MeV)
    public double sourceSigma      = 0.00001;                                               // Sigma of the source (keV)

    // Computation Parameters
    private final static String[] hosts = {             // Computer hosts to use
            "ben-local",
            "chewie-local",
            "luke-local",
            "han-local"
    };
    private final static Integer[] numNodes = {         // Number of nodes to use (max 192)
            46,
            46,
            46,
            46
    };


    public static void main(String ... args) throws Exception{
        double[] energies = MCNP_API_Utilities.linspace(4.5, 10.5, 1.0);
        for (double energy : energies) {

            System.out.print(String.format("Running %.2f case ...", energy));

            StepRangeFilter srf = new StepRangeFilter("Straggle Calculation");
            srf.sourceMeanEnergy = energy;
            srf.buildDeck();

            MCNP_Job job = new MCNP_Job("SRF_3He3He_Straggling_Calculations", srf);
            job.runMPIJob(hosts, numNodes, false);
            parseOutput(job.outputFile);

            System.out.println("Done!");
        }

    }

    public StepRangeFilter(String name){
        super(name);
    }

    public void buildDeck() throws Exception{

        /**
         * Add parameters to the header file
         */

        this.addParameter("", "");

        this.addParameter("Fielding Distance (cm)", fieldingDistance);
        this.addParameter("", "");

        this.addParameter("Detector Diameter (cm)", detectorDiameter);
        this.addParameter("Detector Thickness (um)", detectorThickness);
        this.addParameter("Filter Side Length (cm)", filterSideLength);
        this.addParameter("Tally Standoff (cm)", tallyStandoff);
        this.addParameter("", "");

        this.addParameter("Filter Material", filterMaterial.getName());
        this.addParameter("1x1 Filter Thickness (um)", filterThicknesses[0]);
        this.addParameter("(2/3)x1 Filter Thickness (um)", filterThicknesses[1]);
        this.addParameter("(1/3)x1 Filter Thickness (um)", filterThicknesses[2]);
        this.addParameter("1x(2/3) Filter Thickness (um)", filterThicknesses[3]);
        this.addParameter("1x(1/3) Filter Thickness (um)", filterThicknesses[4]);
        this.addParameter("", "");

        this.addParameter("Shield Material", shieldMaterial.getName());
        this.addParameter("Shield Thickness (cm)", shieldThickness);
        this.addParameter("", "");

        this.addParameter("Source Mean Energy (MeV)", sourceMeanEnergy);
        this.addParameter("Source Sigma (keV)", sourceSigma);
        this.addParameter("", "");

        for (int i = 0; i < hosts.length; i++) {
            this.addParameter("Nodes used on " + hosts[i], numNodes[i]);
        }
        this.addParameter("Num Particles", String.format("%.2e", 1.0*numParticles));



        /**
         * Some unit conversions
         */

        // um -> cm

        detectorThickness *= 1e-4;

        for (int i = 0; i < filterThicknesses.length; i++){
            filterThicknesses[i] *= 1e-4;
        }

        // keV -> MeV

        sourceSigma *= 1e-3;


        // **************
        // Surface Cards
        // **************

        // Handle the outside world
        MCNP_Surface outsideWorldBoundary = new MCNP_Surface("Outside World Boundary", "so");
        outsideWorldBoundary.addParameter(2.0 * this.fieldingDistance);

        MCNP_Surface sourceTallySurface = new MCNP_Surface("Source Tally Surface", "so");
        sourceTallySurface.addParameter(0.1 * this.fieldingDistance);


        // Handle the "pz" surfaces
        double totalDistance = this.fieldingDistance;
        MCNP_Surface tccShieldFace = new MCNP_Surface("TCC Shield Face", "pz");
        tccShieldFace.addParameter(totalDistance);

        totalDistance += shieldThickness;
        MCNP_Surface backShieldFace = new MCNP_Surface("Back Shield Face", "pz");
        backShieldFace.addParameter(totalDistance);

        totalDistance += detectorThickness;
        MCNP_Surface backDetectorFace = new MCNP_Surface("Back Face of the Detector", "pz");
        backDetectorFace.addParameter(totalDistance);
        totalDistance -= detectorThickness;

        MCNP_Surface[] filterFaces = new MCNP_Surface[5];
        String[] names = {"1x1", "(2/3)x1", "(1/3)x1", "1x(2/3)", "1x(1/3)"};
        for (int i = 0; i < filterThicknesses.length; i++) {
            totalDistance -= filterThicknesses[i];
            filterFaces[i] = new MCNP_Surface(names[i] + " Filter Front Face", "pz");
            filterFaces[i].addParameter(totalDistance);
        }


        // Handle the "cz" surfaces
        MCNP_Surface detectorEdge = new MCNP_Surface("Detector Edge", "cz");
        detectorEdge.addParameter(detectorDiameter / 2.0);


        // Handle the "px" surfaces
        MCNP_Surface [] xFilterEdges = new MCNP_Surface[4];
        for (int i = 0; i < xFilterEdges.length; i++) {
            xFilterEdges[i] = new MCNP_Surface("x Filter Edge " + i, "px");
            xFilterEdges[i].addParameter(filterSideLength * (2.0*i - 3.0) / 6.0);
        }

        MCNP_Surface [] xTallyLeftEdges = new MCNP_Surface[3];
        for (int i = 0; i < xTallyLeftEdges.length; i++) {
            xTallyLeftEdges[i] = new MCNP_Surface("x Tally Left Edge " + i, "px");
            xTallyLeftEdges[i].addParameter(( filterSideLength * (2.0*i - 3.0) / 6.0 ) + tallyStandoff);
        }

        MCNP_Surface [] xTallyRightEdges = new MCNP_Surface[3];
        for (int i = 0; i < xTallyRightEdges.length; i++) {
            xTallyRightEdges[i] = new MCNP_Surface("x Tally Right Edge " + i, "px");
            xTallyRightEdges[i].addParameter(( filterSideLength * (2.0*i - 1.0) / 6.0 ) - tallyStandoff);
        }


        // Handle the "py" surfaces
        MCNP_Surface [] yFilterEdges = new MCNP_Surface[4];
        for (int i = 0; i < yFilterEdges.length; i++) {
            yFilterEdges[i] = new MCNP_Surface("y Filter Edge " + i, "py");
            yFilterEdges[i].addParameter(filterSideLength * (2.0*i - 3.0) / 6.0);
        }

        MCNP_Surface [] yTallyBottomEdges = new MCNP_Surface[3];
        for (int i = 0; i < yTallyBottomEdges.length; i++) {
            yTallyBottomEdges[i] = new MCNP_Surface("y Tally Bottom Edge " + i, "py");
            yTallyBottomEdges[i].addParameter(( filterSideLength * (2.0*i - 3.0) / 6.0 ) + tallyStandoff);
        }

        MCNP_Surface [] yTallyTopEdges = new MCNP_Surface[3];
        for (int i = 0; i < yTallyTopEdges.length; i++) {
            yTallyTopEdges[i] = new MCNP_Surface("y Tally Top Edge " + i, "py");
            yTallyTopEdges[i].addParameter(( filterSideLength * (2.0*i - 1.0) / 6.0 ) - tallyStandoff);
        }


        // ***********
        // Cell cards
        // ***********

        MCNP_SurfaceCollection unionSurfaces = new MCNP_SurfaceCollection(true);
        unionSurfaces.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.NEGATIVE);
        unionSurfaces.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.POSITIVE);
        unionSurfaces.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.NEGATIVE);
        unionSurfaces.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.POSITIVE);

        MCNP_SurfaceCollection surfaces = new MCNP_SurfaceCollection(false);
        surfaces.addSurface(tccShieldFace, MCNP_Volume.Orientation.POSITIVE);
        surfaces.addSurface(backShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        surfaces.addSurface(detectorEdge, MCNP_Volume.Orientation.NEGATIVE);
        surfaces.addSubCollection(unionSurfaces);

        MCNP_Cell shieldPlate = new MCNP_Cell("Shield Plate", shieldMaterial, 1);
        shieldPlate.setSurfaces(surfaces);
        this.addCell(shieldPlate);

        MCNP_Cell baseFilter = new MCNP_Cell("1x1 Base Filter", filterMaterial, 1);
        baseFilter.addSurface(backShieldFace , MCNP_Volume.Orientation.NEGATIVE);
        baseFilter.addSurface(filterFaces[0] , MCNP_Volume.Orientation.POSITIVE);
        baseFilter.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        baseFilter.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        baseFilter.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        baseFilter.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(baseFilter);

        MCNP_Cell filter2 = new MCNP_Cell("(2/3)x1 Filter", filterMaterial, 1);
        filter2.addSurface(filterFaces[0], MCNP_Volume.Orientation.NEGATIVE);
        filter2.addSurface(filterFaces[1], MCNP_Volume.Orientation.POSITIVE);
        filter2.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter2.addSurface(xFilterEdges[2], MCNP_Volume.Orientation.NEGATIVE);
        filter2.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter2.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter2);

        MCNP_Cell filter2Void = new MCNP_Cell("(1/3)x1 Void", null, 1);
        filter2Void.addSurface(filterFaces[0], MCNP_Volume.Orientation.NEGATIVE);
        filter2Void.addSurface(filterFaces[1], MCNP_Volume.Orientation.POSITIVE);
        filter2Void.addSurface(xFilterEdges[2], MCNP_Volume.Orientation.POSITIVE);
        filter2Void.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        filter2Void.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter2Void.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter2Void);

        MCNP_Cell filter3 = new MCNP_Cell("(1/3)x1 Filter", filterMaterial, 1);
        filter3.addSurface(filterFaces[1], MCNP_Volume.Orientation.NEGATIVE);
        filter3.addSurface(filterFaces[2], MCNP_Volume.Orientation.POSITIVE);
        filter3.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter3.addSurface(xFilterEdges[1], MCNP_Volume.Orientation.NEGATIVE);
        filter3.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter3.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter3);

        MCNP_Cell filter3Void = new MCNP_Cell("(2/3)x1 Void", null, 1);
        filter3Void.addSurface(filterFaces[1], MCNP_Volume.Orientation.NEGATIVE);
        filter3Void.addSurface(filterFaces[2], MCNP_Volume.Orientation.POSITIVE);
        filter3Void.addSurface(xFilterEdges[1], MCNP_Volume.Orientation.POSITIVE);
        filter3Void.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        filter3Void.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter3Void.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter3Void);

        MCNP_Cell filter4 = new MCNP_Cell("1x(2/3) Filter", filterMaterial, 1);
        filter4.addSurface(filterFaces[2], MCNP_Volume.Orientation.NEGATIVE);
        filter4.addSurface(filterFaces[3], MCNP_Volume.Orientation.POSITIVE);
        filter4.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter4.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        filter4.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter4.addSurface(yFilterEdges[2], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter4);

        MCNP_Cell filter4Void = new MCNP_Cell("1x(1/3) Void", null, 1);
        filter4Void.addSurface(filterFaces[2], MCNP_Volume.Orientation.NEGATIVE);
        filter4Void.addSurface(filterFaces[3], MCNP_Volume.Orientation.POSITIVE);
        filter4Void.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter4Void.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        filter4Void.addSurface(yFilterEdges[2], MCNP_Volume.Orientation.POSITIVE);
        filter4Void.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter4Void);

        MCNP_Cell filter5 = new MCNP_Cell("1x(1/3) Filter", filterMaterial, 1);
        filter5.addSurface(filterFaces[3], MCNP_Volume.Orientation.NEGATIVE);
        filter5.addSurface(filterFaces[4], MCNP_Volume.Orientation.POSITIVE);
        filter5.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter5.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        filter5.addSurface(yFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter5.addSurface(yFilterEdges[1], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter5);

        MCNP_Cell filter5Void = new MCNP_Cell("1x(2/3) Void", null, 1);
        filter5Void.addSurface(filterFaces[3], MCNP_Volume.Orientation.NEGATIVE);
        filter5Void.addSurface(filterFaces[4], MCNP_Volume.Orientation.POSITIVE);
        filter5Void.addSurface(xFilterEdges[0], MCNP_Volume.Orientation.POSITIVE);
        filter5Void.addSurface(xFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        filter5Void.addSurface(yFilterEdges[1], MCNP_Volume.Orientation.POSITIVE);
        filter5Void.addSurface(yFilterEdges[3], MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(filter5Void);


        MCNP_Tally protonSignalTally = new MCNP_Tally("Proton Distribution at CR-39 Front Surface",
                MCNP_Tally.TallyType.CELL_AVERAGED_FLUX, MCNP_Particle.proton());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                MCNP_Cell detectorCell = new MCNP_Cell(i + "," + j + " Detector", null, 1);
                detectorCell.addSurface(backDetectorFace, MCNP_Volume.Orientation.NEGATIVE);
                detectorCell.addSurface(backShieldFace, MCNP_Volume.Orientation.POSITIVE);
                detectorCell.addSurface(xTallyLeftEdges[j], MCNP_Volume.Orientation.POSITIVE);
                detectorCell.addSurface(xTallyRightEdges[j], MCNP_Volume.Orientation.NEGATIVE);
                detectorCell.addSurface(yTallyBottomEdges[i], MCNP_Volume.Orientation.POSITIVE);
                detectorCell.addSurface(yTallyTopEdges[i], MCNP_Volume.Orientation.NEGATIVE);
                this.addCell(detectorCell);
                protonSignalTally.addTallyLocation(detectorCell);
            }
        }

        MCNP_Cell sourceSphere = new MCNP_Cell("Source Sphere (Need for tally)", null, 1);
        sourceSphere.addSurface(sourceTallySurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(sourceSphere);

        MCNP_Cell outsideWorld = new MCNP_Cell("Outside World", 0);
        outsideWorld.addSurface(outsideWorldBoundary, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(outsideWorld);


        /**
         * Tally cards
         */

        MCNP_Tally protonSourceTally = new MCNP_Tally("Proton Distribution leaving Source",
                MCNP_Tally.TallyType.SURFACE_INTEGRATED_CURRENT, MCNP_Particle.proton());
        protonSourceTally.addTallyLocation(sourceTallySurface);

        for (int i = 0; i < energyBins.length; i++){
            protonSignalTally.addEnergyBin(energyBins[i]);
            protonSourceTally.addEnergyBin(energyBins[i]);
        }

        this.addTally(protonSignalTally);
        this.addTally(protonSourceTally);


        /**
         * Options / Physics Cards
         */

        //this.addParticleToSimulate(MCNP_Particle.proton());
        //MCNP_Source source = new MCNP_Source("", MCNP_Particle.proton());

        this.addParticleToSimulate(MCNP_Particle.proton());
        MCNP_Source source = new MCNP_Source("", MCNP_Particle.proton());

        source.setDirectionalDistribution(getDirectionalDistribution());
        source.setEnergyDistribution(getGaussianSpectrum(sourceMeanEnergy, sourceSigma));
        //source.setEnergyDistribution(get3He3HeEnergyDistribution());

        this.setSource(source, numParticles);

    }

    private MCNP_Distribution getDirectionalDistribution(){

        Double l = fieldingDistance;
        Double r = filterSideLength * Math.sqrt(2) / 2.0;

        Double mu = 1 / (Math.sqrt(Math.pow(r/l,2)+1));
        mu = Math.min(mu, 0.999999);

        Vector<Double> dirDistNodes = new Vector<>();
        dirDistNodes.add(-1.0);
        dirDistNodes.add(mu);
        dirDistNodes.add(1.0);

        Vector<Double> dirDistProb = new Vector<>();
        dirDistProb.add(0.0);
        dirDistProb.add((1+mu)/2);
        dirDistProb.add((1-mu)/2);

        Vector<Double> dirDistBiases = new Vector<>();
        dirDistBiases.add(0.0);
        dirDistBiases.add(0.0);
        dirDistBiases.add(1.0);


        MCNP_Distribution dirDist = new MCNP_Distribution();
        dirDist.setNodes(dirDistNodes);
        dirDist.setProbabilities(dirDistProb);
        dirDist.setBiases(dirDistBiases);

        return dirDist;
    }

    private MCNP_Distribution getGaussianSpectrum(double mu, double sigma) {

        NormalDistribution normalDistribution = new NormalDistribution(mu, sigma);
        double[] energyNodes = MCNP_API_Utilities.linspace(0, 15, 0.1);

        MCNP_Distribution energyDist = new MCNP_Distribution();
        Vector<Double> nodes = new Vector<>(energyNodes.length);
        Vector<Double> probs = new Vector<>(energyNodes.length);
        for (int i = 0; i < energyNodes.length; i++){
            double E = energyNodes[i];
            double p = normalDistribution.density(E);
            nodes.add(E);
            probs.add(p);
        }
        energyDist.setNodes(nodes, MCNP_Distribution.NodeOption.EVALUATED_POINTS);
        energyDist.setProbabilities(probs);

        return energyDist;
    }

    private MCNP_Distribution get3He3HeEnergyDistribution() {

        NormalDistribution normalDistribution = new NormalDistribution(9, 1);
        double[] energyNodes = MCNP_API_Utilities.linspace(0, 16, 0.1);

        MCNP_Distribution energyDist = new MCNP_Distribution();
        Vector<Double> nodes = new Vector<>(energyNodes.length);
        Vector<Double> probs = new Vector<>(energyNodes.length);
        for (int i = 0; i < energyNodes.length; i++){
            double E = energyNodes[i];
            double p = Math.sqrt(E) * (1.0 - normalDistribution.cumulativeProbability(E));
            nodes.add(E);
            probs.add(p);
        }
        energyDist.setNodes(nodes, MCNP_Distribution.NodeOption.EVALUATED_POINTS);
        energyDist.setProbabilities(probs);

        return energyDist;

    }

    public static double[][][] parseOutput(File outputFile) throws Exception{

        ArrayList<ArrayList<Double>> energyNodes = new ArrayList<>();
        ArrayList<ArrayList<Double>> tallyValues = new ArrayList<>();
        ArrayList<ArrayList<Double>> uncertainties = new ArrayList<>();

        Scanner s = new Scanner(outputFile);

        while (s.hasNext()){
            String temp = s.nextLine();

            // Flag that we're entering a tally
            if (temp.contains("1tally") && temp.contains("nps")){

                boolean tallyDone = false;
                boolean cellTally = false;
                while(!tallyDone) {

                    // Flag that we're right at the data
                    while (!s.next().equals("energy")) {
                    }

                    ArrayList<Double> E = new ArrayList<>();
                    ArrayList<Double> V = new ArrayList<>();
                    ArrayList<Double> U = new ArrayList<>();

                    // Flag that we've reached the end of a section of data
                    while (!s.hasNext("total")) {


                        E.add(Double.parseDouble(s.next()));
                        V.add(Double.parseDouble(s.next()));

                        double unc = V.get(V.size() - 1);
                        unc *= Double.parseDouble(s.next());
                        U.add(unc);
                    }

                    energyNodes.add(E);
                    tallyValues.add(V);
                    uncertainties.add(U);

                    s.nextLine(); s.nextLine(); s.nextLine();
                    tallyDone = !s.nextLine().contains("cell");

                }
            }
        }

        s.close();

        double[][][] data = new double[energyNodes.size()][3][energyNodes.get(0).size()];
        for (int i = 0; i < data.length; i++){
            for (int j = 0; j < data[i][0].length; j++){

                data[i][0][j] = energyNodes.get(i).get(j);
                data[i][1][j] = tallyValues.get(i).get(j);
                data[i][2][j] = uncertainties.get(i).get(j);

            }
        }

        String outputExt = outputFile.getName().split("\\.")[1];
        File logFile = new File(outputFile.getAbsolutePath().replace(outputExt, "parsed"));

        FileWriter writer = new FileWriter(logFile);
        for (int i = 0; i < energyNodes.get(0).size(); i++){
            String prefix = "";
            for (int j = 0; j < energyNodes.size(); j++){
                writer.write(String.format(prefix + "%.8e,%.8e,%.8e",
                        energyNodes.get(j).get(i),
                        tallyValues.get(j).get(i),
                        uncertainties.get(j).get(i)));
                prefix = ",";
            }
            writer.write("\n");
        }
        writer.close();

        return data;
    }



}
