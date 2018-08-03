package MCNP_API;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Vector;

/**
 * Created by Brandon Lahmann on 6/7/2015.
 * TODO: Generalize this to work with different versions of MCNP
 * TODO: Remove hard coding specific to our number cruncher. Force the user to point at MCNP executable (Static field?)
 */
public class MCNP_Job extends MCNP_Object {

    private static final File storageDir = new File("jobFiles");
    private static final File runningDir = new File("runningDir");

    private String name;
    public File inputFile, outputFile, runFile, logFile, mDataFile, mcTalFile;
    public Long executionTime = new Long(-1);
    private MCNP_Deck deck;

    public MCNP_Job(String name, MCNP_Deck deck){
        this.name = name;
        this.deck = deck;

        // Make directories we'll need
        storageDir.mkdirs();
        runningDir.mkdirs();

        // Clear out the running directory to avoid name conflicts
        for (File file : runningDir.listFiles()){
            file.delete();
        }


        String tempFilename = runningDir.getName() + "/tempFile";

        inputFile = new File(tempFilename + ".input");
        outputFile = new File(tempFilename + ".output");
        runFile = new File(tempFilename + ".run");
        logFile = new File(tempFilename + ".log");
        mDataFile = new File(tempFilename + ".mdata");

    }

    public void runMPIJob(Integer nodes) throws Exception{
        String command = new String();

        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            command += "mpiexec -np ";
        }else {
            command += "mpirun -np ";
        }

        command += nodes.toString() + " ";

        command += "/MCNP/mcnpx";
        runJob(command);
    }

    public void runMPIJob(Integer nodes, String ... hosts) throws Exception{
        String command = new String();

        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            command += "mpiexec -np ";
        }else {
            command += "mpirun -np ";
        }

        command += nodes.toString() + " ";

        if (hosts.length > 0){
            command += "-display-map -H ";

            boolean first = true;
            for (String host : hosts){
                if (!first) command += ",";
                else first = false;

                command += host;
            }
            command += " ";
        }

        command += "/MCNP/mcnp6";
        runJob(command);
    }

    public void runMCNPXJob() throws Exception{
        runJob("mcnpx");
    }

    private void runJob(String command) throws Exception{

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
        command += "mdata= " + mDataFile.getPath() + " ";

        long startTime = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();

        writer.write(command + "\n");

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        String s;
        do{
            s = stdInput.readLine();

            if(s == null){
                break;
            }

            writer.write(s + '\n');
        }while(!s.contains("mcrun  is done"));

        executionTime = System.currentTimeMillis() - startTime;
        writer.close();

        String finalFilename = storageDir.getAbsolutePath() + "/" + name + "/" + name + "_" + System.currentTimeMillis();
        new File(storageDir, name).mkdir();

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

        tempFile = new File(finalFilename + ".mdata");
        while(!mDataFile.renameTo(tempFile)){
        }

        tempFile = new File(finalFilename + ".log");
        while(!logFile.renameTo(new File(finalFilename + ".log"))){
        }
        logFile = tempFile;
    }
}
