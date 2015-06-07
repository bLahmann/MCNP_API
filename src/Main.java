import java.lang.reflect.Field;
import java.util.Vector;

public class Main {

    public static void main(String[] args) {

        Vector<Double> temp = new Vector<Double>();
        temp.add(-1.0);
        temp.add(-3.3);
        temp.add(-1.0);
        temp.add(-3.3);
        temp.add(-1.0);
        temp.add(-3.3);
        temp.add(-1.0);
        temp.add(-3.3);
        temp.add(-1.0);
        temp.add(-3.3);
        temp.add(-1.0);
        temp.add(-3.3);
        temp.add(-1.0);
        temp.add(-3.3);

        MCNP_Surface mySurface = new MCNP_Surface("so", temp);
        MCNP_Isotope uranium235 = new MCNP_Isotope("U-235", 92, 235, "70c");
        MCNP_Isotope uranium238 = new MCNP_Isotope("U-238", 92, 238, "70c");

        MCNP_Material uranium = new MCNP_Material("Uranium", -19.3);
        uranium.addIsotope(uranium235, -.711);
        uranium.addIsotope(uranium238, -(100-.711));

        MCNP_Cell myCell = new MCNP_Cell("My Cell", uranium, 1);
        myCell.addSurface(mySurface, Orientation.NEGATIVE);
        myCell.addSurface(mySurface, Orientation.NEGATIVE);
        myCell.addSurface(mySurface, Orientation.NEGATIVE);
        myCell.addSurface(mySurface, Orientation.NEGATIVE);

        MCNP_Cell mySecondCell = new MCNP_Cell("Second Cell", uranium, 2);
        mySecondCell.addSurface(mySurface, Orientation.POSITIVE);

        MCNP_Particle neutron = MCNP_Particle.neutron();

        MCNP_Distribution myDistribution = new MCNP_Distribution();
        myDistribution.setNodes(temp, MCNP_Distribution.NodeOption.EVALUATED_POINTS);
        myDistribution.setBiases(temp);
        myDistribution.setProbabilities(temp);

        MCNP_Source mySource = new MCNP_Source("Neutron Source", MCNP_Particle.neutron());
        mySource.setEnergyDistribution(myDistribution);

        MCNP_Deck myDeck = new MCNP_Deck("Test Deck");
        myDeck.addCell(myCell);
        myDeck.addCell(mySecondCell);
        myDeck.addParticleToSimulate(neutron);
        myDeck.setSource(mySource);



        System.out.print(myDeck);

    }
}
