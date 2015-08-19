package MCNP_API;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Brandon Lahmann on 6/6/2015.
 * TODO: Add the various options for MCNP sources (i.e position distributions, etc)
 */
public class MCNP_Source extends MCNP_Object {

    private String name;

    private MCNP_Particle particle;

    private MCNP_Distribution energyDistribution;
    private MCNP_Distribution timeDistribution;
    private MCNP_Distribution directionalDistribution;

    private Double[] referenceVector;

    public MCNP_Source(String name, MCNP_Particle particle){
        this.name = name;
        this.particle = particle;

        referenceVector = new Double[] {0.0, 0.0, 1.0};
    }

    public void setEnergyDistribution(MCNP_Distribution distribution){
        energyDistribution = distribution;
        energyDistribution.setName(this.name + " Energy Distribution");
    }

    public void setTimeDistribution(MCNP_Distribution distribution){
        timeDistribution = distribution;
        timeDistribution.setName(this.name + " Time Distribution");
    }

    public void setDirectionalDistribution(MCNP_Distribution distribution){
        directionalDistribution = distribution;
        directionalDistribution.setName(this.name + " Directional Distribution");
    }

    public void setReferenceVector(Double x, Double y, Double z){
        referenceVector = new Double[] {x, y, z};
    }

    public String toString(){
        Vector<String> lines = new Vector<String>();
        String mainLine = "SDEF ";

        mainLine += "PAR=" + particle.getId() + " ";

        if(energyDistribution != null && !energyDistribution.isEmpty()) {
            mainLine += "ERG=";
            if(energyDistribution.isDelta()){
                mainLine += energyDistribution.getNode(0).toString() + " ";
            }else{
                mainLine += "D" + energyDistribution.getID().toString() + " ";
            }

        }

        if(timeDistribution != null && !timeDistribution.isEmpty()){
            mainLine += "TME=";
            if(timeDistribution.isDelta()){
                mainLine += timeDistribution.getNode(0).toString() + " ";
            }else{
                mainLine += "D" + timeDistribution.getID().toString() + " ";
            }
        }

        if(directionalDistribution != null && !directionalDistribution.isEmpty()){
            mainLine += "DIR=";
            if(directionalDistribution.isDelta()){
                mainLine += directionalDistribution.getNode(0).toString() + " ";
            }else{
                mainLine += "D" + directionalDistribution.getID().toString() + " ";
            }
        }

        mainLine += "VEC=" + referenceVector[0] +
                       " " + referenceVector[1] +
                       " " + referenceVector[2];

        lines.add(MCNP_API_Utilities.formatCardEnd(mainLine, this.name));

        if(energyDistribution != null && !energyDistribution.isEmpty() && !energyDistribution.isDelta())
            lines.add(energyDistribution.toString());

        if(timeDistribution != null && !timeDistribution.isEmpty() && !timeDistribution.isDelta())
            lines.add(timeDistribution.toString());

        if(directionalDistribution != null && !directionalDistribution.isEmpty() && !directionalDistribution.isDelta())
            lines.add(directionalDistribution.toString());

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
