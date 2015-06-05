/**
 * Created by lahmann on 06/04/2015.
 * TODO: Implement a way for the user to pick from a list of cross section libraries
 * TODO: Consider a enum solution
 */
public class MCNP_Isotope extends MCNP_Object{

    private String name;
    private Integer Z;
    private Integer A;
    private String crossSectionLibrary;

    public MCNP_Isotope(String name, Integer Z, Integer A, String crossSectionLibrary){
        this.name = name;
        this.Z = Z;
        this.A = A;
        this.crossSectionLibrary = crossSectionLibrary;
    }

    public MCNP_Isotope(Integer Z, Integer A, String crossSectionLibrary){
        this("Unnamed Isotope", Z, A, crossSectionLibrary);
    }

    public Integer getZAID(){
        return 1000*Z + A;
    }

    public String getName(){
        return this.name;
    }

    public String toString(){
        return this.getZAID().toString() + "." + this.crossSectionLibrary;
    }


}
