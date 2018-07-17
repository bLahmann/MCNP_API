package MCNP_API;

public class Material_Library {

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

    public static MCNP_Material boron(String crossSectionLibrary){
        MCNP_Material material = new MCNP_Material("Boron", -2.37);
        material.addIsotope(new MCNP_Isotope("B", 5, 10, crossSectionLibrary), 0.2);
        material.addIsotope(new MCNP_Isotope("B", 5, 11, crossSectionLibrary), 0.8);
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

    public static MCNP_Material titanium(String crossSectionLibrary) {
        MCNP_Material material = new MCNP_Material("Titanium", -4.506);
        material.addIsotope(new MCNP_Isotope("Ti46", 22, 46, crossSectionLibrary), 0.0825);
        material.addIsotope(new MCNP_Isotope("Ti47", 22, 47, crossSectionLibrary), 0.0744);
        material.addIsotope(new MCNP_Isotope("Ti48", 22, 48, crossSectionLibrary), 0.7372);
        material.addIsotope(new MCNP_Isotope("Ti49", 22, 49, crossSectionLibrary), 0.0541);
        material.addIsotope(new MCNP_Isotope("Ti50", 22, 50, crossSectionLibrary), 0.0518);
        return material;
    }

    public static MCNP_Material AlSl304_SS(String crossSectionLibrary) {
        MCNP_Material material = new MCNP_Material("AISI SS", -8.0);
        material.addIsotope(new MCNP_Isotope("12C",  6, 12, crossSectionLibrary), 1.8096E-03);
        material.addIsotope(new MCNP_Isotope("13C",  6, 13, crossSectionLibrary), 2.0127E-05);
        material.addIsotope(new MCNP_Isotope("50Cr",  24, 50, crossSectionLibrary), 8.7232E-03);
        material.addIsotope(new MCNP_Isotope("52Cr",  24, 52, crossSectionLibrary), 1.6822E-01);
        material.addIsotope(new MCNP_Isotope("53Cr",  24, 53, crossSectionLibrary), 1.9075E-02);
        material.addIsotope(new MCNP_Isotope("54Cr",  24, 54, crossSectionLibrary), 4.7481E-03);
        material.addIsotope(new MCNP_Isotope("54Fe",  26, 54, crossSectionLibrary), 4.0387E-02);
        material.addIsotope(new MCNP_Isotope("56Fe",  26, 56, crossSectionLibrary), 6.3342E-01);
        material.addIsotope(new MCNP_Isotope("57Fe",  26, 57, crossSectionLibrary), 1.4636E-02);
        material.addIsotope(new MCNP_Isotope("58Fe",  26, 58, crossSectionLibrary), 1.9331E-03);
        material.addIsotope(new MCNP_Isotope("55Mn",  25, 55, crossSectionLibrary), 1.0001E-02);
        material.addIsotope(new MCNP_Isotope("58Ni",  28, 58, crossSectionLibrary), 5.8946E-02);
        material.addIsotope(new MCNP_Isotope("60Ni",  28, 60, crossSectionLibrary), 2.2706E-02);
        material.addIsotope(new MCNP_Isotope("61Ni",  28, 61, crossSectionLibrary), 9.8710E-04);
        material.addIsotope(new MCNP_Isotope("62Ni",  28, 62, crossSectionLibrary), 3.1475E-03);
        material.addIsotope(new MCNP_Isotope("64Ni",  28, 64, crossSectionLibrary), 8.0180E-04);
        material.addIsotope(new MCNP_Isotope("31P",  15, 31, crossSectionLibrary), 3.9911E-04);
        material.addIsotope(new MCNP_Isotope("32S",  16, 32, crossSectionLibrary), 2.4414E-04);
        material.addIsotope(new MCNP_Isotope("33S",  16, 33, crossSectionLibrary), 1.9276E-06);
        material.addIsotope(new MCNP_Isotope("34S",  16, 34, crossSectionLibrary), 1.0923E-05);
        material.addIsotope(new MCNP_Isotope("28Si",  14, 28, crossSectionLibrary), 9.0183E-03);
        material.addIsotope(new MCNP_Isotope("29Si",  14, 29, crossSectionLibrary), 4.5972E-04);
        material.addIsotope(new MCNP_Isotope("30Si",  14, 30, crossSectionLibrary), 3.0322E-04);
        return material;
    }
}
