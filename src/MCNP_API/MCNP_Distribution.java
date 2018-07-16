package MCNP_API;


import java.io.File;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by Brandon Lahmann on 6/6/2015.
 */
public class MCNP_Distribution extends MCNP_Object {

    public static MCNP_Distribution deltaFunction(double value){
        MCNP_Distribution deltaDist = new MCNP_Distribution();

        Vector<Double> node = new Vector<Double>();
        node.add(value);
        deltaDist.setNodes(node);
        return deltaDist;
    }

    public static MCNP_Distribution wattSpectrum(double a, double b){
        MCNP_Distribution wattSpectrum = new MCNP_Distribution("Watt Spectrum");

        Vector<Double> parameters = new Vector<Double>();
        parameters.add(-3.0);
        parameters.add(a);
        parameters.add(b);

        wattSpectrum.setProbabilities(parameters);
        return wattSpectrum;
    }

    public static Integer totalDistributions = 0;

    public enum NodeOption{
        HISTOGRAM_BOUNDS,       // H
        DISCRETE_VALUES,        // L
        EVALUATED_POINTS,       // A
        DISTRIBUTION_NUMBERS    // S
    }

    private String name;
    private Integer id;

    private Vector<Double> nodes;
    private NodeOption nodeOption;
    private Vector<Double> probabilities;
    private Vector<Double> biases;

    public MCNP_Distribution(String name){
        totalDistributions++;

        this.name = name;
        this.id = totalDistributions;
        this.nodeOption = NodeOption.HISTOGRAM_BOUNDS;

        nodes = new Vector<Double>();
        probabilities = new Vector<Double>();
        biases = new Vector<Double>();
    }

    public MCNP_Distribution(String name, String filename, NodeOption nodeOption) throws Exception{
        this(name);
        this.nodeOption = nodeOption;

        Scanner s = new Scanner(new File(filename));

        while (s.hasNextLine()){
            String line = s.nextLine();
            String[] values = line.split("\\s+");

            if(values.length > 0){
                nodes.add(Double.parseDouble(values[0]));
            }
            if(values.length > 1){
                probabilities.add(Double.parseDouble(values[1]));
            }
            if(values.length > 2) {
                biases.add(Double.parseDouble(values[2]));
            }
        }
    }

    public MCNP_Distribution(String name, String filename) throws Exception{
        this(name, filename, NodeOption.HISTOGRAM_BOUNDS);
    }

    public MCNP_Distribution(){
        this("Unnamed Distribution");
    }

    public void setName(String name){
        this.name = name;
    }

    public void setNodes(Vector<Double> nodes, NodeOption option){
        this.nodes = nodes;
        this.nodeOption = option;
    }

    public void setNodes(Vector<Double> nodes){
        setNodes(nodes, NodeOption.HISTOGRAM_BOUNDS);
    }

    protected Double getNode(int i){
        return nodes.get(i);
    }

    public void setProbabilities(Vector<Double> probabilities){
        this.probabilities = probabilities;
    }

    public void setBiases(Vector<Double> biases){
        this.biases = biases;
    }

    public Integer getID(){
        return this.id;
    }

    public Boolean isDelta(){
        return nodes.size() == 1;
    }

    public Boolean isEmpty(){
        return nodes.isEmpty() && probabilities.isEmpty() && biases.isEmpty();
    }

    public String toString(){
        Vector<String> lines = new Vector<String>();
        String currentLine = new String();

        // Nodes
        if(!nodes.isEmpty()){
            currentLine = "SI" + this.id.toString();

            switch (nodeOption){
                case HISTOGRAM_BOUNDS:
                    currentLine += " H ";
                    break;
                case DISCRETE_VALUES:
                    currentLine += " L ";
                    break;
                case EVALUATED_POINTS:
                    currentLine += " A ";
                    break;
                case DISTRIBUTION_NUMBERS:
                    currentLine += " S ";
                    break;
            }

            for(Double node : nodes){
                String s = String.format("%+.6e ", node);

                if(currentLine.length() + s.length() > 78){
                    lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                    currentLine = "      ";
                }

                currentLine += s;
            }
            lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name + " - Nodes"));
        }


        // Probabilities
        if(!probabilities.isEmpty()){
            currentLine = "SP" + this.id.toString() + "   ";

            for(Double probability : probabilities){
                String s = String.format("%+.6e ", probability);

                if(currentLine.length() + s.length() > 78){
                    lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                    currentLine = "      ";
                }

                currentLine += s;
            }
            lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name + " - Probabilities"));
        }

        // Biases
        if(!biases.isEmpty()){
            currentLine = "SB" + this.id.toString() + "   ";

            for(Double bias : biases){
                String s = String.format("%+.6e ", bias);

                if(currentLine.length() + s.length() > 78){
                    lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                    currentLine = "      ";
                }

                currentLine += s;
            }
            lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name + " - Biases"));
        }


        String finalString = new String();
        Iterator<String> iterator = lines.iterator();
        while(iterator.hasNext()){
            finalString += iterator.next();
            if(iterator.hasNext())
                finalString += "\n";
        }

        return finalString;
    }



}
