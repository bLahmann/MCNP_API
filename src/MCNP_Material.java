import java.util.Vector;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_Material extends MCNP_Object {

    private static Integer totalMaterials = 0;

    private String name;
    private Integer id;
    private Vector<MCNP_Isotope> isotopes;
    private Vector<Double>  fractions;
    private Double density;

    public MCNP_Material(String name, Double density){
        this.totalMaterials++;

        this.name = name;
        this.id = totalMaterials;
        this.isotopes = new Vector<MCNP_Isotope>();
        this.fractions = new Vector<Double>();
        this.density = density;
    }

    public MCNP_Material(Double density){
        this("Unnamed Material", density);
    }

    public void addIsotope(MCNP_Isotope isotope, Double fraction){
        this.isotopes.add(isotope);
        this.fractions.add(fraction);
    }

    protected Integer getID(){
        return this.id;
    }

    protected Double getDensity(){
        return this.density;
    }

    public String toString(){
        Vector<String> lines = new Vector<String>();
        String s = new String();

        s += this.name + " (";
        if(this.density > 0){
            s += "n = ";
        }else{
            s += "rho = ";
        }
        s += String.format("%+.4e)", this.density);

        lines.add(MCNP_API_Utilities.formatCardEnd("m" + id.toString(), s));

        for(int i = 0; i < isotopes.size(); i++){
            s  = "        ";
            s += isotopes.get(i).toString() + " ";
            s += String.format("%+.4e", fractions.get(i));

            lines.add(MCNP_API_Utilities.formatCardEnd(s, isotopes.get(i).getName()));
        }

        String finalString = new String();
        for(String line : lines){
            finalString += line + '\n';
        }

        return finalString;
    }




}
