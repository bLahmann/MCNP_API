package MCNP_API;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_Cell extends MCNP_Object {

    public static Integer totalCells = 0;

    private String name;
    private Integer id;
    private MCNP_Material material;
    private Vector<MCNP_SurfaceGroup> surfaceGroups;
    private Integer importance;
    private Integer forcedCollisions = 0;

    public MCNP_Cell(String name, MCNP_Material material, Integer importance){
        this.totalCells++;

        this.name = name;
        this.id = totalCells;
        this.material = material;
        this.importance = importance;

        this.surfaceGroups = new Vector<MCNP_SurfaceGroup>();
        this.surfaceGroups.add(new MCNP_SurfaceGroup());
    }

    public MCNP_Cell(MCNP_Material material, Integer importance){
        this("Unnamed Cell", material, importance);
    }

    public MCNP_Cell(String name, Integer importance){
        this(name, null, importance);
    }

    public MCNP_Cell(Integer importance){
        this("Unnamed Cell", null, importance);
    }

    public void addSurface(MCNP_Surface surface, Orientation orientation){
        this.surfaceGroups.lastElement().addSurface(surface, orientation);
    }

    public void startNewSurfaceGroup(){
        this.surfaceGroups.add(new MCNP_SurfaceGroup());
    }

    public void setForcedCollisions(Boolean forcedCollisions) {
        if (forcedCollisions)   this.forcedCollisions = 1;
        else                    this.forcedCollisions = 0;
    }

    protected Integer getID(){
        return this.id;
    }

    protected Vector<MCNP_SurfaceGroup> getSurfaceGroups(){
        return this.surfaceGroups;
    }

    protected MCNP_Material getMaterial(){
        return this.material;
    }

    protected Integer getImportance(){
        return this.importance;
    }

    protected Integer getForcedCollisions() {
        return forcedCollisions;
    }

    public String toString(){
        Vector<String> lines = new Vector<String>();
        String currentLine = new String();

        currentLine += id.toString() + " ";
        if(this.material == null){
            currentLine += "0 ";
        }else{
            currentLine += this.material.getID().toString() + " ";
            currentLine += String.format("%+.4e ", this.material.getDensity());
        }

        for(MCNP_SurfaceGroup surfaceGroup : this.surfaceGroups){

            if(currentLine.length() + surfaceGroup.toString().length() > 78){
                lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                currentLine = "    ";
            }

            currentLine += surfaceGroup.toString();

            if(!surfaceGroup.equals(surfaceGroups.lastElement())){
                currentLine += ":";
            }
        }

        lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name));

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
