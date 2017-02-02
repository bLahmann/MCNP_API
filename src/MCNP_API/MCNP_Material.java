package MCNP_API;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_Material extends MCNP_Object {

    public static Integer totalMaterials = 0;

    private String name;
    private Integer id;
    private Vector<MCNP_Isotope> isotopes;
    private Vector<Double> fractions;
    private Double density;

    public static MCNP_Material deuterium(String crossSectionLibrary){
        MCNP_Material material = new MCNP_Material("Deuterium", -1.0);
        material.addIsotope(new MCNP_Isotope("H2 (D)", 1, 2, crossSectionLibrary), 1.0);
        return material;
    }

    public static MCNP_Material beryllium(String crossSectionLibrary){
        MCNP_Material material = new MCNP_Material("Beryllium", -1.85);
        material.addIsotope(new MCNP_Isotope("Be9", 4, 9, crossSectionLibrary), 1.0);
        return material;
    }

    public static MCNP_Material cr39(String crossSectionLibrary) {
        MCNP_Material material = new MCNP_Material("CR-39 Nuclear Track Detector", -1.31);
        material.addIsotope(new MCNP_Isotope("H1", 1, 1, crossSectionLibrary), 18.0);
        material.addIsotope(new MCNP_Isotope("C12", 6, 12, crossSectionLibrary), 12.0);
        material.addIsotope(new MCNP_Isotope("O16", 8, 16, crossSectionLibrary), 7.0);
        return material;
    }

    public static MCNP_Material ch2(String crossSectionLibrary) {
        MCNP_Material material = new MCNP_Material("1:2 Polyethylene", -1.00);
        material.addIsotope(new MCNP_Isotope("H1", 1, 1, crossSectionLibrary), 2.0);
        material.addIsotope(new MCNP_Isotope("C12", 6, 12, crossSectionLibrary), 1.0);
        return material;
    }

    public static MCNP_Material cd2(String crossSectionLibrary)
    {
        MCNP_Material material = new MCNP_Material("1:2 Deuterated Polyethylene", -0.93);
        material.addIsotope(new MCNP_Isotope("H2 (D)", 1, 2, crossSectionLibrary), 2.0);
        material.addIsotope(new MCNP_Isotope("C12", 6, 12, crossSectionLibrary), 1.0);
        return material;

    }
    public static MCNP_Material aluminum(String crossSectionLibrary){
        MCNP_Material material = new MCNP_Material("Aluminum", -2.7);
        material.addIsotope(new MCNP_Isotope("Al27", 13, 27, crossSectionLibrary), 1.0);
        return material;
    }

    public static MCNP_Material tantalum(String crossSectionLibrary) {
        MCNP_Material material = new MCNP_Material("Tantalum", -16.69);
        material.addIsotope(new MCNP_Isotope("Ta181", 73, 181, crossSectionLibrary), 1.0);
        return material;
    }

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

    public String getName() {
        return name;
    }

    protected Integer getID(){
        return this.id;
    }

    protected Double getDensity(){
        return this.density;
    }

    public void setDensity(Double density) {
        this.density = density;
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

            lines.add(MCNP_API_Utilities.formatCardEnd(s, "  " + isotopes.get(i).getName()));
        }

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
