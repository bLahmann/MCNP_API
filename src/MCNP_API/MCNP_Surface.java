package MCNP_API;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 * TODO: Add classes that extend surfaces (Planes, Spheres, etc)
 */
public class MCNP_Surface extends MCNP_Object {

    public static Integer totalSurfaces = 0;

    private String name;
    private String type;
    private Integer id;
    private Vector<Double> parameters;

    /**
     * Predefined sphere surfaces
     */
    public static MCNP_Surface sphere(Double radius){
        return sphere(0.0, 0.0, 0.0, radius);
    }

    public static MCNP_Surface sphere(String name, Double radius){
        return sphere(name, 0.0, 0.0, 0.0, radius);
    }

    public static MCNP_Surface sphere(Double x0, Double y0, Double z0, Double radius){
        return sphere("Unnamed Sphere Surface", x0, y0, z0, radius);
    }

    public static MCNP_Surface sphere(String name, Double x0, Double y0, Double z0, Double radius){
        Vector<Double> parameters = new Vector<Double>();
        parameters.add(x0);
        parameters.add(y0);
        parameters.add(z0);
        parameters.add(radius);

        return new MCNP_Surface(name, "s", parameters);
    }

    public MCNP_Surface(String name, String type, Vector<Double> parameters){
        MCNP_Surface.totalSurfaces++;

        this.name = name;
        this.type = type;
        this.id = totalSurfaces;
        this.parameters = parameters;
    }

    public MCNP_Surface(String type, Vector<Double> parameters){
        this("Unnamed " + type + " Surface", type, parameters);
    }

    public MCNP_Surface(String name, String type){
        this(name, type, new Vector<Double>());
    }

    public MCNP_Surface(String type){
        this("Unnamed " + type + " Surface", type, new Vector<Double>());
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
            String s = String.format("%+.10e ", parameter);

            if(currentLine.length() + s.length() > 78){
                lines.add(MCNP_API_Utilities.formatCardEnd(currentLine));
                currentLine = "    ";
            }

            currentLine += s;
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
