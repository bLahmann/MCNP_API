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
public class Sandia_DDn_Annular_Detector_Spectrometer extends MCNP_Deck{

    /**
     * Detector dimensions
     */

    private Double fieldingDistance = 30.0;                 // Distance of the front of the tube to TCC (cm)

    private Double shieldingFaceWidth = 12.8;               // Width of the square shielding faces
    private Double shieldingFaceHeight = 12.8;              // Height of the square shielding faces
    private Double shieldingFaceEdgeRadius = 7.3025;        // Radius of the shield face rounded edges
    private Double shieldingFaceThickness = 0.635;          // Thickness of the shielding faces
    private Double shieldingMountThickness = 1.27;          // Thickness of the hollow square mount that the shielding faces attach on to

    private Double tubeInnerDiameter = 9.7384;              // Inner diameter of the spectrometer tube housing
    private Double tubeOuterDiameter = 10.16;               // Outer diameter of the spectrometer tube housing
    private Double tubeLength = 99.3775;                    // Length of the spectrometer tube

    private Double cr39HolderWidth = 6.9850;                // Width of the CR-39 holder
    private Double cr39HolderHeight = 6.350;                // Height of the CR-39 holder
    private Double cr39HolderEdgeRadius = 4.27;             // Radius of the CR-39 holder edge
    private Double cr39HolderThickness = 0.9525;            // Thickness of the CR-39 holder
    private Double cr39HolderSeparationDistance = 20.0;     // Separation distance between CR-39 holders
    private Double cr39HolderCenterOffset = 0.3175;         // Offset of the center of the CR-39 holder
    private Double cr39HolderDiameter = 4.9276;             // Inner diameter of the CR-39 holder

    private Double chFoilThickness = 10.0 * 1e-4;           // Thickness of the CH foil

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
    private Integer numSimulatedNeutrons = (int) 1e9;   // Number of particles to simulate



    public static void main(String ... args) throws Exception{

        scanSignalEfficiency();

    }

    private static void scanSignalEfficiency() throws Exception {
        double[] cHThicknesses = new double[]{ 14.0, 17.0, 20.0, 23.0, 26.0, 29.0, 32.0 };
        double[] separations   = new double[]{ 20.0, 24.0, 28.0, 32.0, 36.0, 40.0, 44.0, 48.0 };

        for (double chThickness : cHThicknesses) {
            for (double separation : separations) {

                // Build the deck
                Sandia_DDn_Annular_Detector_Spectrometer spectrometer = new Sandia_DDn_Annular_Detector_Spectrometer(
                        "Sandia DDn Spectrometer - Annular Detector");
                spectrometer.chFoilThickness = chThickness * 1e-4;
                spectrometer.cr39HolderSeparationDistance = separation;
                spectrometer.buildDeck();


                System.out.printf("Starting %.1f um foil %.1f cm separation case ...\n", chThickness, separation);


                // Run the source file (liner) version
                spectrometer.setSourceByFile();
                MCNP_Job job = new MCNP_Job("DDn_Spectrometer_Annular_Detector_Clean_Liner", spectrometer);
                System.out.print("  -> Running liner case ... ");
                //job.runMPIJob(numNodes, false, hosts);
                System.out.println("Done!");


                // Run the temperature (no liner) version
                spectrometer.setSourceByTemperature(2.0);
                job = new MCNP_Job("DDn_Spectrometer_Annular_Detector_Clean_NoLiner", spectrometer);
                System.out.print("  -> Running no-liner case ... ");
                //job.runMPIJob(numNodes, false, hosts);
                System.out.println("Done!");

            }
        }
    }


    public Sandia_DDn_Annular_Detector_Spectrometer(String name){
        super(name);
    }

    public void buildDeck() throws Exception{

        /**
         * Add parameters to the header file
         */

        this.addParameter("", "");

        this.addParameter("Fielding Distance (cm)", fieldingDistance);
        this.addParameter("", "");

        this.addParameter("Shield Face Width (cm)", shieldingFaceWidth);
        this.addParameter("Shield Face Height (cm)", shieldingFaceHeight);
        this.addParameter("Shield Face Edge Radius (cm)", shieldingFaceEdgeRadius);
        this.addParameter("Shield Face Thickness (cm)", shieldingFaceThickness);
        this.addParameter("Shield Mounting Thickness (cm)", shieldingMountThickness);
        this.addParameter("", "");

        this.addParameter("Tube Outer Diameter (cm)", tubeOuterDiameter);
        this.addParameter("Tube Inner Diameter (cm)", tubeInnerDiameter);
        this.addParameter("Tube Length (cm)", tubeLength);
        this.addParameter("", "");

        this.addParameter("CR-39 Holder Width (cm)", cr39HolderWidth);
        this.addParameter("CR-39 Holder Height (cm)", cr39HolderHeight);
        this.addParameter("CR-39 Holder Edge Radius (cm)", cr39HolderEdgeRadius);
        this.addParameter("CR-39 Holder Thickness (cm)", cr39HolderThickness);
        this.addParameter("CR-39 Holder Separation Distance (cm)", cr39HolderSeparationDistance);
        this.addParameter("CR-39 Holder Center Offset (cm)", cr39HolderCenterOffset);
        this.addParameter("CR-39 Holder Inner Diameter (cm)", cr39HolderDiameter);
        this.addParameter("", "");

        this.addParameter("CH Foil Thickness (um)", chFoilThickness * 1e4);
        this.addParameter("", "");

        this.addParameter("Detector Diameter (cm)", detectorDiameter);
        this.addParameter("Detector Thickness (um)", detectorThickness * 1e4);
        this.addParameter("", "");

        this.addParameter("Number of Neutrons Simulated", String.format("%.2e", (double) numSimulatedNeutrons));
        this.addParameter("Neutron Source File", sourceFile.getName());
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
        MCNP_Surface frontShieldFaceStart = new MCNP_Surface("Front Shield Face Start", "pz");
        frontShieldFaceStart.addParameter(totalDistance);


        totalDistance += shieldingFaceThickness;
        MCNP_Surface frontShieldFaceEnd = new MCNP_Surface("Front Shield Face End", "pz");
        frontShieldFaceEnd.addParameter(totalDistance);


        totalDistance += shieldingMountThickness;
        MCNP_Surface frontShieldMountEnd = new MCNP_Surface("Front Shield Mount End", "pz");
        frontShieldMountEnd.addParameter(totalDistance);


        totalDistance += chFoilThickness;
        MCNP_Surface chFoilEnd = new MCNP_Surface("CH Foil End", "pz");
        chFoilEnd.addParameter(totalDistance);


        totalDistance += cr39HolderThickness;
        MCNP_Surface firstCR39HolderEnd = new MCNP_Surface("1st CR-39 Holder End", "pz");
        firstCR39HolderEnd.addParameter(totalDistance);


        totalDistance += cr39HolderSeparationDistance;
        MCNP_Surface outsideWorldBoundary = new MCNP_Surface("Outside World Boundary", "so");
        outsideWorldBoundary.addParameter(2.0*totalDistance);


        totalDistance -= (cr39HolderThickness + detectorThickness);
        MCNP_Surface detectorFrontFace = new MCNP_Surface("Detector Front Face", "pz");
        detectorFrontFace.addParameter(totalDistance);


        totalDistance += detectorThickness;
        MCNP_Surface detectorBackFace = new MCNP_Surface("Detector Back Face", "pz");
        detectorBackFace.addParameter(totalDistance);



        // Handle the "cz" surfaces
        MCNP_Surface shieldFaceEdgeSurface = new MCNP_Surface("Edge Surface of the Shield Faces", "cz");
        shieldFaceEdgeSurface.addParameter(shieldingFaceEdgeRadius);

        MCNP_Surface cr39HolderEdgeSurface = new MCNP_Surface("Edge Surface of the CR-39 Holder", "c/z");
        cr39HolderEdgeSurface.addParameter(0.0);
        cr39HolderEdgeSurface.addParameter(cr39HolderCenterOffset);
        cr39HolderEdgeSurface.addParameter(cr39HolderEdgeRadius);

        MCNP_Surface innerCR39HolderSurface = new MCNP_Surface("Inner CR-39 Holder Surface", "c/z");
        innerCR39HolderSurface.addParameter(0.0);
        innerCR39HolderSurface.addParameter(cr39HolderCenterOffset);
        innerCR39HolderSurface.addParameter(cr39HolderDiameter / 2.0);

        MCNP_Surface innerTubeSurface = new MCNP_Surface("Inner Tube Surface", "cz");
        innerTubeSurface.addParameter(tubeInnerDiameter / 2.0);

        MCNP_Surface outerTubeSurface = new MCNP_Surface("Outer Tube Surface", "cz");
        outerTubeSurface.addParameter(tubeOuterDiameter / 2.0);

        MCNP_Surface detectorInnerSurface = new MCNP_Surface("Annular Detector Inner Surface", "cz");
        detectorInnerSurface.addParameter(shieldingFaceHeight - detectorDiameter / 2.0);


        MCNP_Surface detectorOuterSurface = new MCNP_Surface("Annular Detector Outer Surface", "cz");
        detectorOuterSurface.addParameter(shieldingFaceHeight + detectorDiameter / 2.0);



        // Handle the "px" surfaces
        MCNP_Surface leftShieldFace = new MCNP_Surface("Left Shield Face", "px");
        leftShieldFace.addParameter(-1.0*shieldingFaceWidth/2.0);

        MCNP_Surface rightShieldFace = new MCNP_Surface("Right Shield Face", "px");
        rightShieldFace.addParameter(shieldingFaceWidth/2.0);

        MCNP_Surface leftCR39HolderFace = new MCNP_Surface("Left CR-39 Holder Face", "px");
        leftCR39HolderFace.addParameter(-1.0*cr39HolderWidth/2.0);

        MCNP_Surface rightCR39HolderFace = new MCNP_Surface("Right CR-39 Holder Face", "px");
        rightCR39HolderFace.addParameter(cr39HolderWidth/2.0);



        // Handle the "py" surfaces
        MCNP_Surface bottomShieldFace = new MCNP_Surface("Bottom Shield Face", "py");
        bottomShieldFace.addParameter(-1.0*shieldingFaceHeight / 2.0);

        MCNP_Surface topShieldFace = new MCNP_Surface("Top Shield Face", "py");
        topShieldFace.addParameter(shieldingFaceHeight / 2.0);

        MCNP_Surface bottomCR39HolderFace = new MCNP_Surface("Bottom Shield Face", "py");
        bottomCR39HolderFace.addParameter(-1.0*cr39HolderHeight/2.0 + cr39HolderCenterOffset);

        MCNP_Surface topCR39HolderFace = new MCNP_Surface("Top Shield Face", "py");
        topCR39HolderFace.addParameter(cr39HolderHeight/2.0 + cr39HolderCenterOffset);



        /**
         * Cell cards
         */


        MCNP_Cell frontShieldPlate = new MCNP_Cell("Front Shield Plate", tubeMaterial, 1);
        frontShieldPlate.addSurface(frontShieldFaceStart, MCNP_Volume.Orientation.POSITIVE);
        frontShieldPlate.addSurface(frontShieldFaceEnd, MCNP_Volume.Orientation.NEGATIVE);
        frontShieldPlate.addSurface(leftShieldFace, MCNP_Volume.Orientation.POSITIVE);
        frontShieldPlate.addSurface(rightShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        frontShieldPlate.addSurface(bottomShieldFace, MCNP_Volume.Orientation.POSITIVE);
        frontShieldPlate.addSurface(topShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        frontShieldPlate.addSurface(shieldFaceEdgeSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(frontShieldPlate);


        MCNP_Cell frontShieldMount = new MCNP_Cell("Front Shield Mount", tubeMaterial, 1);
        frontShieldMount.addSurface(frontShieldFaceEnd, MCNP_Volume.Orientation.POSITIVE);
        frontShieldMount.addSurface(frontShieldMountEnd, MCNP_Volume.Orientation.NEGATIVE);
        frontShieldMount.addSurface(leftShieldFace, MCNP_Volume.Orientation.POSITIVE);
        frontShieldMount.addSurface(rightShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        frontShieldMount.addSurface(bottomShieldFace, MCNP_Volume.Orientation.POSITIVE);
        frontShieldMount.addSurface(topShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        frontShieldMount.addSurface(innerTubeSurface, MCNP_Volume.Orientation.POSITIVE);
        frontShieldMount.addSurface(shieldFaceEdgeSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(frontShieldMount);


        MCNP_Cell chFoil = new MCNP_Cell("CH Conversion Foil", conversionMaterial, 1);
        chFoil.setForcedCollisions(1.0);
        chFoil.addSurface(frontShieldMountEnd, MCNP_Volume.Orientation.POSITIVE);
        chFoil.addSurface(chFoilEnd, MCNP_Volume.Orientation.NEGATIVE);
        chFoil.addSurface(leftCR39HolderFace, MCNP_Volume.Orientation.POSITIVE);
        chFoil.addSurface(rightCR39HolderFace, MCNP_Volume.Orientation.NEGATIVE);
        chFoil.addSurface(bottomCR39HolderFace, MCNP_Volume.Orientation.POSITIVE);
        chFoil.addSurface(topCR39HolderFace, MCNP_Volume.Orientation.NEGATIVE);
        chFoil.addSurface(cr39HolderEdgeSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(chFoil);


        MCNP_Cell firstCR39Holder = new MCNP_Cell("1st CR-39 Holder", tubeMaterial, 1);
        firstCR39Holder.addSurface(chFoilEnd, MCNP_Volume.Orientation.POSITIVE);
        firstCR39Holder.addSurface(firstCR39HolderEnd, MCNP_Volume.Orientation.NEGATIVE);
        firstCR39Holder.addSurface(leftCR39HolderFace, MCNP_Volume.Orientation.POSITIVE);
        firstCR39Holder.addSurface(rightCR39HolderFace, MCNP_Volume.Orientation.NEGATIVE);
        firstCR39Holder.addSurface(bottomCR39HolderFace, MCNP_Volume.Orientation.POSITIVE);
        firstCR39Holder.addSurface(topCR39HolderFace, MCNP_Volume.Orientation.NEGATIVE);
        firstCR39Holder.addSurface(cr39HolderEdgeSurface, MCNP_Volume.Orientation.NEGATIVE);
        firstCR39Holder.addSurface(innerCR39HolderSurface, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(firstCR39Holder);

        MCNP_Cell detector = new MCNP_Cell("Detector", detectorMaterial, 1);
        detector.addSurface(detectorFrontFace   , MCNP_Volume.Orientation.POSITIVE);
        detector.addSurface(detectorBackFace    , MCNP_Volume.Orientation.NEGATIVE);
        detector.addSurface(detectorInnerSurface, MCNP_Volume.Orientation.POSITIVE);
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
        Double r = Math.sqrt(Math.pow(shieldingFaceHeight, 2) + Math.pow(shieldingFaceHeight, 2));

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
