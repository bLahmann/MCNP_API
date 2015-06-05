import java.util.Vector;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_Surface extends MCNP_Object {

    private static Integer totalSurfaces = 0;

    private String name;
    private String type;
    private Integer id;
    private Vector<Double> parameters;

    public MCNP_Surface(String name, String type, Vector<Double> parameters){
        MCNP_Surface.totalSurfaces++;

        this.name = name;
        this.type = type;
        this.id = totalSurfaces;
        this.parameters = parameters;
    }

    public MCNP_Surface(String type, Vector<Double> parameters){
        this("Unnamed Surface", type, parameters);
    }

    public MCNP_Surface(String name, String type){
        this(name, type, new Vector<Double>());
    }

    public MCNP_Surface(String type){
        this("Unnamed Surface", type, new Vector<Double>());
    }

    public void addParameter(Double parameter){
        this.parameters.add(parameter);
    }

    protected Integer getID(){
        return this.id;
    }

    public String toString(){
        Vector<String> lines = new Vector<String>();
        String currentLine = new String();

        currentLine += id.toString() + " " + type + " ";

        for(Double parameter : parameters){
            String s = String.format("%+.4e ", parameter);

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
