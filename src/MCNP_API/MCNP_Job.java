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

    private String name;
    public File inputFile, outputFile, runFile, logFile;
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

    public void runMPIJob(Integer nodes) throws Exception{
        String command = new String();

        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            command += "mpiexec -np ";
        }else{
            command += "mpirun  -np ";
        }

        command += nodes.toString();
        command += " mcnpxMpi";

        runJob(command);
    }

    private void runJob(String command) throws Exception{

        runningDir.mkdir();

        inputFile.createNewFile();
        FileWriter writer = new FileWriter(inputFile);
        writer.write(deck.toString());
        writer.close();

        logFile.createNewFile();
        writer = new FileWriter(logFile);

        command += " ";
        command += "i= " + inputFile.getPath() + " ";
        command += "o= " + outputFile.getPath() + " ";
        command += "run= " + runFile.getPath() + " ";

        Process p = Runtime.getRuntime().exec(command);
        writer.write(command + "\n");

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        String s;
        do{
            s = stdInput.readLine();
            writer.write(s);
        }while(!s.contains("mcrun  is done"));

        writer.close();

        String finalFilename = name + "/" + name + "_" + System.currentTimeMillis();
        new File(name).mkdir();

        File tempFile = new File(finalFilename + ".input");
        while(!inputFile.renameTo(tempFile)){
        }
        inputFile = tempFile;

        tempFile = new File(finalFilename + ".output");
        while(!outputFile.renameTo(tempFile)){
        }
        outputFile = tempFile;

        while(!runFile.delete()){
        }

        tempFile = new File(finalFilename + ".log");
        while(!logFile.renameTo(new File(finalFilename + ".log"))){
        }
        logFile = tempFile;
    }
}
