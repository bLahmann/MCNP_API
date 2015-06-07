package MCNP_API;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Brandon Lahmann on 6/7/2015.
 * TODO: Generalize this to work with different versions of MCNP
 */
public class MCNP_Job extends MCNP_Object {

    private String name;
    private File inputFile, outputFile, runFile, logFile;
    private MCNP_Deck deck;

    public MCNP_Job(String name, MCNP_Deck deck){
        this.name = name;
        this.deck = deck;

        String baseFilename = name + "/" + name + "_" + System.currentTimeMillis();

        inputFile = new File(baseFilename + ".input");
        outputFile = new File(baseFilename + ".output");
        runFile = new File(baseFilename + ".run");
        logFile = new File(baseFilename + ".log");
    }

    public void runMPIJob(Integer nodes){
        try {
            String s;

            FileWriter writer = new FileWriter(inputFile);
            writer.write(deck.toString());
            writer.close();

            writer = new FileWriter(runFile);

            String command = "mpirun ";
            command += "-np " + nodes.toString() + " ";
            command += "mcnpxMpi ";
            command += "i= " + inputFile.getName();
            command += "o= " + outputFile.getName();
            command += "run= " + runFile.getName();

            Process p = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null) {
                writer.write(s);
            }

            while ((s = stdError.readLine()) != null) {
                writer.write(s);
            }
        }
        catch(IOException e){
            e.printStackTrace();
            return;
        }
    }
}
