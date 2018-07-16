package MCNP_API;

import java.util.ArrayList;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_Cell extends MCNP_Object {

    public static Integer totalCells = 0;

    private String name;
    private Integer id;
    private MCNP_Material material;
    private MCNP_SurfaceCollection surfaces;

    private Integer importance;
    private Double forcedCollisions = 0.0;

    /**
     * Predefined cells
     */

    public static MCNP_Cell canAboutZ(String name, MCNP_Material material, Integer importance,
                                      double diameter, double height, Vec3d baseCenter){

        // Define the surfaces
        MCNP_Surface outerBounds = MCNP_Surface.canAboutZAxis("Outer wall bounds of " + name,
                baseCenter, height, diameter/2.0);

        // Build the cell
        MCNP_Cell can = new MCNP_Cell(name, material, importance);
        can.addSurface(outerBounds, MCNP_Volume.Orientation.NEGATIVE);

        return can;
    }

    public static MCNP_Cell hollowCanAboutZ(String name, MCNP_Material material, Integer importance,
                                            double outerDiameter, double innerDiameter, double height, Vec3d baseCenter){

        // Define the surfaces
        MCNP_Surface outerBounds = MCNP_Surface.canAboutZAxis("Outer wall bounds of " + name,
                baseCenter, height, outerDiameter/2.0);

        MCNP_Surface innerBounds = MCNP_Surface.canAboutZAxis("Inner wall bounds of " + name,
                baseCenter, height, innerDiameter/2.0);


        // Build the cell
        MCNP_Cell hollowCan = new MCNP_Cell(name, material, importance);
        hollowCan.addSurface(outerBounds, MCNP_Volume.Orientation.NEGATIVE);
        hollowCan.addSurface(innerBounds, MCNP_Volume.Orientation.POSITIVE);

        return hollowCan;
    }

    public static MCNP_Cell hollowTruncatedConeAboutZ(String name, MCNP_Material material, Integer importance,
                                                double lowerOuterDiameter, double upperOuterDiameter, double innerDiameter, double height, Vec3d baseCenter){

        // Define the surface
        MCNP_Surface outerBounds = MCNP_Surface.truncatedConeAboutZ("Outer Bounds of " + name,
                baseCenter, height, lowerOuterDiameter/2.0, upperOuterDiameter/2.0);

        MCNP_Surface innerBounds = MCNP_Surface.canAboutZAxis("Inner Bounds of " + name,
                baseCenter, height, innerDiameter/2.0);

        // Define the cell
        MCNP_Cell truncatedCone = new MCNP_Cell(name, material, importance);
        truncatedCone.addSurface(outerBounds, MCNP_Volume.Orientation.NEGATIVE);
        truncatedCone.addSurface(innerBounds, MCNP_Volume.Orientation.POSITIVE);

        return truncatedCone;
    }

    public static MCNP_Cell box(String name, MCNP_Material material, Integer importance, Vec3d[] corners){
        Vec3d sideVector1 = new Vec3d(corners[1]);
        sideVector1.sub(corners[0]);
        Vec3d sideVector2 = new Vec3d(corners[2]);
        sideVector2.sub(corners[0]);
        Vec3d sideVector3 = new Vec3d(corners[3]);
        sideVector3.sub(corners[0]);

        MCNP_Surface boxBounds = MCNP_Surface.box(name + " Outer Bounds",
                corners[0], sideVector1, sideVector2, sideVector3);

        MCNP_Cell box = new MCNP_Cell(name, material, importance);
        box.addSurface(boxBounds, MCNP_Volume.Orientation.NEGATIVE);

        return box;
    }



    /**
     * Constructors
     */

    public MCNP_Cell(String name, MCNP_Material material, Integer importance){
        this.totalCells++;

        this.name = name;
        this.id = totalCells;
        this.material = material;
        this.importance = importance;
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

    public void setSurfaces(MCNP_SurfaceCollection surfaces) {
        this.surfaces = surfaces;
    }

    public ArrayList<MCNP_Surface> getSurfaces(){
        return surfaces.getSurfaces();
    }

    public void addSurface(MCNP_Surface surface, MCNP_Volume.Orientation orientation){
        if (surfaces == null){
            surfaces = new MCNP_SurfaceCollection(false);
        }

        surfaces.addSurface(surface, orientation);
    }

    public void setForcedCollisions(Double forcedCollisions){
        this.forcedCollisions = forcedCollisions;
    }

    protected Integer getID(){
        return this.id;
    }

    protected MCNP_Material getMaterial(){
        return this.material;
    }

    protected Integer getImportance(){
        return this.importance;
    }

    protected Double getForcedCollisions() {
        return forcedCollisions;
    }

    public String toString(){
        ArrayList<String> lines = new ArrayList<>();
        String currentLine = "";

        currentLine += id.toString() + " ";
        if(this.material == null){
            currentLine += "0 ";
        }else{
            currentLine += this.material.getID().toString() + " ";
            currentLine += String.format("%+.4e ", this.material.getDensity());
        }

        lines.add(MCNP_API_Utilities.formatCardEnd(currentLine) + "&");
        currentLine = "    ";

        String[] surfaceStrings = surfaces.toString(false).split(" ");
        for (String surfaceString : surfaceStrings){
            if (currentLine.length() + surfaceString.length() > 78){
                lines.add(MCNP_API_Utilities.formatCardEnd(currentLine) + "&");
                currentLine = "    ";
            }
            currentLine += surfaceString + " ";
        }
        lines.add(MCNP_API_Utilities.formatCardEnd(currentLine, this.name));

        String formattedString = "";
        Boolean first = true;
        for (String line : lines){
            if (!first) formattedString += "\n";
            else        first = false;
            formattedString += line;
        }

        return formattedString;
    }

}
