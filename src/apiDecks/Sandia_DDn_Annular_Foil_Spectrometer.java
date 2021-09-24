package apiDecks;

import MCNP_API.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by lahmann on 2016-11-22.
 */
public class Sandia_DDn_Annular_Foil_Spectrometer extends MCNP_Deck{

    /**
     * Detector dimensions
     */

    private Double fieldingDistance = 30.0;                 // Distance of the CH foil to TCC
    private Double separationDistance = 20.0;               // Distance between the CH foil and detector

    private Double pitchDistance = 8.0;                     // Distance of the Foil "center" to detector center
    private Double chFoilThickness = 10.0 * 1e-4;           // Thickness of the CH foil
    private Double foilDiameter  = 4.0;                     // Distance between the inner and outer diameter of the foil

    private Double detectorDiameter = 5.0;                  // Diameter of the detector
    private Double detectorThickness = 1500 * 1e-4;         // Thickness of the detector


    /**
     * Detector materials
     */

    private MCNP_Material tubeMaterial = MCNP_Material.aluminum("70c");
    private MCNP_Material conversionMaterial = MCNP_Material.ch2("70c");
    private MCNP_Material detectorMaterial = null;



    /**
     * Tally parameters
     */

    private Double maxEnergyBound = 3.0;
    private Double energyBinWidth = 0.025;


    /**
     * Source parameters
     */

    private File sourceFile = new File("./lib/DD_Tion_2.0_rhoR_1.3_Equator.source");


    private final static String[] hosts = {"han", "luke", "ben", "chewie"};    // Computer hosts to use
    private final static Integer  numNodes = 180;                              // Number of nodes to use (max 192)
    private Integer numSimulatedNeutrons = (int) 1e8;   // Number of particles to simulate



    public static void main(String ... args) throws Exception{

        scanSignalEfficiency();

    }

    private static void scanSignalEfficiency() throws Exception {
        double[] cHThicknesses = new double[]{ 10.0, 13.0, 16.0, 19.0, 22.0, 25.0, 28.0};
        double[] separations   = new double[]{ 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0};

        for (double chThickness : cHThicknesses) {
            for (double separation : separations) {

                // Build the deck
                Sandia_DDn_Annular_Foil_Spectrometer spectrometer = new Sandia_DDn_Annular_Foil_Spectrometer(
                        "Sandia DDn Spectrometer - Annular Foil");
                spectrometer.chFoilThickness = chThickness * 1e-4;
                spectrometer.separationDistance = separation;
                spectrometer.buildDeck();


                System.out.printf("Starting %.1f um foil %.1f cm separation case ...\n", chThickness, separation);


                // Run the source file (liner) version
                spectrometer.setSourceByFile();
                MCNP_Job job = new MCNP_Job("DDn_Spectrometer_1e8_Annular_Foil_Pitch_8.0_Clean_Liner", spectrometer);
                System.out.print("  -> Running liner case ... ");
                //job.runMPIJob(numNodes, false, hosts);
                System.out.println("Done!");


                // Run the temperature (no liner) version
                spectrometer.setSourceByTemperature(2.0);
                job = new MCNP_Job("DDn_Spectrometer_1e8_Annular_Foil_Pitch_8.0_Clean_NoLiner", spectrometer);
                System.out.print("  -> Running no-liner case ... ");
                //job.runMPIJob(numNodes, false, hosts);
                System.out.println("Done!");

            }
        }
    }


    public Sandia_DDn_Annular_Foil_Spectrometer(String name){
        super(name);
    }

    public void buildDeck() throws Exception{



        /**
         * Add parameters to the header file
         */

        this.addParameter("", "");

        this.addParameter("Fielding Distance (cm)", fieldingDistance);
        this.addParameter("Seperation Distance (cm)", separationDistance);
        this.addParameter("", "");

        this.addParameter("Pitch Distance (cm)", pitchDistance);
        this.addParameter("CH Foil 'Diameter' (cm)", foilDiameter);
        this.addParameter("CH Foil Thickness (um)", 1e4*chFoilThickness);
        this.addParameter("", "");

        this.addParameter("Detector Diameter (cm)", detectorDiameter);
        this.addParameter("Detector Thickness (um)", detectorThickness * 1e4);
        this.addParameter("", "");


        String hostList = "";
        boolean first = true;
        for (String host : this.hosts){
            if (!first) hostList += ", ";
            hostList += host;
            first = false;
        }

        this.addParameter("Hosts Used", hostList);
        this.addParameter("Number of Nodes Used", this.numNodes);

        /**
         * Some unit conversions
         */


        /**
         * Surface Cards
         */

        // Handle the "pz" surfaces
        double totalDistance = this.fieldingDistance;
        MCNP_Surface chFoilFrontFace = new MCNP_Surface("Front Face of CH Foil", "pz");
        chFoilFrontFace.addParameter(totalDistance);


        totalDistance += chFoilThickness;
        MCNP_Surface chFoilBackFace = new MCNP_Surface("Back Face of CH Foil", "pz");
        chFoilBackFace.addParameter(totalDistance);


        totalDistance += separationDistance;
        MCNP_Surface detectorFrontFace = new MCNP_Surface("Front Face of the Detector", "pz");
        detectorFrontFace.addParameter(totalDistance);


        totalDistance += detectorThickness;
        MCNP_Surface detectorBackFace = new MCNP_Surface("Back Face of the Detector", "pz");
        detectorBackFace.addParameter(totalDistance);


        MCNP_Surface outsideWorldBoundary = new MCNP_Surface("Outside World Boundary", "so");
        outsideWorldBoundary.addParameter(2.0*totalDistance);



        // Handle the "cz" surfaces
        MCNP_Surface chFoilOuterSurface = new MCNP_Surface("Outer Surface of the CH Foil", "cz");
        chFoilOuterSurface.addParameter(pitchDistance / 2.0 + foilDiameter);


        MCNP_Surface chFoilInnerSurface = new MCNP_Surface("Inner Surface of the CH Foil", "cz");
        chFoilInnerSurface.addParameter(pitchDistance / 2.0);


        MCNP_Surface detectorOuterSurface = new MCNP_Surface("Detector Outer Surface", "cz");
        detectorOuterSurface.addParameter(detectorDiameter / 2.0);



        /**
         * Cell cards
         */


        MCNP_Cell chFoil = new MCNP_Cell("CH Conversion Foil", conversionMaterial, 1);
        chFoil.setForcedCollisions(1.0);
        chFoil.addSurface(chFoilFrontFace, MCNP_Volume.Orientation.POSITIVE);
        chFoil.addSurface(chFoilBackFace, MCNP_Volume.Orientation.NEGATIVE);
        chFoil.addSurface(chFoilInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        chFoil.addSurface(chFoilOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(chFoil);


        MCNP_Cell detector = new MCNP_Cell("Detector", detectorMaterial, 1);
        detector.addSurface(detectorFrontFace   , MCNP_Volume.Orientation.POSITIVE);
        detector.addSurface(detectorBackFace    , MCNP_Volume.Orientation.NEGATIVE);
        detector.addSurface(detectorOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(detector);


        MCNP_Cell outsideWorld = new MCNP_Cell("Outside World", 0);
        outsideWorld.addSurface(outsideWorldBoundary, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(outsideWorld);


        /**
         * Tally cards
         */


        MCNP_Tally protonSignalTally = new MCNP_Tally("Proton Distribution at CR-39 Front Surface",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.proton());
        protonSignalTally.addTallyLocation(detectorFrontFace);

        MCNP_Tally neutronBackgroundTally = new MCNP_Tally("Neutron Distribution at CR-39 Front Surface",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.neutron());
        neutronBackgroundTally.addTallyLocation(detectorFrontFace);


        Double energyBin = 0.0;
        while(energyBin <= this.maxEnergyBound){
            protonSignalTally.addEnergyBin(energyBin);
            neutronBackgroundTally.addEnergyBin(energyBin);
            energyBin += this.energyBinWidth;
        }


        this.addTally(protonSignalTally);
        this.addTally(neutronBackgroundTally);

        /**
         * Options / Physics Cards
         */

        this.addParticleToSimulate(MCNP_Particle.neutron());
        this.addParticleToSimulate(MCNP_Particle.proton());

    }


    /**
     * Set the neutron source card using a temperature and number of neutrons
     * @param temperature
     */
    private void setSourceByTemperature(Double temperature){
        this.addParameter("", "");
        this.addParameter("Neutron Source Type", "DD");
        this.addParameter("Ion Temperature (keV)", temperature);
        this.addParameter("Number of Neutrons Simulated", numSimulatedNeutrons);

        String name = temperature.toString() + " keV Neutron Source:";

        Double temperatureInMeV = temperature/1000;

        MCNP_Source neutronSource = new MCNP_Source(name, MCNP_Particle.neutron());

        Vector<Double> energyDistProbs = new Vector<Double>();
        energyDistProbs.add(-4.0);
        energyDistProbs.add(-temperatureInMeV);
        energyDistProbs.add(-2.0);

        MCNP_Distribution energyDist = new MCNP_Distribution();
        energyDist.setProbabilities(energyDistProbs);


        MCNP_Distribution dirDistribution = getDirectionalDistribution();

        neutronSource.setEnergyDistribution(energyDist);
        neutronSource.setDirectionalDistribution(dirDistribution);

        this.setSource(neutronSource, numSimulatedNeutrons);
    }


    /**
     * Set the neutron source card using a file
     */
    private void setSourceByFile(){
        this.addParameter("", "");
        this.addParameter("Source From File", sourceFile.getName());
        this.addParameter("Number of Neutrons Simulated", numSimulatedNeutrons);

        String name = "Source from " + sourceFile.getName();
        MCNP_Source neutronSource = new MCNP_Source(name, MCNP_Particle.neutron());


        // Energy Distribution from File

        Scanner s;
        Vector<Double> energyDistNodes = new Vector<>();
        Vector<Double> energyDistProbs = new Vector<>();

        try {
            s = new Scanner(sourceFile);

            while (s.hasNext()) {
                if (s.hasNext("C")) {
                    s.nextLine();
                } else {
                    energyDistNodes.add(s.nextDouble());
                    energyDistProbs.add(s.nextDouble());
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }


        MCNP_Distribution energyDist = new MCNP_Distribution();
        energyDist.setNodes(energyDistNodes);
        energyDist.setProbabilities(energyDistProbs);
        neutronSource.setEnergyDistribution(energyDist);


        MCNP_Distribution dirDistribution = getDirectionalDistribution();

        neutronSource.setEnergyDistribution(energyDist);
        neutronSource.setDirectionalDistribution(dirDistribution);

        this.setSource(neutronSource, numSimulatedNeutrons);
    }

    private MCNP_Distribution getDirectionalDistribution(){

        Double l = fieldingDistance;
        Double r = pitchDistance / 2.0 + foilDiameter;

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

    public static void parseOutput(File logFile, File outputFile) throws Exception{

        ArrayList<Double> energyNodes = new ArrayList<Double>();
        ArrayList<Double> tallyValues  = new ArrayList<Double>();
        ArrayList<Double> uncertainties = new ArrayList<Double>();

        Scanner s = new Scanner(outputFile);

        while (s.hasNext()){
            String temp = s.next();

            if (temp.equals("1tally")){
                boolean isProton = false;

                while (!s.hasNext("energy")){
                    if (s.next().equals("proton"))  isProton = true;
                }
                s.next();

                if (isProton) {
                    while (!s.hasNext("total")) {
                        energyNodes.add(Double.parseDouble(s.next()));
                        tallyValues.add(Double.parseDouble(s.next()));

                        double unc = tallyValues.get(tallyValues.size() - 1);
                        unc *= Double.parseDouble(s.next());
                        uncertainties.add(unc);
                    }

                    break;
                }
            }
        }

        s.close();
        FileWriter writer = new FileWriter(logFile, true);

        // Write the nodes
        boolean first = true;
        for (Double node : energyNodes){
            if (!first) writer.write(",");
            writer.write(node.toString());
            first = false;
        }
        writer.write("\n");

        // Write the values
        first = true;
        for (Double value : tallyValues){
            if (!first) writer.write(",");
            writer.write(value.toString());
            first = false;
        }
        writer.write("\n");

        // Write the uncertainties
        first = true;
        for (Double uncertainty : uncertainties){
            if (!first) writer.write(",");
            writer.write(uncertainty.toString());
            first = false;
        }
        writer.write("\n");

        writer.close();
    }

}
