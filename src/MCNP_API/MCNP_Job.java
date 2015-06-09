package MCNP_API;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Brandon Lahmann on 6/7/2015.
 * TODO: Generalize this to work with different versions of MCNP
 * TODO: Remove hard coding specific to our number cruncher. Force the user to point at MCNP executable (Static field?)
 */
public class MCNP_Job extends MCNP_Object {

    private static final File runningDir = new File("runningDir");
    private static final String tempName = "tempFile";

    private String name;
    private File inputFile, outputFile, runFile, logFile;
    private MCNP_Deck deck;

    public MCNP_Job(String name, MCNP_Deck deck){
        this.name = name;
        this.deck = deck;

        String tempFilename = runningDir.getName() + "/tempFile";

        inputFile = new File(tempFilename + ".input");
        outputFile = new File(tempFilename + ".output");
        runFile = new File(tempFilename + ".run");
        logFile = new File(tempFilename + ".log");
    }

    public void runMPIJob(Integer nodes){
        try {
            String s;

            runningDir.mkdir();

            inputFile.createNewFile();
            FileWriter writer = new FileWriter(inputFile);
            writer.write(deck.toString());
            writer.close();

            logFile.createNewFile();
            writer = new FileWriter(logFile);

            String command = "mpirun ";
            command += "-np " + nodes.toString() + " ";
            command += "mcnpxMpi ";
            command += "i= " + inputFile.getPath() + " ";
            command += "o= " + outputFile.getPath() + " ";
            command += "run= " + runFile.getPath() + " ";

            Process p = Runtime.getRuntime().exec(command);
            writer.write(command + "\n");

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null) {
                writer.write(s + '\n');
            }

            while ((s = stdError.readLine()) != null) {
                writer.write(s + '\n');
            }

            writer.close();

            String finalFilename = name + "/" + name + "_" + System.currentTimeMillis();
            new File(name).mkdir();

            inputFile.renameTo(new File(finalFilename + ".input"));
            outputFile.renameTo(new File(finalFilename + ".output"));
            runFile.renameTo(new File(finalFilename + ".run"));
            logFile.renameTo(new File(finalFilename + ".log"));

        }
        catch(IOException e){
            e.printStackTrace();
            return;
        }
    }
}
