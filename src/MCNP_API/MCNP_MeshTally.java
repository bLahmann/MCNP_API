package MCNP_API;

import java.util.ArrayList;
import java.util.Vector;

public class MCNP_MeshTally extends MCNP_Object {

    public enum CoordinateSystem{
        CARTESIAN,
        CYLINDRICAL,
        SPHERICAL,
    }

    private String name;
    private Integer id;
    private MCNP_Particle particle;
    private CoordinateSystem coordinateSystem;

    private double[] startPoint;
    private double[] endPoint;
    private int[] ints;

    public MCNP_MeshTally(String name, CoordinateSystem coordinateSystem, MCNP_Particle particle){
        MCNP_Tally.totalTallies++;

        this.name = name;
        this.id = MCNP_Tally.totalTallies;
        this.coordinateSystem = coordinateSystem;
        this.particle = particle;
    }

    public void setStartPoint(double i, double j, double k) {
        this.startPoint = new double[] {i, j, k};
    }

    public void setEndPoint(double i, double j, double k) {
        this.endPoint = new double[] {i, j, k};
    }

    public void setInts(int i, int j, int k) {
        this.ints = new int[] {i, j, k};
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        StringBuilder lineBuilder = new StringBuilder();

        // Mesh tallies must end in 4 in MCNP6
        Integer tallyId = 10*this.id + 4;

        // First line
        lineBuilder.append("FMESH").append(tallyId).append(":").append(particle.getId());

        lineBuilder.append(" GEOM=");
        switch (coordinateSystem){
            case CARTESIAN:
                lineBuilder.append("REC");
                break;
            case CYLINDRICAL:
                lineBuilder.append("CYL");
                break;
        }

        lineBuilder.append(" ORIGIN= ");
        lineBuilder.append(startPoint[0]).append(" ");
        lineBuilder.append(startPoint[1]).append(" ");
        lineBuilder.append(startPoint[2]).append(" ");
        builder.append(MCNP_API_Utilities.formatCardEnd(lineBuilder.toString(), name)).append("\n");

        // Coordinate A
        lineBuilder = new StringBuilder();
        lineBuilder.append("        IMESH= ").append(endPoint[0]);
        lineBuilder.append(" IINTS= ").append(ints[0]);
        builder.append(lineBuilder.toString()).append("\n");

        // Coordinate B
        lineBuilder = new StringBuilder();
        lineBuilder.append("        JMESH= ").append(endPoint[1]);
        lineBuilder.append(" JINTS= ").append(ints[1]);
        builder.append(lineBuilder.toString()).append("\n");

        // Coordinate C
        lineBuilder = new StringBuilder();
        lineBuilder.append("        KMESH= ").append(endPoint[2]);
        lineBuilder.append(" KINTS= ").append(ints[2]);
        builder.append(lineBuilder.toString()).append("\n");

        return builder.toString();
    }
}
