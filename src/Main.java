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

        MCNP_Surface test1 = new MCNP_Surface("test", "so", temp);

        System.out.println(test1.toString());
    }
}
