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

    private ArrayList<Double> coordinateA_Bins = new ArrayList<>();
    private ArrayList<Double> coordinateB_Bins = new ArrayList<>();
    private ArrayList<Double> coordinateC_Bins = new ArrayList<>();

    public MCNP_MeshTally(String name, CoordinateSystem coordinateSystem, MCNP_Particle particle){
        MCNP_Tally.totalTallies++;

        this.name = name;
        this.id = MCNP_Tally.totalTallies;
        this.coordinateSystem = coordinateSystem;
        this.particle = particle;
    }

    public void setCoordinateA_Bins(double min, double max, int N) {
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < N; i++){
            list.add(min + i* (max - min) / (N-1));
        }
        setCoordinateA_Bins(list);
    }

    public void setCoordinateB_Bins(double min, double max, int N) {
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < N; i++){
            list.add(min + i* (max - min) / (N-1));
        }
        setCoordinateB_Bins(list);
    }

    public void setCoordinateC_Bins(double min, double max, int N) {
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < N; i++){
            list.add(min + i* (max - min) / (N-1));
        }
        setCoordinateC_Bins(list);
    }

    public void setCoordinateA_Bins(ArrayList<Double> coordinateA_Bins) {
        this.coordinateA_Bins = coordinateA_Bins;
    }

    public void setCoordinateB_Bins(ArrayList<Double> coordinateB_Bins) {
        this.coordinateB_Bins = coordinateB_Bins;
    }

    public void setCoordinateC_Bins(ArrayList<Double> coordinateC_Bins) {
        this.coordinateC_Bins = coordinateC_Bins;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        StringBuilder lineBuilder = new StringBuilder();

        // Mesh tallies must end in 1 in MCNPX
        Integer tallyId = 10*this.id + 1;

        // First line
        builder.append(MCNP_API_Utilities.formatCardEnd("TMESH", name)).append("\n");

        // Second line
        lineBuilder.append("    ");
        switch (coordinateSystem){
            case CARTESIAN:
                lineBuilder.append("R");
                break;
            case CYLINDRICAL:
                lineBuilder.append("C");
                break;
            case SPHERICAL:
                lineBuilder.append("S");
                break;
        }

        lineBuilder.append("MESH").append(tallyId).append(":").append(particle.getId()).append(" FLUX");
        builder.append(lineBuilder.toString()).append("\n");


        // Coordinate A
        lineBuilder = new StringBuilder();
        lineBuilder.append("    CORA").append(tallyId);
        for (Double bin : coordinateA_Bins){

            String binString = String.format(" %.4e", bin);
            if (lineBuilder.toString().length() + binString.length() > 77){
                builder.append(lineBuilder.toString()).append("\n");
                lineBuilder = new StringBuilder("          ");
            }

            lineBuilder.append(binString);
        }
        builder.append(lineBuilder.toString()).append("\n");


        // Coordinate B
        lineBuilder = new StringBuilder();
        lineBuilder.append("    CORB").append(tallyId);
        for (Double bin : coordinateB_Bins){

            String binString = String.format(" %.4e", bin);
            if (lineBuilder.toString().length() + binString.length() > 77){
                builder.append(lineBuilder.toString()).append("\n");
                lineBuilder = new StringBuilder("          ");
            }

            lineBuilder.append(binString);
        }
        builder.append(lineBuilder.toString()).append("\n");


        // Coordinate C
        lineBuilder = new StringBuilder();
        lineBuilder.append("    CORC").append(tallyId);
        for (Double bin : coordinateC_Bins){

            String binString = String.format(" %.4e", bin);
            if (lineBuilder.toString().length() + binString.length() > 77){
                builder.append(lineBuilder.toString()).append("\n");
                lineBuilder = new StringBuilder("          ");
            }

            lineBuilder.append(binString);
        }
        builder.append(lineBuilder.toString()).append("\n");


        // Final line
        builder.append("ENDMD");

        return builder.toString();
    }
}
