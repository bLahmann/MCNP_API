package apiDecks;

import MCNP_API.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

/**
 * MCNP_Deck that simulates the transmission of mono-energetic charged particles through
 * some material as a function of thickness
 */
public class ChargedParticleTransmissionSimulation extends MCNP_Deck{

    // Charged source particle who's transmission we're simulating
    private MCNP_Particle sourceParticle = MCNP_Particle.proton();

    // Energy of the source particles (MeV)
    private Double sourceEnergy = 0.0;

    // Number of source particles to simulate
    private Integer numSourceParticles = (int) 2e5;



    // Material that the particles will be ranged through
    private MCNP_Material filterMaterial = MCNP_Material.aluminum("");

    // Max filter thickness for this simulation (cm)
    private Double maxFilterThickness = 700.0 * 1e-4;

    // Expected range of the source particle (cm)
    // tallies are centered about here
    private Double expectedSourceRange = 0.0;

    // Distance between transmission surface tallies
    private Double filterStepSize = 1.0 * 1e-4;

    // Standoff distance of the filter(cm)
    private Double filterStandoff = 50.0;

    // Diameter of the filter region (cm)
    private Double filterDiameter = 5.0;



    // Energy bin width of the tallies (MeV)
    private Double energyBinWidth = 25.0 * 1e-3;



    // List of hosts to be used by this simulation
    private final static String[] hosts = {"han", "luke", "ben", "chewie"};

    // Number of CPUs to be used by this simulation
    private final static Integer  numNodes = 180;                              // Number of nodes to use (max 192)



    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String ... args) throws Exception{

        combineParsedFiles(new File("jobFiles/Proton_in_Titanium").listFiles(), 0.5);
        //scanSourceEnergies();

    }


    /**
     *
     * @throws Exception
     */
    private static void scanSourceEnergies() throws Exception {

        // *************
        // User settings
        // *************

        MCNP_Particle sourceParticle = MCNP_Particle.proton();
        MCNP_Material filterMaterial = Material_Library.titanium("");

        File particleRanges = new File("lib/protonRangesInTi");
        double maxThickness  = 200.0*1e-4;
        double thicknessStep = 0.5 * 1e-4;



        // ****************
        // Simulation scans
        // ****************

        Scanner s = new Scanner(particleRanges);
        while (s.hasNextDouble()){

            double sourceEnergy   = s.nextDouble();         // MeV
            double expectedRange  = s.nextDouble() *1e-4;   // um -> cm

            String simulationName = String.format("%.4f MeV %s in %s Transmission Simulation", sourceEnergy,
                    sourceParticle.getName(), filterMaterial.getName());

            ChargedParticleTransmissionSimulation simulation = new ChargedParticleTransmissionSimulation(simulationName);
            simulation.sourceParticle = sourceParticle;
            simulation.filterMaterial = filterMaterial;
            simulation.sourceEnergy = sourceEnergy;
            simulation.expectedSourceRange = expectedRange;
            simulation.maxFilterThickness = maxThickness;
            simulation.filterStepSize = thicknessStep;
            simulation.buildDeck();


            double time = System.currentTimeMillis();
            System.out.printf("Running the %.4f MeV case ... ", sourceEnergy);

            String jobName = String.format("%s_in_%s", sourceParticle.getName(), filterMaterial.getName());
            MCNP_Job job = new MCNP_Job(jobName, simulation);
            job.runMPIJob(numNodes, false, hosts);

            parseOutputFile(job.outputFile);

            System.out.printf("Done! (%.2f s)\n", 1e-3 * (System.currentTimeMillis() - time));

        }
    }



    private static void parseOutputFile(File outputFile) throws Exception{

        double sourceEnergy = 0.0;
        String material = "";

        // Make a new file
        String[] temp = outputFile.getAbsolutePath().split(".output");
        String name = temp[0] + ".parsed";
        File parsedFile = new File(name);

        // Create a file writer
        FileWriter w = new FileWriter(parsedFile);

        // Scan the file
        Scanner s = new Scanner(outputFile);
        while (s.hasNext()){

            String line = s.nextLine();

            // Grab the source energy
            if (line.contains("Source Energy (MeV)")){
                temp = line.split(":");
                sourceEnergy = Double.parseDouble(temp[1]);
            }

            // Grab the material name
            if (line.contains("Transmission Material")){
                temp = line.split(":");
                material = temp[1].trim();
            }

            // Handle the tally
            if (line.contains("1tally")){

                double thickness = 0.0;

                // Get the next line
                line = s.nextLine();

                // If it doesn't have the "+" comment we're at the end of the file
                if (!line.contains("+")){
                    break;
                }

                // Get the thickness
                temp = line.split("\\s+");
                thickness = Double.valueOf(temp[1]);

                // Get to the actual data
                while (!line.contains(" cosine bin:  -3.49148E-15 to  1.00000E+00")){
                    line = s.nextLine();
                }

                // Skip on more line
                s.nextLine();


                // Write the header to this section
                w.write(String.format("Source,%.4f,Thickness,%.2f,Material,%s\n",
                        sourceEnergy, thickness, material));
                w.write("Energy Node,Counts\n");


                // Handle the data
                line = s.nextLine();
                while (!line.contains("total")){

                    temp = line.split("\\s+");
                    w.write(temp[1] + "," + temp[2] + "\n");

                    line = s.nextLine();

                }
            }

        }

        // Close the file manipulators
        s.close();
        w.close();
    }



    private static void combineParsedFiles(File[] files, double cutoff) throws Exception {

        // Make a file writer
        FileWriter w = new FileWriter("temp.dat");

        // Loop through all of the files
        for (File file : files){

            // Skip bad files
            if (!file.getName().contains(".parsed")){
                continue;
            }

            System.out.println(file.getName());

            // Make a scanner for this file
            Scanner s = new Scanner(file);

            // Init the parameters we need to parse
            double thickness = 0.0, sourceEnergy = 0.0, transmission = 0.0;

            // Flags
            boolean pastCutoff = false;
            boolean first = true;

            // Loop through each line
            while (s.hasNextLine()){

                String line = s.nextLine();
                String[] temp = line.split(",");

                // This is the start of a tally
                if (line.contains("Source,")){

                    // If this isn't the first time, write the previous data
                    if (!first){
                        w.write(thickness + " " +
                                1000*sourceEnergy + " " +
                                1000*cutoff + " " +
                                transmission + "\n"
                        );
                    }

                    // Reset flags and transmission
                    first = false;
                    pastCutoff = false;
                    transmission = 0.0;

                    // Get the thickness and source energy
                    thickness    = Double.parseDouble(temp[3]);     // um
                    sourceEnergy = Double.parseDouble(temp[1]);     // MeV

                }

                // This is a data line. Only look at if it we're past the cutoff
                else if (pastCutoff && !line.contains("Energy")){
                    transmission += Double.valueOf(temp[1]);
                }


                // Check to see if we're past the cutoff
                if (line.contains(String.format("%.4E", cutoff))){
                    pastCutoff = true;
                }
            }

            // Write the last line of data
            w.write(thickness + " " +
                    1000*sourceEnergy + " " +
                    1000*cutoff + " " +
                    transmission + "\n"
            );

            // Close the scanner before opening a new file
            s.close();
        }

        // Close the writer
        w.close();

    }

    /**
     * Generic constructor method
     * @param name
     */
    private ChargedParticleTransmissionSimulation(String name){
        super(name);
    }


    /**
     * Method that converts the internal fields into MCNP_Objects so that the simulation can be submitted
     * @throws Exception
     */
    private void buildDeck() throws Exception{



        // *********************************
        // Add parameters to the file header
        // *********************************


        // Build a list of the hosts
        StringBuilder hostList = new StringBuilder();
        String prefix = "";
        for (String host : hosts){
            hostList.append(prefix).append(host);
            prefix = ", ";
        }


        // Computer parameters
        this.addParameter("", "");
        this.addParameter("Hosts used", hostList);
        this.addParameter("Number of CPUs used", numNodes);
        this.addParameter("Submitted by user", System.getProperty("user.name"));


        // Source parameters
        this.addParameter("", "");
        this.addParameter("Source Particle", sourceParticle.getName());
        this.addParameter("Source Energy (MeV)", sourceEnergy);
        this.addParameter("Number of particles simulated", numSourceParticles);


        // Filter parameters
        this.addParameter("", "");
        this.addParameter("Transmission Material", filterMaterial.getName());
        this.addParameter("Max Filter Thickness (um)", maxFilterThickness * 1e4);
        this.addParameter("Expected Source Range (um)", expectedSourceRange * 1e4);
        this.addParameter("Filter Step Size (um)", filterStepSize * 1e4);
        this.addParameter("Filter Standoff Distance (cm)", filterStandoff);
        this.addParameter("Filter Diameter (cm)", filterDiameter);


        // Tally parameters
        this.addParameter("", "");
        this.addParameter("Tally Energy Bin Width (keV)", energyBinWidth * 1e3);



        // ******************
        // Convert some units
        // ******************

        // Nothing needs to be done



        // ******************************
        // Build all of the SURFACE CARDS
        // ******************************

        // Make a "pz" surface for every filter step
        double[] filterThicknesses   = MCNP_API_Utilities.linspace(0.0, maxFilterThickness, filterStepSize);
        MCNP_Surface[] filterSurfaces = new MCNP_Surface[filterThicknesses.length];
        for (int i = 0; i < filterSurfaces.length; i++){
            filterSurfaces[i] = new MCNP_Surface(String.format("%.2f um Surface", filterThicknesses[i]*1e4), "pz");
            filterSurfaces[i].addParameter(filterStandoff + filterThicknesses[i]);
        }

        // Make a negative "pz" surface to fully enclose the simulation
        MCNP_Surface negativePlanarBoundary = new MCNP_Surface("Negative Planar Boundary", "pz");
        negativePlanarBoundary.addParameter(-1 * filterStandoff);


        // Make a "cz" for the outside world boundary
        MCNP_Surface outsideWorldBoundary = new MCNP_Surface("Outside World Boundary", "cz");
        outsideWorldBoundary.addParameter(filterDiameter / 2.0);
        outsideWorldBoundary.setReflective(true);



        // ***************************
        // Build all of the CELL CARDS
        // ***************************


        // Make a cell of every "pz" surface pair
        for (int i = 1; i < filterThicknesses.length; i++){
            String cellName = String.format("%.2f - %.2f um Filter Region", 1e4*filterThicknesses[i-1], 1e4*filterThicknesses[i]);
            MCNP_Cell filter = new MCNP_Cell(cellName, filterMaterial, 1);
            filter.addSurface(filterSurfaces[i-1], MCNP_Volume.Orientation.POSITIVE);
            filter.addSurface(filterSurfaces[i]  , MCNP_Volume.Orientation.NEGATIVE);
            filter.addSurface(outsideWorldBoundary, MCNP_Volume.Orientation.NEGATIVE);
            this.addCell(filter);
        }

        // Make a cell for the void region where the source sits
        MCNP_Cell voidRegion = new MCNP_Cell("Void Region", 1);
        voidRegion.addSurface(negativePlanarBoundary, MCNP_Volume.Orientation.POSITIVE);
        voidRegion.addSurface(filterSurfaces[0], MCNP_Volume.Orientation.NEGATIVE);
        voidRegion.addSurface(outsideWorldBoundary, MCNP_Volume.Orientation.NEGATIVE);
        this.addCell(voidRegion);

        // Make a cell of the outside world
        MCNP_Cell outsideWorld = new MCNP_Cell("Outside World", 0);
        outsideWorld.setSurfaces(new MCNP_SurfaceCollection(true));
        outsideWorld.addSurface(outsideWorldBoundary, MCNP_Volume.Orientation.POSITIVE);
        outsideWorld.addSurface(negativePlanarBoundary, MCNP_Volume.Orientation.NEGATIVE);
        outsideWorld.addSurface(filterSurfaces[filterSurfaces.length - 1], MCNP_Volume.Orientation.POSITIVE);
        this.addCell(outsideWorld);



        // ****************************
        // Build all of the TALLY CARDS
        // ****************************

        // Make the energy bins for the tallies
        double[] energyBins = MCNP_API_Utilities.linspace(0.0, sourceEnergy+energyBinWidth, energyBinWidth);

        // MCNP will only let us have 100 tallies, so we'll choose the surfaces about the expected range
        double minTallyThickness = expectedSourceRange - 45 * filterStepSize;
        double maxTallyThickness = expectedSourceRange + 45 * filterStepSize;

        for (int i = 0; i < filterSurfaces.length; i++){

            if (filterThicknesses[i] > minTallyThickness && filterThicknesses[i] < maxTallyThickness) {

                String tallyName = String.format("%.2f um Tally", 1e4 * filterThicknesses[i]);
                MCNP_Tally surfaceTally = new MCNP_Tally(tallyName, MCNP_Tally.TallyType.SURFACE_INTEGRATED_CURRENT, sourceParticle);
                surfaceTally.addTallyLocation(filterSurfaces[i]);

                // Energy Bins
                for (double energyBin : energyBins) {
                    surfaceTally.addEnergyBin(energyBin);
                }

                // Cosine Bins
                surfaceTally.addCosineBin(90.0);
                surfaceTally.addCosineBin(0.0);

                // Add the tally
                this.addTally(surfaceTally);

            }
        }



        // **********************
        // Build the SOURCE CARDS
        // **********************

        // Mono-energetic source
        MCNP_Distribution energyDistribution = MCNP_Distribution.deltaFunction(sourceEnergy);

        // Source directed at the filter
        MCNP_Distribution directionalDistribution = MCNP_Distribution.deltaFunction(1.0);

        // Build source
        MCNP_Source source = new MCNP_Source("Source", sourceParticle);
        source.setEnergyDistribution(energyDistribution);
        source.setDirectionalDistribution(directionalDistribution);

        // Add to the deck
        this.addParticleToSimulate(sourceParticle);
        this.setSource(source, numSourceParticles);

    }

}
