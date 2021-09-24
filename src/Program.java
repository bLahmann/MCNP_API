import MCNP_API.*;
import apiDecks.StepRangeFilter;
import cStopPow.StopPow;
import cStopPow.StopPow_SRIM;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.File;

public class Program {

    private final static Integer  numNodes = 180;                               // Number of nodes to use (max 192)
    private final static String[] hosts = {
            "ben-local",
            "luke-local",
            "han-local",
            "chewie-local"
    };


    static final double[] filterThicknesses = new double[] {10.0, 5.0, 5.0, 15.0, 15.0};
    //static final double[] filterThicknesses = new double[] {10.0, 25.0, 25.0, 75.0, 75.0};


    static final double[][] A = {
            {1, 1, 1, 1, 1},
            {1, 1, 0, 1, 1},
            {1, 0, 0, 1, 1},
            {1, 1, 1, 0, 1},
            {1, 1, 0, 0, 1},
            {1, 0, 0, 0, 1},
            {1, 1, 1, 0, 0},
            {1, 1, 0, 0, 0},
            {1, 0, 0, 0, 0}
    };


    /*
    static final double[] filterThicknesses = new double[] {10.0, 5.0, 5.0, 15.0};

    static final double[][] A = {
            {1, 1, 1, 1},
            {1, 1, 0, 1},
            {1, 0, 0, 1},
            {1, 1, 1, 0},
            {1, 1, 0, 0},
            {1, 0, 0, 0},
    };
     */



    public static void main(String ... args) throws Exception {

        System.load("/storage/lahmann/MCNP_API/lib/libcStopPow.so");

        StopPow_SRIM stopPow = new StopPow_SRIM("lib/Hydrogen in Tantalum.txt");
        stopPow.set_mode(StopPow.getMODE_LENGTH());


        double[][][] line1 = StepRangeFilter.parseOutput(new File("jobFiles/SRF_Calibration_Simulations/SRF_Calibration_Simulations_1576273627467.output"));
        double[][][] line2 = StepRangeFilter.parseOutput(new File("jobFiles/SRF_Calibration_Simulations/SRF_Calibration_Simulations_1576273868488.output"));
        double[][][] line3 = StepRangeFilter.parseOutput(new File("jobFiles/SRF_Calibration_Simulations/SRF_Calibration_Simulations_1576274037424.output"));
        double[][][] line4 = StepRangeFilter.parseOutput(new File("jobFiles/SRF_Calibration_Simulations/SRF_Calibration_Simulations_1576274203480.output"));


        //line1 = truncateData(new int[] {3, 4, 5, 6, 7, 8, 9}, line1);
        //line2 = truncateData(new int[] {3, 4, 5, 6, 7, 8, 9}, line2);
        //line3 = truncateData(new int[] {3, 4, 5, 6, 7, 8, 9}, line3);
        //line4 = truncateData(new int[] {3, 4, 5, 6, 7, 8, 9}, line4);


        int count = 0;
        double minGoodness = Double.MAX_VALUE;
        double[] bestFilters = filterThicknesses;
        while (count < 1000) {

            final int NUM_THREADS = 48;
            SRF_Calibration_Thread[] threads = new SRF_Calibration_Thread[NUM_THREADS];
            double[] goodness = new double[NUM_THREADS];
            double[][] perturbedFilters = new double[NUM_THREADS][filterThicknesses.length];
            double[][] t = new double[NUM_THREADS][A.length];


            // Make the filters
            for (int k = 0; k < threads.length; k++) {

                NormalDistribution n = new NormalDistribution(1, 0.2);
                double pert1 = n.sample();
                double pert2 = n.sample();
                double pert3 = n.sample();

                perturbedFilters[k] = new double[filterThicknesses.length];
                perturbedFilters[k][0] = filterThicknesses[0] * pert1;
                perturbedFilters[k][1] = filterThicknesses[1] * pert2;
                perturbedFilters[k][2] = filterThicknesses[2] * pert2;
                perturbedFilters[k][3] = filterThicknesses[3] * pert3;
                perturbedFilters[k][4] = filterThicknesses[4] * pert3;

                t[k] = new double[A.length];
                for (int i = 0; i < A.length; i++) {
                    for (int j = 0; j < A[i].length; j++) {
                        t[k][i] += perturbedFilters[k][j] * A[i][j];
                    }
                }
            }

            // 2.3 MeV data
            for (int k = 0; k < threads.length; k++) {
                threads[k] = new SRF_Calibration_Thread(line1, t[k], 1.1, stopPow);
                threads[k].start();
            }

            for (int k = 0; k < threads.length; k++) {
                threads[k].join();
                goodness[k] += threads[k].getResult();
            }


            // 2.5 MeV data
            for (int k = 0; k < threads.length; k++) {
                threads[k] = new SRF_Calibration_Thread(line2, t[k], 1.1, stopPow);
                threads[k].start();
            }

            for (int k = 0; k < threads.length; k++) {
                threads[k].join();
                goodness[k] += threads[k].getResult();
            }


            // 2.8 MeV data
            for (int k = 0; k < threads.length; k++) {
                threads[k] = new SRF_Calibration_Thread(line3, t[k], 1.1, stopPow);
                threads[k].start();
            }

            for (int k = 0; k < threads.length; k++) {
                threads[k].join();
                goodness[k] += threads[k].getResult();
            }


            // 3.0 MeV data
            for (int k = 0; k < threads.length; k++) {
                threads[k] = new SRF_Calibration_Thread(line4, t[k], 1.1, stopPow);
                threads[k].start();
            }

            for (int k = 0; k < threads.length; k++) {
                threads[k].join();
                goodness[k] += threads[k].getResult();
            }


            // Print data
            for (int k = 0; k < threads.length; k++) {

                if (goodness[k] < minGoodness) {
                    minGoodness = goodness[k];
                    bestFilters = perturbedFilters[k];

                    for (int i = 0; i < bestFilters.length; i++) {
                        System.out.printf("%.4f, ", bestFilters[i]);
                    }
                    System.out.printf("%.4e\n", goodness[k]);

                }

                /*
                for (int i = 0; i < perturbedFilters[0].length; i++) {
                    System.out.printf("%.4f, ", perturbedFilters[k][i]);
                }
                System.out.printf("%.4e\n", goodness[k]);
                 */
            }

            count++;
            System.out.println(count);
        }
    }

    public static File generateOutput(String jobName, double mu, double sigma) throws Exception{

        // Build Deck
        StepRangeFilter srf = new StepRangeFilter("DDp 5f9w SRF Design");
        srf.numParticles = (int) 1e7;
        srf.filterThicknesses = filterThicknesses;
        srf.sourceMeanEnergy = mu;
        srf.sourceSigma = sigma;
        srf.buildDeck();


        // Run job
        MCNP_Job job = new MCNP_Job(jobName, srf);
        //job.runMPIJob(hosts, numNodes, true);
        return job.outputFile;

    }

    public static double[][][] truncateData(int[] indexes, double[][][] data) {

        double[][][] truncatedData = new double[indexes.length][data[0].length][data[0][0].length];
        for (int i = 0; i < indexes.length; i++){
            truncatedData[i] = data[indexes[i]];
        }
        return truncatedData;

    }
}
