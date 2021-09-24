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
public class Sandia_DDn_Shielded_Annular_Foil_Spectrometer extends MCNP_Deck{

    // ********************
    // Detector dimensions
    // ********************

    private boolean tubeModelled         = true;
    private boolean neutronShieldModeled = true;
    private boolean detectorModeled      = false;


    private Double detectorBlastShieldThickness = 0.25 * 2.54;      // Thickness of the plates between TCC and the detector (cm)
    private Double detectorTubeThickess         = 0.25 * 2.54;      // Thickness of the tube surrounding the detector (cm)


    private Double neutronShieldingThickness = 40.0;                // Thickness of the neutron shielding plug (cm)
    private Double neutronShieldingDiameter  =  5.0;                // Diameter of the neutron shielding plug (cm)


    private Double fieldingDistance   = 30.0;                       // Distance of the CH foil to TCC (cm)
    private Double separationDistance = 25.0;                       // Distance between the CH foil and detector (cm)


    private Double pitchDistance   =  6.0;                          // Distance of the foil "center" to detector center (cm)
    private Double foilDiameter    =  3.0;                          // Distance between the inner and outer diameter of the foil (cm)
    private Double chFoilThickness = 25.0 * 1e-4;                   // Thickness of the CH foil (cm)


    private Double detectorDiameter  = 4.5;                         // Diameter of the detector (cm)
    private Double detectorThickness = 1500 * 1e-4;                 // Thickness of the detector (cm)



    // ******************************
    // Scattering material dimensions
    // ******************************

    private boolean spacerModeled = false;

    private Double spacerInnerDiameter  = 1.485 * 2.54;     // cm
    private Double spacerOuterDiameter  = 5.218 * 2.54;     // cm
    private Double spacerHeight         = 0.518 * 2.54;     // cm


    private boolean blastShieldModeled = false;

    private Double blastShieldInnerDiameter = 20.75 * 2.54;     // cm
    private Double blastShieldOuterDiameter = 22.00 * 2.54;     // cm
    private Double blastShieldHeight        =  8.00 * 2.54;     // cm


    private boolean mitlDeckModeled = false;

    private Double mitlDeckInnerDiameter = 2* 15.063 * 2.54;       // cm
    private Double mitlDeckOuterDiameter = 2* 58.0   * 2.54;       // cm
    private Double mitlDeckThickness     = 0.5    * 2.54;       // cm
    private Double mitlDeckOffset        = 1.23   * 2.54;       // Distance between the bottom of the blast shield and top of the mitl deck (cm)



    // **********
    // Materials
    // **********

    private MCNP_Material tubeMaterial          = MCNP_Material.aluminum("70c");
    private MCNP_Material neutronShieldMaterial = MCNP_Material.ch2("70c");
    private MCNP_Material conversionMaterial    = MCNP_Material.ch2("70c");
    private MCNP_Material detectorMaterial      = null;                         // Should be CR-39

    private MCNP_Material spacerMaterial        = Material_Library.titanium("70c");
    private MCNP_Material blastShieldMaterial   = Material_Library.AlSl304_SS("70c");
    private MCNP_Material mitlDeckMaterial      = null;                         // Should be 6061 Al



    // *****************
    // Tally parameters
    // *****************

    private Double maxEnergyBound = 3.0;
    private Double energyBinWidth = 10.0 * 1e-3;



    // ******************
    // Source parameters
    // ******************

    private File sourceFile = new File("./lib/DD_Tion_2.0_rhoR_1.3_Equator.source");



    // **********************
    // Simulation parameters
    // **********************

    private final static String[] hosts = {"han", "luke", "ben", "chewie"};    // Computer hosts to use
    private final static Integer  numNodes = 180;                              // Number of nodes to use (max 192)
    private Integer numSimulatedNeutrons = (int) 1e8;   // Number of particles to simulate



    public static void main(String ... args) throws Exception{

        testTubeEffects();

    }

    private static void testTubeEffects() throws Exception {

        // Build the deck
        Sandia_DDn_Shielded_Annular_Foil_Spectrometer spectrometer = new Sandia_DDn_Shielded_Annular_Foil_Spectrometer(
                "Sandia DDn Spectrometer - Shielded Annular Foil");
        spectrometer.setSourceByTemperature(2.0);


        // Run the case with no materials

        spectrometer.tubeMaterial = Material_Library.aluminum("70c");
        spectrometer.buildDeck();
        System.out.println(spectrometer);

        MCNP_Job job = new MCNP_Job("Tube_Material_Test", spectrometer);
        System.out.print("Running no material model ... ");
        job.plotGeometry();
        //job.runMPIJob(numNodes, false, hosts);
        System.out.println("Done!");

    }



    private static void scanSignalEfficiency() throws Exception {
        double[] shieldThicknesses = new double[]{ 20.0 };
        double[] cHThicknesses     = new double[]{ 25.0 };
        double[] separations       = new double[]{ 25.0 };

        for (double shieldThickness : shieldThicknesses) {
            for (double chThickness : cHThicknesses) {
                for (double separation : separations) {

                    // Build the deck
                    Sandia_DDn_Shielded_Annular_Foil_Spectrometer spectrometer = new Sandia_DDn_Shielded_Annular_Foil_Spectrometer(
                            "Sandia DDn Spectrometer - Shielded Annular Foil");
                    spectrometer.neutronShieldingThickness = shieldThickness;
                    spectrometer.chFoilThickness = chThickness * 1e-4;
                    spectrometer.separationDistance = separation;
                    spectrometer.buildDeck();
                    
                    System.out.printf("Starting %.1f cm shield, %.1f um foil, %.1f cm separation case ...\n", shieldThickness, chThickness, separation);
                    String name = String.format("DDn_Spectrometer_1e8_Voided_Shielded_Annular_Foil_Clean_");


                    // Run the source file (liner) version
                    spectrometer.setSourceByFile();
                    System.out.println(spectrometer);
                    MCNP_Job job = new MCNP_Job(name + "Liner", spectrometer);
                    System.out.print("  -> Running liner case ... ");
                    //job.runMPIJob(numNodes, false, hosts);
                    System.out.println("Done!");


                    // Run the temperature (no liner) version
                    spectrometer.setSourceByTemperature(2.0);
                    job = new MCNP_Job(name + "NoLiner", spectrometer);
                    System.out.print("  -> Running no-liner case ... ");
                    //job.runMPIJob(numNodes, false, hosts);
                    System.out.println("Done!");

                }
            }
        }
    }


    public Sandia_DDn_Shielded_Annular_Foil_Spectrometer(String name){
        super(name);
    }

    public void buildDeck() throws Exception{


        // *************************************************************
        // Null out some materials as needed before we write the header
        // *************************************************************

        if (!tubeModelled)          tubeMaterial = null;
        if (!neutronShieldModeled)  neutronShieldMaterial = null;
        if (!detectorModeled)       detectorMaterial = null;
        if (!spacerModeled)         spacerMaterial = null;
        if (!blastShieldModeled)    blastShieldMaterial = null;
        if (!mitlDeckModeled)       mitlDeckMaterial = null;



        // **********************************
        // Add parameters to the header file
        // **********************************

        this.addParameter("", "");

        this.addParameter("Fielding Distance (cm)", fieldingDistance);
        this.addParameter("Seperation Distance (cm)", separationDistance);
        this.addParameter("", "");


        this.addParameter("Tube Material", tubeMaterial);
        this.addParameter("Blast Shield Face Thickness (cm)", detectorBlastShieldThickness);
        this.addParameter("Tube Thickness (cm)", detectorTubeThickess);
        this.addParameter("", "");


        this.addParameter("Neutron Shielding Material", neutronShieldMaterial);
        this.addParameter("Neutron Shield Plug Thickness (cm)", neutronShieldingThickness);
        this.addParameter("Neutron Shield Plug Diameter (cm)", neutronShieldingDiameter);
        this.addParameter("", "");


        this.addParameter("Conversion Foil Material", conversionMaterial);
        this.addParameter("Conversion Foil 'Diameter' (cm)", foilDiameter);
        this.addParameter("Conversion Foil Thickness (um)", 1e4*chFoilThickness);
        this.addParameter("Pitch Distance (cm)", pitchDistance);
        this.addParameter("", "");


        this.addParameter("Detector Material", detectorMaterial);
        this.addParameter("Detector Diameter (cm)", detectorDiameter);
        this.addParameter("Detector Thickness (um)", detectorThickness * 1e4);
        this.addParameter("", "");


        this.addParameter("Spacer Material", spacerMaterial);
        this.addParameter("Spacer Inner Diameter (cm)", spacerInnerDiameter);
        this.addParameter("Spacer Outer Diameter (cm)", spacerOuterDiameter);
        this.addParameter("Spacer Height (cm)", spacerHeight);
        this.addParameter("", "");


        this.addParameter("Blast Shield Material", blastShieldMaterial);
        this.addParameter("Blast Shield Inner Diameter (cm)", blastShieldInnerDiameter);
        this.addParameter("Blast Shield Outer Diameter (cm)", blastShieldOuterDiameter);
        this.addParameter("Blast Shield Height (cm)", blastShieldHeight);
        this.addParameter("", "");


        this.addParameter("MITL Deck Material", mitlDeckMaterial);
        this.addParameter("MITL Deck Inner Diameter (cm)", mitlDeckInnerDiameter);
        this.addParameter("MITL Deck Outer Diameter (cm)", mitlDeckOuterDiameter);
        this.addParameter("MITL Deck Thickness (cm)", mitlDeckThickness);
        this.addParameter("MITL Deck Offset Distance (cm)", mitlDeckOffset);
        this.addParameter("", "");


        StringBuilder hostList = new StringBuilder();
        String prefix   = "";
        for (String host : this.hosts){
            hostList.append(prefix).append(host);
            prefix = ", ";
        }

        this.addParameter("Hosts Used", hostList);
        this.addParameter("Number of Nodes Used", this.numNodes);



        // *****************
        // Unit conversions
        // *****************



        // **************
        // SURFACE CARDS
        // **************


        // Handle the "pz" surfaces
        double totalDistance = this.fieldingDistance;
        MCNP_Surface firstBlastShieldFrontFace = new MCNP_Surface("Front Face of the First Blast Shield", "pz");
        firstBlastShieldFrontFace.addParameter(totalDistance);


        totalDistance += detectorBlastShieldThickness;
        MCNP_Surface shieldPlugFrontFace = new MCNP_Surface("Front Face of the Shield Plug", "pz");
        shieldPlugFrontFace.addParameter(totalDistance);


        totalDistance += neutronShieldingThickness;
        MCNP_Surface secondBlastShieldFrontFace = new MCNP_Surface("Front Face of the Second Blast Shield", "pz");
        secondBlastShieldFrontFace.addParameter(totalDistance);


        totalDistance += detectorBlastShieldThickness;
        MCNP_Surface chFoilFrontFace = new MCNP_Surface("Front Face of the CH Foil", "pz");
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

        totalDistance += separationDistance;
        MCNP_Surface thirdBlastShieldFrontFace = new MCNP_Surface("Front Face of the Third Blast Shield", "pz");
        thirdBlastShieldFrontFace.addParameter(totalDistance);

        totalDistance += detectorBlastShieldThickness;
        MCNP_Surface thirdBlastShieldBackFace = new MCNP_Surface("Back Face of the Third Blast Shield", "pz");
        thirdBlastShieldBackFace.addParameter(totalDistance);


        MCNP_Surface outsideWorldBoundary = new MCNP_Surface("Outside World Boundary", "so");
        outsideWorldBoundary.addParameter(2.0*totalDistance);


        // Handle the "px" surfaces
        MCNP_Surface spacerTopBoundary = new MCNP_Surface("Spacer Top Boundary", "px");
        spacerTopBoundary.addParameter(spacerHeight / 2.0);

        MCNP_Surface spacerBottomBoundary = new MCNP_Surface("Spacer Bottom Boundary", "px");
        spacerBottomBoundary.addParameter((-1) * spacerHeight / 2.0);

        MCNP_Surface blastShieldTopBoundary = new MCNP_Surface("Blast Shield Top Boundary", "px");
        blastShieldTopBoundary.addParameter(blastShieldHeight / 2.0);

        MCNP_Surface blastShieldBottomBoundary = new MCNP_Surface("Blast Shield Bottom Boundary", "px");
        blastShieldBottomBoundary.addParameter((-1) * blastShieldHeight / 2.0);

        MCNP_Surface mitlDeckTopBoundary = new MCNP_Surface("MITL Deck Top Boundary", "px");
        mitlDeckTopBoundary.addParameter((-1.0) * blastShieldHeight / 2.0 - mitlDeckOffset);

        MCNP_Surface mitlDeckBottomBoundary = new MCNP_Surface("MITL Deck Bottom Boundary", "px");
        mitlDeckBottomBoundary.addParameter((-1.0) * blastShieldHeight / 2.0 - mitlDeckOffset - mitlDeckThickness);


        // Handle the "cz" surfaces

        MCNP_Surface tubeOuterSurface = new MCNP_Surface("Outer Surface of the Al Tube", "cz");
        tubeOuterSurface.addParameter(pitchDistance / 2.0 + foilDiameter + detectorTubeThickess);


        MCNP_Surface chFoilOuterSurface = new MCNP_Surface("Outer Surface of the CH Foil", "cz");
        chFoilOuterSurface.addParameter(pitchDistance / 2.0 + foilDiameter);


        MCNP_Surface chFoilInnerSurface = new MCNP_Surface("Inner Surface of the CH Foil", "cz");
        chFoilInnerSurface.addParameter(pitchDistance / 2.0);


        MCNP_Surface shieldPlugOuterSurface = new MCNP_Surface("Shield Plug Outer Surface", "cz");
        shieldPlugOuterSurface.addParameter(detectorDiameter / 2.0);


        MCNP_Surface detectorOuterSurface = new MCNP_Surface("Detector Outer Surface", "cz");
        detectorOuterSurface.addParameter(detectorDiameter / 2.0);


        // Handle the "cx" surfaces
        MCNP_Surface spacerInnerSurface = new MCNP_Surface("Spacer Inner Surface", "cx");
        spacerInnerSurface.addParameter(spacerInnerDiameter / 2.0);

        MCNP_Surface spacerOuterSurface = new MCNP_Surface("Spacer Outer Surface", "cx");
        spacerOuterSurface.addParameter(spacerOuterDiameter / 2.0);

        MCNP_Surface blastShieldInnerSurface = new MCNP_Surface("Blast Shield Inner Surface", "cx");
        blastShieldInnerSurface.addParameter(blastShieldInnerDiameter / 2.0);

        MCNP_Surface blastShieldOuterSurface = new MCNP_Surface("Blast Shield Outer Surface", "cx");
        blastShieldOuterSurface.addParameter(blastShieldOuterDiameter / 2.0);

        MCNP_Surface mitlDeckInnerSurface = new MCNP_Surface("MITL Deck Inner Surface", "cx");
        mitlDeckInnerSurface.addParameter(mitlDeckInnerDiameter / 2.0);

        MCNP_Surface mitlDeckOuterSurface = new MCNP_Surface("MITL Deck Outer Surface", "cx");
        mitlDeckOuterSurface.addParameter(mitlDeckOuterDiameter / 2.0);





        /**
         * Cell cards
         */

        MCNP_Cell spacer = new MCNP_Cell("Spacer", spacerMaterial, 1);
        spacer.addSurface(spacerTopBoundary, MCNP_Volume.Orientation.NEGATIVE);
        spacer.addSurface(spacerBottomBoundary, MCNP_Volume.Orientation.POSITIVE);
        spacer.addSurface(spacerInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        spacer.addSurface(spacerOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(spacer);

        MCNP_Cell blastShield = new MCNP_Cell("Blast Shield", blastShieldMaterial, 1);
        blastShield.addSurface(blastShieldTopBoundary, MCNP_Volume.Orientation.NEGATIVE);
        blastShield.addSurface(blastShieldBottomBoundary, MCNP_Volume.Orientation.POSITIVE);
        blastShield.addSurface(blastShieldInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        blastShield.addSurface(blastShieldOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(blastShield);

        MCNP_Cell mitlDeck = new MCNP_Cell("MITL Deck", mitlDeckMaterial, 1);
        mitlDeck.addSurface(mitlDeckTopBoundary, MCNP_Volume.Orientation.NEGATIVE);
        mitlDeck.addSurface(mitlDeckBottomBoundary, MCNP_Volume.Orientation.POSITIVE);
        mitlDeck.addSurface(mitlDeckInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        mitlDeck.addSurface(mitlDeckOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(mitlDeck);


        MCNP_Cell firstBlastShield = new MCNP_Cell("1st Detector Blast Shield Plate", tubeMaterial, 1);
        firstBlastShield.addSurface(firstBlastShieldFrontFace , MCNP_Volume.Orientation.POSITIVE);
        firstBlastShield.addSurface(shieldPlugFrontFace       , MCNP_Volume.Orientation.NEGATIVE);
        firstBlastShield.addSurface(chFoilOuterSurface        , MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(firstBlastShield);


        MCNP_Cell shieldPlug = new MCNP_Cell("Neutron Shield Plug", neutronShieldMaterial, 1);
        shieldPlug.addSurface(shieldPlugFrontFace        , MCNP_Volume.Orientation.POSITIVE);
        shieldPlug.addSurface(secondBlastShieldFrontFace , MCNP_Volume.Orientation.NEGATIVE);
        shieldPlug.addSurface(shieldPlugOuterSurface    , MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(shieldPlug);


        MCNP_Cell secondBlastShield = new MCNP_Cell("2nd Detector Blast Shield Plate", tubeMaterial, 1);
        secondBlastShield.addSurface(secondBlastShieldFrontFace , MCNP_Volume.Orientation.POSITIVE);
        secondBlastShield.addSurface(chFoilFrontFace       , MCNP_Volume.Orientation.NEGATIVE);
        secondBlastShield.addSurface(chFoilOuterSurface        , MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(secondBlastShield);


        MCNP_Cell chFoil = new MCNP_Cell("Conversion Foil", conversionMaterial, 1);
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

        MCNP_Cell thirdBlastShield = new MCNP_Cell("3rd Detector Blast Shield Plate", tubeMaterial, 1);
        thirdBlastShield.addSurface(thirdBlastShieldFrontFace , MCNP_Volume.Orientation.POSITIVE);
        thirdBlastShield.addSurface(thirdBlastShieldBackFace       , MCNP_Volume.Orientation.NEGATIVE);
        thirdBlastShield.addSurface(chFoilOuterSurface        , MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(thirdBlastShield);

        MCNP_Cell tube = new MCNP_Cell("Containing Tube", tubeMaterial, 1);
        tube.addSurface(tubeOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        tube.addSurface(chFoilOuterSurface, MCNP_Volume.Orientation.POSITIVE);
        tube.addSurface(firstBlastShieldFrontFace, MCNP_Volume.Orientation.POSITIVE);
        tube.addSurface(thirdBlastShieldBackFace, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(tube);


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

        MCNP_Tally neutronsBeforeShieldingTally = new MCNP_Tally("Neutron Distribution Before Shielding",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.neutron());
        neutronsBeforeShieldingTally.addTallyLocation(firstBlastShieldFrontFace);

        Double energyBin = 0.0;
        while(energyBin <= this.maxEnergyBound){
            protonSignalTally.addEnergyBin(energyBin);
            neutronBackgroundTally.addEnergyBin(energyBin);
            neutronsBeforeShieldingTally.addEnergyBin(energyBin);
            energyBin += this.energyBinWidth;
        }


        this.addTally(protonSignalTally);
        this.addTally(neutronBackgroundTally);
        this.addTally(neutronsBeforeShieldingTally);


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
