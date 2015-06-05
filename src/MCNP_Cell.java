import java.util.Vector;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_Cell extends MCNP_Object {

    private Integer totalCells = 0;

    private String name;
    private Integer id;
    private MCNP_Material material;
    private Vector<MCNP_SurfaceGroup> surfaceGroups;

    public MCNP_Cell(String name, MCNP_Material material){
        this.totalCells++;

        this.name = name;
        this.id = totalCells;
        this.material = material;

        this.surfaceGroups = new Vector<MCNP_SurfaceGroup>();
        this.surfaceGroups.add(new MCNP_SurfaceGroup());
    }

    public MCNP_Cell(MCNP_Material material){
        this("Unnamed Cell", material);
    }

    public MCNP_Cell(String name){
        this(name, null);
    }

    public MCNP_Cell(){
        this("Unnamed Cell", null);
    }

    public void addSurface(MCNP_Surface surface, MCNP_SurfaceGroup.Orientation orientation){
        this.surfaceGroups.lastElement().addSurface(surface, orientation);
    }

    public void startNewSurfaceGroup(){
        this.surfaceGroups.add(new MCNP_SurfaceGroup());
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
        }

        lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name));

        String finalString = new String();
        for(String line : lines){
            finalString += line + '\n';
        }

        return finalString;
    }

}
