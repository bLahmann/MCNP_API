import java.util.Vector;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_Cell extends MCNP_Object {

    private Integer totalCells = 0;

    private String name;
    private Integer id;
    private MCNP_Material material;
    private Vector<Vector<MCNP_Surface>> surfaceGroups;
    private Vector<Vector<Orientation>> orientationGroups;

    public enum Orientation{
        NEGATIVE,
        POSITIVE
    }

    public MCNP_Cell(String name, MCNP_Material material){
        this.totalCells++;

        this.name = name;
        this.id = totalCells;
        this.material = material;
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

    public void addSurfaceGroup(Vector<MCNP_Surface> surfaceGroup, Vector<Orientation> orientationGroup){
        this.surfaceGroups.add(surfaceGroup);
        this.orientationGroups.add(orientationGroup);
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

        for(int i = 0; i < surfaceGroups.size(); i++){

            String s = "(";
            for(int j = 0; j < surfaceGroups.get(i).size(); j++){

                if(orientationGroups.get(i).get(j) == Orientation.NEGATIVE){
                    s += "-";
                }

                s += surfaceGroups.get(i).get(i).getID().toString();

                if(j + 1 == surfaceGroups.get(i).size()){
                    s += ")";
                }else{
                    s += " ";
                }
            }

            if(currentLine.length() + s.length() > 78){
                lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                currentLine = "    ";
            }

            currentLine += s;
        }

        lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name));

        String finalString = new String();
        for(String line : lines){
            finalString += line + '\n';
        }

        return finalString;
    }


}
