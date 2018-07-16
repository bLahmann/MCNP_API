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

    private boolean reflective = false;

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

    public static MCNP_Surface box(String name, Vec3d corner, Vec3d sideVector1, Vec3d sideVector2, Vec3d sideVector3){
        MCNP_Surface box = new MCNP_Surface(name, "BOX");

        // Corner Vector
        box.addParameter(corner.x);
        box.addParameter(corner.y);
        box.addParameter(corner.z);

        // Side Vector 1
        box.addParameter(sideVector1.x);
        box.addParameter(sideVector1.y);
        box.addParameter(sideVector1.z);

        // Side Vector 2
        box.addParameter(sideVector2.x);
        box.addParameter(sideVector2.y);
        box.addParameter(sideVector2.z);

        // Side Vector 3
        box.addParameter(sideVector3.x);
        box.addParameter(sideVector3.y);
        box.addParameter(sideVector3.z);

        return box;
    }

    public static MCNP_Surface canAboutZAxis(String name, Vec3d baseCenter, double height, double radius){
        return MCNP_Surface.can(name, baseCenter, new Vec3d(0.0, 0.0, height), radius);
    }

    public static MCNP_Surface can(String name, Vec3d baseCenter, Vec3d heightVector, double radius){
        MCNP_Surface can = new MCNP_Surface(name, "RCC");

        // Base Vector
        can.addParameter(baseCenter.x);
        can.addParameter(baseCenter.y);
        can.addParameter(baseCenter.z);

        // Height Vector
        can.addParameter(heightVector.x);
        can.addParameter(heightVector.y);
        can.addParameter(heightVector.z);

        // Radius
        can.addParameter(radius);

        return can;
    }

    public static MCNP_Surface truncatedConeAboutZ(String name, Vec3d baseCenter, double height, double lowerRadius, double upperRadius){
        return MCNP_Surface.truncatedCone(name, baseCenter, new Vec3d(0.0, 0.0, height), lowerRadius, upperRadius);
    }

    public static MCNP_Surface truncatedCone(String name, Vec3d baseCenter, Vec3d heightVector, double lowerRadius, double upperRadius){
        MCNP_Surface truncatedCone = new MCNP_Surface(name, "TRC");

        // Base Vector
        truncatedCone.addParameter(baseCenter.x);
        truncatedCone.addParameter(baseCenter.y);
        truncatedCone.addParameter(baseCenter.z);

        // Height Vector
        truncatedCone.addParameter(heightVector.x);
        truncatedCone.addParameter(heightVector.y);
        truncatedCone.addParameter(heightVector.z);

        // Radius
        truncatedCone.addParameter(lowerRadius);
        truncatedCone.addParameter(upperRadius);

        return truncatedCone;
    }

    public static MCNP_Surface wedge(String name, Vec3d corner, Vec3d sideVector1, Vec3d sideVector2, Vec3d sideVector3){
        MCNP_Surface wedge = new MCNP_Surface(name, "WED");

        // Corner Vector
        wedge.addParameter(corner.x);
        wedge.addParameter(corner.y);
        wedge.addParameter(corner.z);

        // Side Vector 1
        wedge.addParameter(sideVector1.x);
        wedge.addParameter(sideVector1.y);
        wedge.addParameter(sideVector1.z);

        // Side Vector 2
        wedge.addParameter(sideVector2.x);
        wedge.addParameter(sideVector2.y);
        wedge.addParameter(sideVector2.z);

        // Side Vector 3
        wedge.addParameter(sideVector3.x);
        wedge.addParameter(sideVector3.y);
        wedge.addParameter(sideVector3.z);

        return wedge;
    }



    /**
     * Default constructors
     */

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

    public void setReflective(boolean reflective) {
        this.reflective = reflective;
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

        if (reflective){
            currentLine += "*";
        }

        currentLine += id.toString() + " " + type + " ";

        for(Double parameter : parameters){
            String s = String.format("%+.8e ", parameter);

            if(currentLine.length() + s.length() > 78){
                lines.add(MCNP_API_Utilities.formatCardEnd(currentLine) + "&");
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
