package apiDecks;

import MCNP_API.*;

import java.io.File;
import java.util.*;

/**
 * Created by lahmann on 2016-11-22.
 */
public class ShieldedNeutronRecoilSpectrometer extends MCNP_Deck{

    // Housing Dimensions
    private boolean housingModeled              = true;
    private MCNP_Material housingMaterial       = Material_Library.aluminum("70c");
    private Double housingOuterThickness        = 0.5 * 2.54;
    private Double housingMidPlateThickness     = 100* 1e-4;
    private Double housingSourceDistance        = 35.0;
    private Double housingDetectorStandoff      = 10.0;
    private Double housingFoilStandoff          = 1.0;
    private Double housingMisalign_z            = 0.0;
    private Double housingMisalign_x            = 0.0;


    // External Shield
    private boolean outerShieldModeled          = true;
    private MCNP_Material outerShieldMaterial   = Material_Library.ch2("70c");
    private Double outerShieldThickness         = 6.0;


    // Neutron Shielding
    private boolean shieldingModeled            = true;
    private MCNP_Material shieldingMaterial     = Material_Library.ch2("70c");
    private Double shieldingOuterDiameter       = 5.0;
    private Double shieldingLength              = 50.0;
    private Double shieldMisalign               = 0.0;


    // Conversion Foil
    private boolean foilModeled                 = true;
    private MCNP_Material foilMaterial          = Material_Library.ch2("70c");
    private Double foilInnerDiameter            = 0.1;
    private Double foilOuterDiameter            = 9.0;
    private Double foilThickness                = 20.0 * 1e-4;
    private Double foilMisalign                 = 0.0;


    // Detector
    private boolean detectorModeled             = true;
    private MCNP_Material detectorMaterial      = Material_Library.cr39("70c");
    private Double detectorDiameter             = 5.0;
    private Double detectorThickness            = 1500 * 1e-4;
    private Double detectorDistance             = 15.0;
    private double detectorMisalign             = 0.0;


    // Liner
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

    // Line of Sight Hole
    private boolean clearLoS = true;
    private double  losDiameterAtDetector       = 10.0;


    // Tally Parameters
    private Double maxEnergyBound = 3.0;
    private Double energyBinWidth = 0.025;


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

    private final static int numSimulatedNeutrons = (int) 2e8;


    public static void main(String ... args) throws Exception {


        double[] parameters = new double[] {6.0};
        for (double parameter : parameters) {
            simulateSignalToBackground_ExternalShielding("DEBUG_", parameter);

        }

    }

    public static void temp() throws Exception {

        File[] linerFiles = new File("/storage/lahmann/MCNP_API/jobFiles/PublicationValues_ExternalShield_Liner").listFiles();
        File[] noLinerFiles = new File("/storage/lahmann/MCNP_API/jobFiles/PublicationValues_ExternalShield_NoLiner").listFiles();

        Arrays.sort(linerFiles);
        Arrays.sort(noLinerFiles);

        for (int j = 0; j < linerFiles.length; j++) {

            if (linerFiles[j].getName().contains(".output")){

                ArrayList<Double[][]> linerTallies = parseOutput(linerFiles[j]);
                ArrayList<Double[][]> nonLinerTallies = parseOutput(noLinerFiles[j]);

                double[] data = calculateSignalToBackground(linerTallies.get(0), linerTallies.get(1), nonLinerTallies.get(1), 5e-5);
                for (int i = 0; i < data.length; i++) {
                    System.out.printf(", %.4e", data[i]);
                }
                System.out.println();

            }

        }
    }

    public static void simulateSignalToBackground_ExternalShielding(String name, double parameter) throws Exception{


        ShieldedNeutronRecoilSpectrometer spectrometer = new ShieldedNeutronRecoilSpectrometer("Test");
        spectrometer.setModelFlags(true, false, false, true,
                true, false, false);
        spectrometer.setSourceByTemperature(2.0);
        spectrometer.outerShieldThickness = parameter;
        spectrometer.buildDeck();
        //System.out.println(spectrometer);

        MCNP_Job job = new MCNP_Job(String.format("%s_Liner", name), spectrometer);
        job.runMPIJob(hosts, numNodes, false);
        ArrayList<Double[][]> linerTallies = parseOutput(job.outputFile);




        spectrometer = new ShieldedNeutronRecoilSpectrometer("Test");
        spectrometer.setModelFlags(false, false, false, true,
                true, false, false);
        spectrometer.setSourceByTemperature(2.0);
        spectrometer.outerShieldThickness = parameter;
        spectrometer.buildDeck();

        job = new MCNP_Job(String.format("%s_NoLiner", name), spectrometer);
        job.runMPIJob(hosts, numNodes, true);
        ArrayList<Double[][]> nonLinerTallies = parseOutput(job.outputFile);


        double[] data = calculateSignalToBackground(linerTallies.get(0), linerTallies.get(1), nonLinerTallies.get(1), 5e-5);
        System.out.printf("%.4e", parameter);
        for (int i = 0; i < data.length; i++) {
            System.out.printf(", %.4e", data[i]);
        }
        System.out.println();

    }

    public static void simulateSignalToBackground(
            String name,
            double losDiameterAtDetector,
            boolean spacerModeled,
            boolean blastShieldModeled,
            boolean mitlDeckModeled,
            boolean housingModeled,
            boolean outerShieldModeled,
            boolean detectorModeled
    ) throws Exception{


        ShieldedNeutronRecoilSpectrometer spectrometer = new ShieldedNeutronRecoilSpectrometer("Test");
        spectrometer.setModelFlags(true, spacerModeled, blastShieldModeled, mitlDeckModeled, housingModeled, outerShieldModeled, detectorModeled);
        spectrometer.losDiameterAtDetector = losDiameterAtDetector;
        spectrometer.setSourceByTemperature(2.0);
        spectrometer.buildDeck();
        //System.out.println(spectrometer);

        MCNP_Job job = new MCNP_Job(String.format("%s_Liner", name), spectrometer);
        job.runMPIJob(hosts, numNodes, false);
        ArrayList<Double[][]> linerTallies = parseOutput(job.outputFile);




        spectrometer = new ShieldedNeutronRecoilSpectrometer("Test");
        spectrometer.setModelFlags(false, spacerModeled, blastShieldModeled, mitlDeckModeled, housingModeled, outerShieldModeled, detectorModeled);
        spectrometer.losDiameterAtDetector = losDiameterAtDetector;
        spectrometer.setSourceByTemperature(2.0);
        spectrometer.buildDeck();

        job = new MCNP_Job(String.format("%s_NoLiner", name), spectrometer);
        job.runMPIJob(hosts, numNodes, false);
        ArrayList<Double[][]> nonLinerTallies = parseOutput(job.outputFile);


        double[] data = calculateSignalToBackground(linerTallies.get(0), linerTallies.get(1), nonLinerTallies.get(1), 5e-5);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%.4e ", data[i]);
        }
        System.out.println();
    }

    public static void simulateSignalToBackground(String name, double neutronShieldLength) throws Exception{


        ShieldedNeutronRecoilSpectrometer spectrometer = new ShieldedNeutronRecoilSpectrometer("Test");
        spectrometer.setModelFlags(true, false, false, false,
                true, false, false);
        spectrometer.shieldingLength = neutronShieldLength;
        spectrometer.setSourceByTemperature(2.0);
        spectrometer.buildDeck();
        //System.out.println(spectrometer);

        MCNP_Job job = new MCNP_Job(String.format("%s_Liner", name), spectrometer);
        job.runMPIJob(hosts, numNodes, false);
        ArrayList<Double[][]> linerTallies = parseOutput(job.outputFile);




        spectrometer = new ShieldedNeutronRecoilSpectrometer("Test");
        spectrometer.setModelFlags(false, false, false, false,
                true, false, false);
        spectrometer.shieldingLength = neutronShieldLength;
        spectrometer.setSourceByTemperature(2.0);
        spectrometer.buildDeck();

        job = new MCNP_Job(String.format("%s_NoLiner", name), spectrometer);
        job.runMPIJob(hosts, numNodes, false);
        ArrayList<Double[][]> nonLinerTallies = parseOutput(job.outputFile);


        System.out.printf("%.1f ", neutronShieldLength);
        double[] data = calculateSignalToBackground(linerTallies.get(0), linerTallies.get(1), nonLinerTallies.get(1), 5e-5);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%.4e ", data[i]);
        }
        System.out.println();
    }


    public ShieldedNeutronRecoilSpectrometer(String name){
        super(name);
    }

    public void setModelFlags(
            boolean linerModeled,
            boolean spacerModeled,
            boolean blastShieldModeled,
            boolean mitlDeckModeled,
            boolean housingModeled,
            boolean outerShieldModeled,
            boolean detectorModeled
    ) {
        this.linerModeled = linerModeled;
        this.spacerModeled = spacerModeled;
        this.blastShieldModeled = blastShieldModeled;
        this.mitlDeckModeled = mitlDeckModeled;
        this.housingModeled = housingModeled;
        this.outerShieldModeled = outerShieldModeled;
        this.detectorModeled = detectorModeled;

    }

    public void buildDeck() throws Exception{

        // *************************
        // ADD PARAMETERS TO HEADER
        // *************************

        this.addParameter("", "");

        if (housingModeled) {
            this.addParameter("Housing Material", housingMaterial);
            this.addParameter("Housing Outer Thickness (cm)", housingOuterThickness);
            this.addParameter("Housing MidPlate Thickness (cm)", housingMidPlateThickness);
            this.addParameter("Housing Source Distance (cm)", housingSourceDistance);
            this.addParameter("Housing Detector Standoff (cm)", housingDetectorStandoff);
            this.addParameter("Housing Foil Standoff (cm)", housingFoilStandoff);
            this.addParameter("Housing Misalignment in z (cm)", housingMisalign_z);
            this.addParameter("Housing Misalignment in x (cm)", housingMisalign_x);
            this.addParameter("", "");
        }

        if (outerShieldModeled) {
            this.addParameter("Outer Shield Material", outerShieldMaterial);
            this.addParameter("Outer Shield Thickness (cm)", outerShieldThickness);
            this.addParameter("", "");
        }

        if (shieldingModeled) {
            this.addParameter("Shielding Material", shieldingMaterial);
            this.addParameter("Shielding Outer Diameter (cm)", shieldingOuterDiameter);
            this.addParameter("Shielding Length (cm)", shieldingLength);
            this.addParameter("Shielding Misalignment in x (cm)", shieldMisalign);
            this.addParameter("", "");
        }

        if (foilModeled) {
            this.addParameter("Foil Material", foilMaterial);
            this.addParameter("Foil Inner Diameter (cm)", foilInnerDiameter);
            this.addParameter("Foil Outer Diameter (cm)", foilOuterDiameter);
            this.addParameter("Foil Thickness (um)", 1e4 * foilThickness);
            this.addParameter("Foil Misalignment in x (cm)", foilMisalign);
            this.addParameter("", "");
        }

        if (detectorModeled) {
            this.addParameter("Detector Material", detectorMaterial);
            this.addParameter("Detector Diameter (cm)", detectorDiameter);
            this.addParameter("Detector Thickness (um)", 1e4 * detectorThickness);
            this.addParameter("Detector Distance (cm)", detectorDistance);
            this.addParameter("Detector Misalignment in x (cm)", detectorMisalign);
            this.addParameter("", "");
        }

        if (linerModeled) {
            this.addParameter("Liner Material", linerMaterial);
            this.addParameter("Liner Density (g/cc)", linerDensity);
            this.addParameter("Liner Inner Diameter (um)", 1e4 * linerInnerDiameter);
            this.addParameter("Liner Outer Diameter (um)", 1e4 * linerOuterDiameter);
            this.addParameter("Liner Height (um)", 1e4 * linerHeight);
            this.addParameter("", "");
        }

        if (spacerModeled) {
            this.addParameter("Spacer Material", spacerMaterial);
            this.addParameter("Spacer Inner Diameter (cm)", spacerInnerDiameter);
            this.addParameter("Spacer Outer Diameter (cm)", spacerOuterDiameter);
            this.addParameter("Spacer Height (cm)", spacerHeight);
            this.addParameter("", "");
        }

        if (blastShieldModeled) {
            this.addParameter("Blast Shield Material", blastShieldMaterial);
            this.addParameter("Blast Shield Inner Diameter (cm)", blastShieldInnerDiameter);
            this.addParameter("Blast Shield Outer Diameter (cm)", blastShieldOuterDiameter);
            this.addParameter("Blast Shield Height (cm)", blastShieldHeight);
            this.addParameter("", "");
        }

        if (mitlDeckModeled) {
            this.addParameter("MITL Deck Material", mitlDeckMaterial);
            this.addParameter("MITL Deck Inner Diameter (cm)", mitlDeckInnerDiameter);
            this.addParameter("MITL Deck Outer Diameter (cm)", mitlDeckOuterDiameter);
            this.addParameter("MITL Deck Thickness (cm)", mitlDeckThickness);
            this.addParameter("MITL Deck Offset (cm)", mitlDeckOffset);
            this.addParameter("", "");
        }

        if (clearLoS){
            this.addParameter("LOS Diameter at Detector Plane (cm)", losDiameterAtDetector);
        }


        for (int i = 0; i < hosts.length; i++) {
            this.addParameter("Nodes used on " + hosts[i], numNodes[i]);
        }


        // Void materials that we dont want to model
        // ------------------------------------------

        if (!linerModeled)          linerMaterial = null;
        if (!spacerModeled)         spacerMaterial = null;
        if (!blastShieldModeled)    blastShieldMaterial = null;
        if (!mitlDeckModeled)       mitlDeckMaterial = null;
        if (!housingModeled)        housingMaterial = null;
        if (!outerShieldModeled)    outerShieldMaterial = null;
        if (!shieldingModeled)      shieldingMaterial = null;
        if (!foilModeled)           foilMaterial = null;
        if (!detectorModeled)       detectorMaterial = null;


        // **************
        // SURFACE CARDS
        // **************

        // PZ Surfaces
        double totalDistance = 0.0;

        totalDistance += housingSourceDistance + housingMisalign_z;
        MCNP_Surface housingFrontPlateFrontFace = new MCNP_Surface("Housing Front Plate Front Face", "pz");
        housingFrontPlateFrontFace.addParameter(totalDistance);

        totalDistance += housingOuterThickness;
        MCNP_Surface shieldingFrontFace = new MCNP_Surface("Shielding Front Face", "pz");
        shieldingFrontFace.addParameter(totalDistance);

        totalDistance += shieldingLength;
        MCNP_Surface shieldingBackFace = new MCNP_Surface("Shielding Back Face", "pz");
        shieldingBackFace.addParameter(totalDistance);

        totalDistance += housingMidPlateThickness;
        MCNP_Surface foilFrontFace = new MCNP_Surface("Conversion Foil Front Face", "pz");
        foilFrontFace.addParameter(totalDistance);

        totalDistance += foilThickness;
        MCNP_Surface foilBackFace = new MCNP_Surface("Conversion Foil Back Face", "pz");
        foilBackFace.addParameter(totalDistance);

        totalDistance += detectorDistance;
        MCNP_Surface detectorFrontFace = new MCNP_Surface("Detector Front Face", "pz");
        detectorFrontFace.addParameter(totalDistance);

        totalDistance += detectorThickness;
        MCNP_Surface detectorBackFace = new MCNP_Surface("Detector Back Face", "pz");
        detectorBackFace.addParameter(totalDistance);

        totalDistance += housingDetectorStandoff;
        MCNP_Surface housingBackPlateFrontFace = new MCNP_Surface("Housing Back Plate Front Face", "pz");
        housingBackPlateFrontFace.addParameter(totalDistance);

        totalDistance += housingOuterThickness;
        MCNP_Surface housingBackPlateBackFace = new MCNP_Surface("Housing Back Plate Back Face", "pz");
        housingBackPlateBackFace.addParameter(totalDistance);

        totalDistance *= 10;
        MCNP_Surface outsideWorldBoundary = new MCNP_Surface("Outside World Boundary", "so");
        outsideWorldBoundary.addParameter(totalDistance);

        // PY surfaces
        MCNP_Surface blastShieldHoleTopSurface = new MCNP_Surface("Blast Shield Hole Top Surface", "py");
        blastShieldHoleTopSurface.addParameter(blastShieldHoleHeight / 2.0);

        MCNP_Surface blastShieldHoleBottomSurface = new MCNP_Surface("Blast Shield Hole Bottom Surface", "py");
        blastShieldHoleBottomSurface.addParameter(- blastShieldHoleHeight / 2.0);

        MCNP_Surface blastShieldHoleRightSurface = new MCNP_Surface("Blast Shield Hole Right Surface", "px");
        blastShieldHoleRightSurface.addParameter(blashShieldHoleWidth / 2.0);

        MCNP_Surface blastShieldHoleLeftSurface = new MCNP_Surface("Blast Shield Hole Left Surface", "px");
        blastShieldHoleLeftSurface.addParameter(- blashShieldHoleWidth / 2.0);

        // KZ Surfaces
        double run = 0.0, rise = 0.0;
        run  = housingSourceDistance + housingOuterThickness + shieldingLength + housingMidPlateThickness;
        rise = foilOuterDiameter/2.0 + housingFoilStandoff + housingOuterThickness;

        MCNP_Surface housingOuterSurface = new MCNP_Surface("Housing Outer Surface", "k/z");
        housingOuterSurface.addParameter(housingMisalign_x);        // x
        housingOuterSurface.addParameter(0.0);                      // y
        housingOuterSurface.addParameter(housingMisalign_z);                      // z
        housingOuterSurface.addParameter(Math.pow(rise/run, 2));    // t^2
        housingOuterSurface.addParameter(1.0);                      // +1

        MCNP_Surface housingInnerSurface = new MCNP_Surface("Housing Inner Surface", "k/z");
        housingInnerSurface.addParameter(housingMisalign_x);        // x
        housingInnerSurface.addParameter(0.0);                      // y
        housingInnerSurface.addParameter(housingOuterThickness *run/rise + housingMisalign_z);
        housingInnerSurface.addParameter(Math.pow(rise/run, 2));
        housingInnerSurface.addParameter(1.0);

        MCNP_Surface outerShieldOuterSurface = new MCNP_Surface("Outer Shield Outer Surface", "k/z");
        outerShieldOuterSurface.addParameter(housingMisalign_x);        // x
        outerShieldOuterSurface.addParameter(0.0);                      // y
        outerShieldOuterSurface.addParameter(- (outerShieldThickness) *run/rise + housingMisalign_z);
        outerShieldOuterSurface.addParameter(Math.pow(rise/run, 2));
        outerShieldOuterSurface.addParameter(1.0);

        run  = housingSourceDistance + housingOuterThickness + shieldingLength;
        rise = shieldingOuterDiameter / 2.0;
        MCNP_Surface shieldingOuterSurface = new MCNP_Surface("Shielding Outer Surface", "k/z");
        shieldingOuterSurface.addParameter(housingMisalign_x + shieldMisalign);        // x
        shieldingOuterSurface.addParameter(0.0);                      // y
        shieldingOuterSurface.addParameter(housingMisalign_z);
        shieldingOuterSurface.addParameter(Math.pow(rise/run, 2));
        shieldingOuterSurface.addParameter(1.0);

        run = housingSourceDistance + housingOuterThickness + shieldingLength + housingMidPlateThickness + foilThickness + detectorDistance;
        rise = losDiameterAtDetector / 2.0;
        MCNP_Surface losHole = new MCNP_Surface("LOS Hole", "kz");
        losHole.addParameter(0.0);
        losHole.addParameter(Math.pow(rise/run, 2));
        losHole.addParameter(1.0);


        // CZ Surfaces
        MCNP_Surface foilInnerSurface = new MCNP_Surface("Foil Inner Surface", "c/z");
        foilInnerSurface.addParameter(housingMisalign_x + foilMisalign);
        foilInnerSurface.addParameter(0.0);
        foilInnerSurface.addParameter(foilInnerDiameter / 2.0);

        MCNP_Surface foilOuterSurface = new MCNP_Surface("Foil Outer Surface", "c/z");
        foilOuterSurface.addParameter(housingMisalign_x + foilMisalign);
        foilOuterSurface.addParameter(0.0);
        foilOuterSurface.addParameter(foilOuterDiameter / 2.0);

        MCNP_Surface detectorOuterSurface = new MCNP_Surface("Detector Outer Surface", "c/z");
        detectorOuterSurface.addParameter(housingMisalign_x + detectorMisalign);
        detectorOuterSurface.addParameter(0.0);
        detectorOuterSurface.addParameter(detectorDiameter / 2.0);


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



        // ***********
        // CELL CARDS
        // ***********

        if (linerModeled)       linerMaterial.setDensity(-linerDensity);
        MCNP_Cell liner = new MCNP_Cell("Liner", linerMaterial, 1);
        liner.addSurface(linerOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        liner.addSurface(linerInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        liner.addSurface(linerBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        liner.addSurface(linerTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(liner);

        MCNP_Cell spacer = new MCNP_Cell("Spacer", spacerMaterial, 1);
        spacer.addSurface(spacerOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        spacer.addSurface(spacerInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        spacer.addSurface(spacerBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        spacer.addSurface(spacerTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        if (clearLoS)   spacer.addSurface(losHole, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(spacer);

        MCNP_Cell blastShield = new MCNP_Cell("Blast Shield", blastShieldMaterial, 1);
        MCNP_SurfaceCollection blastShieldSurfaces = new MCNP_SurfaceCollection(false);
        blastShieldSurfaces.addSurface(blastShieldOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        blastShieldSurfaces.addSurface(blastShieldInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        blastShieldSurfaces.addSurface(blastShieldBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        blastShieldSurfaces.addSurface(blastShieldTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        if (clearLoS) {
            MCNP_SurfaceCollection blastShieldHoleSurfaces = new MCNP_SurfaceCollection(true);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleTopSurface, MCNP_Volume.Orientation.POSITIVE);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleBottomSurface, MCNP_Volume.Orientation.NEGATIVE);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleLeftSurface, MCNP_Volume.Orientation.NEGATIVE);
            blastShieldHoleSurfaces.addSurface(blastShieldHoleRightSurface, MCNP_Volume.Orientation.POSITIVE);
            blastShieldSurfaces.addSubCollection(blastShieldHoleSurfaces);
        }
        blastShield.setSurfaces(blastShieldSurfaces);
        this.addCell(blastShield);

        MCNP_Cell mitlDeck = new MCNP_Cell("MITL Deck", mitlDeckMaterial, 1);
        mitlDeck.addSurface(mitlDeckOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        mitlDeck.addSurface(mitlDeckInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        mitlDeck.addSurface(mitlDeckBottomSurface, MCNP_Volume.Orientation.POSITIVE);
        mitlDeck.addSurface(mitlDeckTopSurface, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(mitlDeck);

        MCNP_Cell housingShell = new MCNP_Cell("Housing Shell", housingMaterial, 1);
        housingShell.addSurface(housingOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        housingShell.addSurface(housingInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        housingShell.addSurface(housingFrontPlateFrontFace, MCNP_Volume.Orientation.POSITIVE);
        housingShell.addSurface(housingBackPlateBackFace, MCNP_Volume.Orientation.NEGATIVE);
        housingShell.addSurface(mitlDeckTopSurface, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(housingShell);

        MCNP_Cell outerShield = new MCNP_Cell("Outer Shield", outerShieldMaterial, 1);
        outerShield.addSurface(outerShieldOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        outerShield.addSurface(housingOuterSurface, MCNP_Volume.Orientation.POSITIVE);
        outerShield.addSurface(housingFrontPlateFrontFace, MCNP_Volume.Orientation.POSITIVE);
        outerShield.addSurface(housingBackPlateBackFace, MCNP_Volume.Orientation.NEGATIVE);
        outerShield.addSurface(mitlDeckTopSurface, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(outerShield);

        MCNP_Cell housingFrontPlate = new MCNP_Cell("Housing Front Plate", housingMaterial, 1);
        housingFrontPlate.addSurface(housingInnerSurface, MCNP_Volume.Orientation.NEGATIVE);
        housingFrontPlate.addSurface(housingFrontPlateFrontFace, MCNP_Volume.Orientation.POSITIVE);
        housingFrontPlate.addSurface(shieldingFrontFace, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(housingFrontPlate);

        MCNP_Cell neutronShield = new MCNP_Cell("Neutron Shield", shieldingMaterial, 1);
        neutronShield.addSurface(shieldingOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        neutronShield.addSurface(shieldingFrontFace, MCNP_Volume.Orientation.POSITIVE);
        neutronShield.addSurface(shieldingBackFace, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(neutronShield);

        MCNP_Cell housingMidPlate = new MCNP_Cell("Housing Mid Plate", housingMaterial, 1);
        housingMidPlate.addSurface(housingInnerSurface, MCNP_Volume.Orientation.NEGATIVE);
        housingMidPlate.addSurface(shieldingBackFace, MCNP_Volume.Orientation.POSITIVE);
        housingMidPlate.addSurface(foilFrontFace, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(housingMidPlate);

        MCNP_Cell conversionFoil = new MCNP_Cell("Conversion Foil", foilMaterial, 1);
        conversionFoil.addSurface(foilOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        conversionFoil.addSurface(foilInnerSurface, MCNP_Volume.Orientation.POSITIVE);
        conversionFoil.addSurface(foilFrontFace, MCNP_Volume.Orientation.POSITIVE);
        conversionFoil.addSurface(foilBackFace, MCNP_Volume.Orientation.NEGATIVE);
        conversionFoil.setForcedCollisions(1.0);
        this.addCell(conversionFoil);

        MCNP_Cell detector = new MCNP_Cell("Detector", detectorMaterial, 1);
        detector.addSurface(detectorOuterSurface, MCNP_Volume.Orientation.NEGATIVE);
        detector.addSurface(detectorFrontFace, MCNP_Volume.Orientation.POSITIVE);
        detector.addSurface(detectorBackFace, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(detector);

        MCNP_Cell housingBackPlate = new MCNP_Cell("Housing Back Plate", housingMaterial, 1);
        housingBackPlate.addSurface(housingInnerSurface, MCNP_Volume.Orientation.NEGATIVE);
        housingBackPlate.addSurface(housingBackPlateFrontFace, MCNP_Volume.Orientation.POSITIVE);
        housingBackPlate.addSurface(housingBackPlateBackFace, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(housingBackPlate);


        MCNP_Cell outsideWorld = new MCNP_Cell("Outside World", null, 0);
        outsideWorld.addSurface(outsideWorldBoundary, MCNP_Volume.Orientation.POSITIVE);
        this.addCell(outsideWorld);


        // ************
        // Tally Cards
        // ************

        MCNP_Tally protonSignalTally = new MCNP_Tally("Proton Distribution at CR-39 Front Surface",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.proton());
        protonSignalTally.addTallyLocation(detectorFrontFace);

        MCNP_Tally neutronBackgroundTally = new MCNP_Tally("Neutron Distribution at CR-39 Front Surface",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.neutron());
        neutronBackgroundTally.addTallyLocation(detectorFrontFace);

        MCNP_Tally neutronSpectrumTally = new MCNP_Tally("Neutron Distribution at Foil Front Surface",
                MCNP_Tally.TallyType.SURFACE_AVERAGED_FLUX, MCNP_Particle.neutron());
        neutronSpectrumTally.addTallyLocation(housingFrontPlateFrontFace);

        MCNP_MeshTally meshTally = new MCNP_MeshTally("Neutron Flux Map",
                MCNP_MeshTally.CoordinateSystem.CARTESIAN, MCNP_Particle.neutron());
        meshTally.setStartPoint(-1.0, -20.0, 0.0);
        meshTally.setEndPoint(1.0, 20.0, 110);
        meshTally.setInts(1, 201, 201);


        Double energyBin = 0.0;
        while(energyBin <= this.maxEnergyBound){
            protonSignalTally.addEnergyBin(energyBin);
            neutronBackgroundTally.addEnergyBin(energyBin);
            neutronSpectrumTally.addEnergyBin(energyBin);
            energyBin += this.energyBinWidth;
        }


        this.addTally(protonSignalTally);
        this.addTally(neutronBackgroundTally);
        //this.addTally(neutronSpectrumTally);
        //this.addMeshTally(meshTally);

        /**
         * Options / Physics Cards
         */

        this.addParticleToSimulate(MCNP_Particle.neutron());
        this.addParticleToSimulate(MCNP_Particle.proton());



    }



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
        //neutronSource.setDirectionalDistribution(dirDistribution);

        this.setSource(neutronSource, numSimulatedNeutrons);
    }

    private void setMonoEnergeticSource(Double energy){
        this.addParameter("", "");
        this.addParameter("Neutron Source Type", "Mono-energetic");
        this.addParameter("Energy (MeV)", energy);
        this.addParameter("Number of Neutrons Simulated", numSimulatedNeutrons);

        String name = energy.toString() + " MeV Neutron Source:";

        MCNP_Source neutronSource = new MCNP_Source(name, MCNP_Particle.neutron());
        neutronSource.setEnergyDistribution(MCNP_Distribution.deltaFunction(energy));
        //neutronSource.setDirectionalDistribution(getDirectionalDistribution());

        this.setSource(neutronSource, numSimulatedNeutrons);
    }

    private MCNP_Distribution getDirectionalDistribution(){

        Double l = housingDetectorStandoff + housingOuterThickness + shieldingLength + housingOuterThickness;
        Double r = foilOuterDiameter / 2.0 + housingFoilStandoff + housingOuterThickness;

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

    public static ArrayList<Double[][]> parseOutput(File outputFile) throws Exception{

        ArrayList<Double[][]> tallies = new ArrayList<>();
        Scanner s = new Scanner(outputFile);

        while (s.hasNext()){
            String temp = s.next();

            if (temp.equals("1tally")){

                // We're at the end
                if (s.next().equals("fluctuation")) {
                    break;
                }

                // Init the array lists
                ArrayList<Double> energyNodes = new ArrayList<Double>();
                ArrayList<Double> tallyValues  = new ArrayList<Double>();
                ArrayList<Double> uncertainties = new ArrayList<Double>();

                // Loop until we get to "energy"
                while (!s.next().equals("energy")){
                }

                // Loop through the data
                while (!s.hasNext("total")) {
                    energyNodes.add(Double.parseDouble(s.next()));
                    tallyValues.add(Double.parseDouble(s.next()));

                    double unc = tallyValues.get(tallyValues.size() - 1);
                    unc *= Double.parseDouble(s.next());
                    uncertainties.add(unc);
                }

                // Convert to an array and add it to the final list
                Double[][] data = new Double[3][energyNodes.size()];
                for (int i = 0; i < energyNodes.size(); i++){
                    data[0][i] = energyNodes.get(i);
                    data[1][i] = tallyValues.get(i);
                    data[2][i] = uncertainties.get(i);
                }

                tallies.add(data);

            }
        }

        s.close();
        return tallies;

    }

    public static double[] calculateSignalToBackground(
            Double[][] neutronTally,
            Double[][] protonLinerTally,
            Double[][] protonBackgroundTally,
            double neutronEta
    ) {

        double neutronTotal = 0.0;
        for (int i = 0; i < neutronTally[0].length; i++) {

            if (neutronTally[0][i] >= 1.0) {
                double value = neutronTally[1][i];
                neutronTotal += neutronEta * value;
            }
        }

        double protonDSRTotal = 0.0;
        double protonPrimTotal = 0.0;
        for (int i = 0; i < protonLinerTally[0].length; i++) {

            if (protonLinerTally[0][i] >= 1.0 && protonLinerTally[0][i] <= 1.55) {

                double sigma = protonLinerTally[2][i];
                double value = 0;
                if (sigma > 0.5) {
                    value = 0.5 * (protonLinerTally[1][i-1] + protonLinerTally[1][i+1]);
                }else {
                    value = protonLinerTally[1][i];
                }
                protonDSRTotal += value;
            }

            else if (protonLinerTally[0][i] > 1.55) {
                protonPrimTotal += protonLinerTally[1][i];

            }
        }

        double protonBackground = 0.0;
        for (int i = 0; i < protonBackgroundTally[0].length; i++) {

            if (protonBackgroundTally[0][i] >= 1.0 && protonBackgroundTally[0][i] <= 1.55) {

                double sigma = protonBackgroundTally[2][i];
                double value = 0;
                if (sigma > 0.5) {
                    value = 0.5 * (protonBackgroundTally[1][i-1] + protonBackgroundTally[1][i+1]);
                }else {
                    value = protonBackgroundTally[1][i];
                }
                protonBackground += value;
            }
        }


        double s2b = (protonDSRTotal - protonBackground) / (protonBackground + neutronTotal);
        return new double[] {s2b, neutronTotal, protonDSRTotal, protonBackground, protonPrimTotal};




    }

}