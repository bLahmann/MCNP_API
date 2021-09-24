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
public class NeutronRecoilSpectrometer extends MCNP_Deck{

    /**
     * Detector dimensions
     */

    private Double fieldingDistance = 30.0;                 // Distance of the CH foil to TCC
    private Double separationDistance = 25.0;               // Distance between the CH foil and detector

    private Double chFoilThickness = 10.0;           // Thickness of the CH foil
    private Double foilDiameter  = 5.0;                     // Distance between the inner and outer diameter of the foil

    private Double detectorDiameter = 5.0;                  // Diameter of the detector
    private Double detectorThickness = 1500.0;         // Thickness of the detector


    /**
     * Detector materials
     */

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


    private final static String[] hosts = {             // Computer hosts to use
            "ben-local",
            "chewie-local",
            "luke-local",
            "han-local"
    };
    private final static Integer[] numNodes = {         // Number of nodes to use (max 192)
            45,
            45,
            45,
            45
    };

    private Integer numSimulatedNeutrons = (int) 2e8;                               // Number of particles to simulate



    public static void main(String ... args) throws Exception{

        System.out.printf("Sleeping ... ");
        //Thread.sleep(3 * 3600 * 1000);
        System.out.println(" Done!");

        double[] thicknesses = new double[] {45.0, 50.0};
        double[] distances   = new double[] {0.001, 2.5, 5.0, 7.5, 10.0, 12.5, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0};

        for (double thickness : thicknesses) {
            for (double distance : distances) {

                System.out.printf("Running t = %.1f and l = %.1f ... ", thickness, distance);

                NeutronRecoilSpectrometer spectrometer = new NeutronRecoilSpectrometer("Basic Neutron-Recoil-Spectrometer");
                spectrometer.chFoilThickness = thickness;
                spectrometer.separationDistance = distance;
                spectrometer.buildDeck();
                spectrometer.setMonoEnergeticSource(2.5);

                MCNP_Job job = new MCNP_Job("NeutronSpec_ParameterSweep3", spectrometer);
                job.runMPIJob(hosts, numNodes, false);

                System.out.println("Done!");


            }
        }



    }


    public NeutronRecoilSpectrometer(String name){
        super(name);
    }

    public void buildDeck() throws Exception{



        /**
         * Add parameters to the header file
         */

        this.addParameter("", "");

        this.addParameter("Fielding Distance (cm)", fieldingDistance);
        this.addParameter("Separation Distance (cm)", separationDistance);
        this.addParameter("", "");

        this.addParameter("CH Foil 'Diameter' (cm)", foilDiameter);
        this.addParameter("CH Foil Thickness (um)", chFoilThickness);
        this.addParameter("", "");

        this.addParameter("Detector Diameter (cm)", detectorDiameter);
        this.addParameter("Detector Thickness (um)", detectorThickness);
        this.addParameter("", "");


        for (int i = 0; i < hosts.length; i++) {
            this.addParameter("Nodes used on " + hosts[i], numNodes[i]);
        }


        /**
         * Some unit conversions
         */

        chFoilThickness *= 1e-4;
        detectorThickness *= 1e-4;


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
        chFoilOuterSurface.addParameter(foilDiameter / 2.0);

        MCNP_Surface detectorOuterSurface = new MCNP_Surface("Detector Outer Surface", "cz");
        detectorOuterSurface.addParameter(detectorDiameter / 2.0);



        /**
         * Cell cards
         */


        MCNP_Cell chFoil = new MCNP_Cell("CH Conversion Foil", conversionMaterial, 1);
        chFoil.setForcedCollisions(1.0);
        chFoil.addSurface(chFoilFrontFace, MCNP_Volume.Orientation.POSITIVE);
        chFoil.addSurface(chFoilBackFace, MCNP_Volume.Orientation.NEGATIVE);
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

    private void setMonoEnergeticSource(Double energy){
        this.addParameter("", "");
        this.addParameter("Neutron Source Type", "Mono-energetic");
        this.addParameter("Energy (MeV)", energy);
        this.addParameter("Number of Neutrons Simulated", numSimulatedNeutrons);

        String name = energy.toString() + " MeV Neutron Source:";

        MCNP_Source neutronSource = new MCNP_Source(name, MCNP_Particle.neutron());
        neutronSource.setEnergyDistribution(MCNP_Distribution.deltaFunction(energy));
        neutronSource.setDirectionalDistribution(getDirectionalDistribution());

        this.setSource(neutronSource, numSimulatedNeutrons);
    }


    private MCNP_Distribution getDirectionalDistribution(){

        Double l = fieldingDistance;
        Double r = foilDiameter / 2.0;

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

    public static double[][] parseOutput(File outputFile) throws Exception{

        ArrayList<Double> energyNodes = new ArrayList<Double>();
        ArrayList<Double> tallyValues  = new ArrayList<Double>();
        ArrayList<Double> uncertainties = new ArrayList<Double>();

        Scanner s = new Scanner(outputFile);

        while (s.hasNext()){
            String temp = s.next();

            if (temp.equals("1tally")){
                boolean isProton = false;

                while (!s.hasNext("energy")){
                    if (s.next().equals("protons"))  isProton = true;
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

        double[][] data = new double[3][energyNodes.size()];
        for (int i = 0; i < energyNodes.size(); i++){
            data[0][i] = energyNodes.get(i);
            data[1][i] = tallyValues.get(i);
            data[2][i] = uncertainties.get(i);
        }

        return data;

    }

}