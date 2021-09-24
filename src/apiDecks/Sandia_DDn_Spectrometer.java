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
public class Sandia_DDn_Spectrometer extends MCNP_Deck{

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
    private Double cr39HolderSeparationDistance = 25.0;     // Separation distance between CR-39 holders
    private Double cr39HolderCenterOffset = 0.3175;         // Offset of the center of the CR-39 holder
    private Double cr39HolderDiameter = 4.9276;             // Inner diameter of the CR-39 holder

    private Double chFoilThickness = 10.0 * 1e-4;           // Thickness of the CH foil

    private Double detectorDiameter = 5.0;                  // Diameter of the detector
    private Double detectorThickness = 1500 * 1e-4;         // Thickness of the detector

    private Double railingWidth  = 6.350;           // Width of the railing
    private Double railingHeight = 0.9525;          // Height of the railing

    private boolean linerModeled                = true;
    private MCNP_Material linerMaterial         = Material_Library.beryllium("70c");
    private Double linerDensity                 = 560.0;
    private Double linerInnerDiameter           = 116.0 * 1e-4;
    private Double linerOuterDiameter           = 139.2 * 1e-4;
    private Double linerHeight                  = 5000.0 * 1e-4;


    // Spacer
    private boolean spacerModeled               = true;
    private MCNP_Material spacerMaterial        = Material_Library.titanium("70c");
    private Double spacerInnerDiameter          = 1.82 * 2.54;     // cm
    private Double spacerOuterDiameter          = 5.22 * 2.54;     // cm
    private Double spacerHeight                 = 1.33 * 2.54;     // cm


    // Blast Shield
    private boolean blastShieldModeled          = true;
    private MCNP_Material blastShieldMaterial   = Material_Library.AlSl304_SS("70c");
    private Double blastShieldInnerDiameter     = 20.75 * 2.54;     // cm
    private Double blastShieldOuterDiameter     = 22.00 * 2.54;     // cm
    private Double blastShieldHoleHeight        =  4.00 * 2.54;     // cm
    private Double blashShieldHoleWidth         =  2.00 * 2.54;     // cm
    private Double blastShieldHeight            =  8.00 * 2.54;     // cm


    // MITL Deck
    private boolean mitlDeckModeled             = true;
    private MCNP_Material mitlDeckMaterial      = Material_Library.aluminum("70c");
    private Double mitlDeckInnerDiameter        = 2* 15.063 * 2.54;       // cm
    private Double mitlDeckOuterDiameter        = 2* 58.0   * 2.54;       // cm
    private Double mitlDeckThickness            =    0.5    * 2.54;       // cm
    private Double mitlDeckOffset               =    1.23   * 2.54;       // Distance between the bottom of the blast shield and top of the mitl deck (cm)



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

    // Computation Parameters
    private final static String[] hosts = {             // Computer hosts to use
            "ben-local",
            "chewie-local",
            "luke-local",
            "han-local"
    };
    private final static Integer[] numNodes = {         // Number of nodes to use (max 192)
            9,
            10,
            10,
            9
    };

    private Integer numSimulatedNeutrons = (int) 1e9;   // Number of particles to simulate



    public static void main(String ... args) throws Exception{

        // Init
        Sandia_DDn_Spectrometer spectrometer;
        MCNP_Job job;


        // Al case
        spectrometer = new Sandia_DDn_Spectrometer(
                "Sandia DDn Spectrometer ( JJ49970-000-UNC )");
        spectrometer.setSourceByTemperature(2.0);
        spectrometer.buildDeck();
        System.out.println(spectrometer);


        job = new MCNP_Job("DDn_Spectrometer_Material_Test", spectrometer);
        job.runMPIJob(hosts, numNodes, true);

    }

    private static void scanSignalEfficiency() throws Exception{
        double[] cHThicknesses = new double[] {10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0};
        double[] separations   = new double[] {4.0, 6.0, 8.0};

        for (double chThickness : cHThicknesses){
            for (double separation : separations){

                // Build the deck
                Sandia_DDn_Spectrometer spectrometer = new Sandia_DDn_Spectrometer(
                        "Sandia DDn Spectrometer ( JJ49970-000-UNC )");
                spectrometer.chFoilThickness = chThickness * 1e-4;
                spectrometer.cr39HolderSeparationDistance = separation;
                spectrometer.buildDeck();


                System.out.printf("Starting %.1f um foil %.1f cm separation case ...\n", chThickness, separation);


                // Run the source file (liner) version
                spectrometer.setSourceByFile();
                MCNP_Job job = new MCNP_Job("DDn_Spectrometer_Clean_Liner", spectrometer);
                System.out.print("  -> Running liner case ... ");
                //job.runMPIJob(numNodes, false, hosts);
                System.out.println("Done!");


                // Run the temperature (no liner) version
                spectrometer.setSourceByTemperature(2.0);
                job = new MCNP_Job("DDn_Spectrometer_Clean_NoLiner", spectrometer);
                System.out.print("  -> Running no-liner case ... ");
                //job.runMPIJob(numNodes, false, hosts);
                System.out.println("Done!");

            }
        }

    }


    public Sandia_DDn_Spectrometer(String name){
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

        totalDistance += tubeLength;
        MCNP_Surface backShieldMountStart = new MCNP_Surface("Back Shield Mount Start", "pz");
        backShieldMountStart.addParameter(totalDistance);

        totalDistance += shieldingMountThickness;
        MCNP_Surface backShieldMountEnd = new MCNP_Surface("Back Shield Mount End", "pz");
        backShieldMountEnd.addParameter(totalDistance);

        totalDistance += shieldingFaceThickness;
        MCNP_Surface backShieldFaceEnd = new MCNP_Surface("Back Shield Face End", "pz");
        backShieldFaceEnd.addParameter(totalDistance);

        MCNP_Surface outsideWorldBoundary = new MCNP_Surface("Outside World Boundary", "so");
        outsideWorldBoundary.addParameter(2.0*totalDistance);


        totalDistance = this.fieldingDistance;
        totalDistance += shieldingFaceThickness;
        totalDistance += shieldingMountThickness;
        totalDistance += chFoilThickness;
        MCNP_Surface chFoilEnd = new MCNP_Surface("CH Foil End", "pz");
        chFoilEnd.addParameter(totalDistance);

        totalDistance += cr39HolderThickness;
        MCNP_Surface firstCR39HolderEnd = new MCNP_Surface("1st CR-39 Holder End", "pz");
        firstCR39HolderEnd.addParameter(totalDistance);

        totalDistance += cr39HolderSeparationDistance;
        MCNP_Surface secondCR39HolderEnd = new MCNP_Surface("2nd CR-39 Holder End", "pz");
        secondCR39HolderEnd.addParameter(totalDistance);

        totalDistance -= cr39HolderThickness;
        MCNP_Surface secondCR39HolderStart = new MCNP_Surface("2nd CR-39 Holder Start", "pz");
        secondCR39HolderStart.addParameter(totalDistance);

        totalDistance -= detectorThickness;
        MCNP_Surface detectorStart = new MCNP_Surface("Detector Front Surface", "pz");
        detectorStart.addParameter(totalDistance);



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

        MCNP_Surface detectorOuterBound = new MCNP_Surface("Detector Outer Bound", "c/z");
        detectorOuterBound.addParameter(0.0);
        detectorOuterBound.addParameter(cr39HolderCenterOffset);
        detectorOuterBound.addParameter(detectorDiameter / 2.0);

        MCNP_Surface innerTubeSurface = new MCNP_Surface("Inner Tube Surface", "cz");
        innerTubeSurface.addParameter(tubeInnerDiameter / 2.0);

        MCNP_Surface outerTubeSurface = new MCNP_Surface("Outer Tube Surface", "cz");
        outerTubeSurface.addParameter(tubeOuterDiameter / 2.0);



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


        // PY Surfaces
        MCNP_Surface linerTopSurface = new MCNP_Surface("Liner Top Surface", "py");
        linerTopSurface.addParameter(linerHeight / 2.0);

        MCNP_Surface linerBottomSurface = new MCNP_Surface("Liner Bottom Surface", "py");
        linerBottomSurface.addParameter(-linerHeight / 2.0);

        MCNP_Surface spacerTopSurface = new MCNP_Surface("Spacer Top Surface", "py");
        spacerTopSurface.addParameter(spacerHeight / 2.0);

        MCNP_Surface spacerBottomSurface = new MCNP_Surface("Spacer Bottom Surface", "py");
        spacerBottomSurface.addParameter(-spacerHeight / 2.0);

        MCNP_Surface blastShieldTopSurface = new MCNP_Surface("Blast Shield Top Surface", "py");
        blastShieldTopSurface.addParameter(blastShieldHeight / 2.0);

        MCNP_Surface blastShieldBottomSurface = new MCNP_Surface("Blast Shield Bottom Surface", "py");
        blastShieldBottomSurface.addParameter(-blastShieldHeight / 2.0);

        MCNP_Surface mitlDeckTopSurface = new MCNP_Surface("MITL Deck Top Surface", "py");
        mitlDeckTopSurface.addParameter(mitlDeckThickness / 2.0 - mitlDeckOffset - blastShieldHeight / 2.0);

        MCNP_Surface mitlDeckBottomSurface = new MCNP_Surface("MITL Deck Bottom Surface", "py");
        mitlDeckBottomSurface.addParameter(-mitlDeckThickness / 2.0 - mitlDeckOffset - blastShieldHeight / 2.0);

        MCNP_Surface blastShieldHoleTopSurface = new MCNP_Surface("Blast Shield Hole Top Surface", "py");
        blastShieldHoleTopSurface.addParameter(blastShieldHoleHeight / 2.0);

        MCNP_Surface blastShieldHoleBottomSurface = new MCNP_Surface("Blast Shield Hole Bottom Surface", "py");
        blastShieldHoleBottomSurface.addParameter(- blastShieldHoleHeight / 2.0);

        MCNP_Surface blastShieldHoleRightSurface = new MCNP_Surface("Blast Shield Hole Right Surface", "px");
        blastShieldHoleRightSurface.addParameter(blashShieldHoleWidth / 2.0);

        MCNP_Surface blastShieldHoleLeftSurface = new MCNP_Surface("Blast Shield Hole Left Surface", "px");
        blastShieldHoleLeftSurface.addParameter(- blashShieldHoleWidth / 2.0);

        // CY Surfaces
        MCNP_Surface linerInnerSurface = new MCNP_Surface("Liner Inner Surface", "cy");
        linerInnerSurface.addParameter(linerInnerDiameter / 2.0);

        MCNP_Surface linerOuterSurface = new MCNP_Surface("Liner Outer Surface", "cy");
        linerOuterSurface.addParameter(linerOuterDiameter / 2.0);

        MCNP_Surface spacerInnerSurface = new MCNP_Surface("Spacer Inner Surface", "cy");
        spacerInnerSurface.addParameter(spacerInnerDiameter / 2.0);

        MCNP_Surface spacerOuterSurface = new MCNP_Surface("Spacer Outer Surface", "cy");
        spacerOuterSurface.addParameter(spacerOuterDiameter / 2.0);

        MCNP_Surface blastShieldInnerSurface = new MCNP_Surface("Blast Shield Inner Surface", "cy");
        blastShieldInnerSurface.addParameter(blastShieldInnerDiameter / 2.0);

        MCNP_Surface blastShieldOuterSurface = new MCNP_Surface("Blast Shield Outer Surface", "cy");
        blastShieldOuterSurface.addParameter(blastShieldOuterDiameter / 2.0);

        MCNP_Surface mitlDeckInnerSurface = new MCNP_Surface("MITL Deck Inner Surface", "cy");
        mitlDeckInnerSurface.addParameter(mitlDeckInnerDiameter / 2.0);

        MCNP_Surface mitlDeckOuterSurface = new MCNP_Surface("MITL Deck Outer Surface", "cy");
        mitlDeckOuterSurface.addParameter(mitlDeckOuterDiameter / 2.0);


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


        MCNP_Cell backShieldMount = new MCNP_Cell("Back Shield Mount", tubeMaterial, 1);
        backShieldMount.addSurface(backShieldMountStart, MCNP_Volume.Orientation.POSITIVE);
        backShieldMount.addSurface(backShieldMountEnd, MCNP_Volume.Orientation.NEGATIVE);
        backShieldMount.addSurface(leftShieldFace, MCNP_Volume.Orientation.POSITIVE);
        backShieldMount.addSurface(rightShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        backShieldMount.addSurface(bottomShieldFace, MCNP_Volume.Orientation.POSITIVE);
        backShieldMount.addSurface(topShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        backShieldMount.addSurface(innerTubeSurface, MCNP_Volume.Orientation.POSITIVE);
        backShieldMount.addSurface(shieldFaceEdgeSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(backShieldMount);


        MCNP_Cell backShieldPlate = new MCNP_Cell("Back Shield Plate", tubeMaterial, 1);
        backShieldPlate.addSurface(backShieldMountEnd, MCNP_Volume.Orientation.POSITIVE);
        backShieldPlate.addSurface(backShieldFaceEnd, MCNP_Volume.Orientation.NEGATIVE);
        backShieldPlate.addSurface(leftShieldFace, MCNP_Volume.Orientation.POSITIVE);
        backShieldPlate.addSurface(rightShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        backShieldPlate.addSurface(bottomShieldFace, MCNP_Volume.Orientation.POSITIVE);
        backShieldPlate.addSurface(topShieldFace, MCNP_Volume.Orientation.NEGATIVE);
        backShieldPlate.addSurface(shieldFaceEdgeSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(backShieldPlate);


        MCNP_Cell tube = new MCNP_Cell("Tube", tubeMaterial, 1);
        tube.addSurface(frontShieldMountEnd, MCNP_Volume.Orientation.POSITIVE);
        tube.addSurface(backShieldMountStart, MCNP_Volume.Orientation.NEGATIVE);
        tube.addSurface(outerTubeSurface, MCNP_Volume.Orientation.NEGATIVE);
        tube.addSurface(innerTubeSurface, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(tube);



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

/*
        MCNP_Cell chFoil = new MCNP_Cell("CH Conversion Foil", conversionMaterial, 1);
        chFoil.setForcedCollisions(1.0);
        chFoil.addSurface(frontShieldMountEnd, MCNP_Volume.Orientation.POSITIVE);
        chFoil.addSurface(chFoilEnd, MCNP_Volume.Orientation.NEGATIVE);
        chFoil.addSurface(innerCR39HolderSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(chFoil);
*/

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


        MCNP_Cell secondCR39Holder = new MCNP_Cell("2nd CR-39 Holder", tubeMaterial, 1);
        secondCR39Holder.addSurface(secondCR39HolderStart, MCNP_Volume.Orientation.POSITIVE);
        secondCR39Holder.addSurface(secondCR39HolderEnd, MCNP_Volume.Orientation.NEGATIVE);
        secondCR39Holder.addSurface(leftCR39HolderFace, MCNP_Volume.Orientation.POSITIVE);
        secondCR39Holder.addSurface(rightCR39HolderFace, MCNP_Volume.Orientation.NEGATIVE);
        secondCR39Holder.addSurface(bottomCR39HolderFace, MCNP_Volume.Orientation.POSITIVE);
        secondCR39Holder.addSurface(topCR39HolderFace, MCNP_Volume.Orientation.NEGATIVE);
        secondCR39Holder.addSurface(cr39HolderEdgeSurface, MCNP_Volume.Orientation.NEGATIVE);
        secondCR39Holder.addSurface(innerCR39HolderSurface, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(secondCR39Holder);

        MCNP_Cell detector = new MCNP_Cell("Detector", detectorMaterial, 1);
        detector.addSurface(detectorStart, MCNP_Volume.Orientation.POSITIVE);
        detector.addSurface(secondCR39HolderStart, MCNP_Volume.Orientation.NEGATIVE);
        detector.addSurface(detectorOuterBound, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(detector);


        MCNP_Cell outsideWorld = new MCNP_Cell("Outside World", 0);
        outsideWorld.addSurface(outsideWorldBoundary, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(outsideWorld);

        if (linerModeled)       linerMaterial.setDensity(-linerDensity);
        MCNP_Cell liner = new MCNP_Cell("Liner", null, 1);
        liner.addSurface(linerOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        liner.addSurface(linerInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        liner.addSurface(linerBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        liner.addSurface(linerTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(liner);

        MCNP_Cell spacer = new MCNP_Cell("Spacer", null, 1);
        spacer.addSurface(spacerOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        spacer.addSurface(spacerInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        spacer.addSurface(spacerBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        spacer.addSurface(spacerTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(spacer);

        MCNP_Cell blastShield = new MCNP_Cell("Blast Shield", null, 1);
        MCNP_SurfaceCollection blastShieldSurfaces = new MCNP_SurfaceCollection(false);
        blastShieldSurfaces.addSurface(blastShieldOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        blastShieldSurfaces.addSurface(blastShieldInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        blastShieldSurfaces.addSurface(blastShieldBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        blastShieldSurfaces.addSurface(blastShieldTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        if (true) {
            MCNP_SurfaceCollection blastShieldHoleSurfaces = new MCNP_SurfaceCollection(true);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleTopSurface, MCNP_Volume.Orientation.POSITIVE);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleBottomSurface, MCNP_Volume.Orientation.NEGATIVE);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleLeftSurface, MCNP_Volume.Orientation.NEGATIVE);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleRightSurface, MCNP_Volume.Orientation.POSITIVE);
            blastShieldSurfaces.addSubCollection(blastShieldHoleSurfaces);
        }
        blastShield.setSurfaces(blastShieldSurfaces);
        this.addCell(blastShield);

        MCNP_Cell mitlDeck = new MCNP_Cell("MITL Deck", null, 1);
        mitlDeck.addSurface(mitlDeckOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        mitlDeck.addSurface(mitlDeckInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        mitlDeck.addSurface(mitlDeckBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        mitlDeck.addSurface(mitlDeckTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(mitlDeck);


        /**
         * Tally cards
         */

        MCNP_Tally protonSignalTally = new MCNP_Tally("Proton Distribution at CR-39 Front Surface",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.proton());
        protonSignalTally.addTallyLocation(detectorStart);

        MCNP_Tally neutronBackgroundTally = new MCNP_Tally("Neutron Distribution at CR-39 Front Surface",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.neutron());
        neutronBackgroundTally.addTallyLocation(detectorStart);


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


        //MCNP_Distribution dirDistribution = getDirectionalDistribution();

        neutronSource.setEnergyDistribution(energyDist);
        //neutronSource.setDirectionalDistribution(dirDistribution);

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
