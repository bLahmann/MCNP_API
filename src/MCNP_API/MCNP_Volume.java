package MCNP_API;

/**
 * Created by lahmann on 06/04/2015.
 */
public class MCNP_Volume extends MCNP_Object{

    private MCNP_Surface surface;
    private Orientation orientation;

    public enum Orientation{
        POSITIVE, NEGATIVE
    }

    public MCNP_Volume(MCNP_Surface surface, Orientation orientation) {
        this.surface = surface;
        this.orientation = orientation;
    }

    public MCNP_Surface getSurface() {
        return surface;
    }

    public String toString(){
        String string = "";

        if (orientation == Orientation.POSITIVE){
            string = string.concat("+");
        }else{
            string = string.concat("-");
        }

        string = string.concat(surface.getID().toString());

        return string;
    }


}
