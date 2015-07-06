package MCNP_API;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Brandon Lahmann on 6/7/2015.
 * TODO: Add an exception for invalid particle type combos
 * TODO: Tallies should really only accept cells and surfaces, not all objects
 * TODO: Add a method for specifying linear and log ranges for bins
 */
public class MCNP_Tally extends MCNP_Object {

    public static Integer totalTallies = 0;

    public enum TallyType{
        SURFACE_INTEGRATED_CURRENT,     // 1
        SURFACE_AVERAGED_FLUX,          // 2
        CELL_AVERAGED_FLUX,             // 4
        CELL_AVERAGED_DOSE,             // 6
        AVERAGED_COLLISION_HEATING,     //+6
        CELL_AVERAGED_FISSION_ENERGY    // 7
    }

    private String name;
    private Integer id;
    private MCNP_Particle particle;
    private TallyType tallyType;
    private Double multiplier = 1.0;

    private Vector<MCNP_Object> tallyLocations;
    private Vector<Double> energyBins;
    private Vector<Double> timeBins;

    public MCNP_Tally(String name, TallyType tallyType, MCNP_Particle particle){
        this.totalTallies++;

        this.name = name;
        this.id = totalTallies;
        this.tallyType = tallyType;
        this.particle = particle;

        tallyLocations = new Vector<MCNP_Object>();
        energyBins = new Vector<Double>();
        timeBins = new Vector<Double>();
    }

    public MCNP_Tally(String name, TallyType tallyType){
        this(name, tallyType, null);
    }

    public MCNP_Tally(MCNP_Particle particle, TallyType tallyType){
        this("Unnamed Tally", tallyType, particle);
    }

    public MCNP_Tally(TallyType tallyType){
        this("Unnamed Tally", tallyType, null);
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public void addTallyLocation(MCNP_Object tallyLocation){
        this.tallyLocations.add(tallyLocation);
    }

    public void addEnergyBin(Double energyBin){
        this.energyBins.add(energyBin);
    }

    public void addTimeBin(Double timeBin){
        this.timeBins.add(timeBin);
    }

    public void setEnergyBins(Vector<Double> energyBins){
        this.energyBins = energyBins;
    }

    public void setTimeBins(Vector<Double> timeBins){
        this.timeBins = timeBins;
    }

    public String toString(){
        Vector<String> lines = new Vector<String>();
        String currentLine = new String();

        Integer tallyId = 10*this.id;

        switch (tallyType){
            case SURFACE_INTEGRATED_CURRENT:
                tallyId += 1;
                currentLine += "F" + tallyId.toString()
                        + ":" + particle.getId() + " ";
                break;
            case SURFACE_AVERAGED_FLUX:
                tallyId += 2;
                currentLine += "F" + tallyId.toString()
                        + ":" + particle.getId() + " ";
                break;
            case CELL_AVERAGED_FLUX:
                tallyId += 4;
                currentLine += "F" + tallyId.toString()
                        + ":" + particle.getId() + " ";
                break;
            case CELL_AVERAGED_DOSE:
                tallyId += 6;
                currentLine += "F" + tallyId.toString()
                        + ":" + particle.getId() + " ";
                break;
            case AVERAGED_COLLISION_HEATING:
                tallyId += 6;
                currentLine += "+F" + tallyId.toString() + " ";
                break;
            case CELL_AVERAGED_FISSION_ENERGY:
                tallyId += 7;
                currentLine += "F" + tallyId.toString()
                        + ":" + particle.getId() + " ";
                break;
        }

        for(MCNP_Object tallyLocation : tallyLocations){
            String s = new String();
            if(tallyLocation.getClass().equals(MCNP_Cell.class)){
                MCNP_Cell cell = (MCNP_Cell) tallyLocation;
                s = cell.getID().toString() + " ";
            }else if(tallyLocation.getClass().equals(MCNP_Surface.class)){
                MCNP_Surface surface = (MCNP_Surface) tallyLocation;
                s = surface.getID().toString() + " ";
            }

            if(currentLine.length() + s.length() > 78){
                lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                currentLine = "        ";
            }

            currentLine += s;
        }
        lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name));

        // Multiplier line
        lines.add("FM" + tallyId.toString() + " " + String.format("%+.4e ", multiplier));

        if(!energyBins.isEmpty()){
            currentLine = "E" + tallyId + " ";

            for(Double bin : energyBins){
                String s = String.format("%+.4e ", bin);
                if(currentLine.length() + s.length() > 78){
                    lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                    currentLine = "        ";
                }

                currentLine += s;
            }
            lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name + " - Energy Bins"));
        }

        if(!timeBins.isEmpty()){
            currentLine = "T" + tallyId + " ";

            for(Double bin : timeBins){
                String s = String.format("%+.4e ", bin);
                if(currentLine.length() + s.length() > 78){
                    lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                    currentLine = "        ";
                }

                currentLine += s;
            }
            lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name + " - Time Bins"));
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
