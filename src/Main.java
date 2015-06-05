import java.util.Vector;

public class Main {

    public static void main(String[] args) {

        Vector<Double> temp = new Vector<Double>();
        temp.add(-1.0);
        temp.add(0.0);
        temp.add(0.0);
        temp.add(0.0);
        temp.add(0.0);
        temp.add(0.0);
        temp.add(0.0);
        temp.add(0.0);
        temp.add(0.0);
        temp.add(0.0);

        MCNP_Surface test1 = new MCNP_Surface("so", temp);
        MCNP_Isotope uranium235 = new MCNP_Isotope("U-235", 92, 235, "70c");
        MCNP_Isotope uranium238 = new MCNP_Isotope("U-238", 92, 238, "70c");

        MCNP_Material uranium = new MCNP_Material("Uranium", -19.3);
        uranium.addIsotope(uranium235, -.711);
        uranium.addIsotope(uranium238, -(100-.711));

        MCNP_Cell myCell = new MCNP_Cell("My Cell", uranium);

        Vector<Vector<Double>> vectorTest = new Vector<Vector<Double>>();
        Vector<Double> doubleVector = new Vector<Double>();

        vectorTest.add(doubleVector);

        doubleVector.add(5.0);

        System.out.println(vectorTest.get(0).get(0));
    }
}
