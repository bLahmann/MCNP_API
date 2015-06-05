import java.util.Vector;

/**
 * Created by lahmann on 06/04/2015.
 */
public class MCNP_SurfaceGroup extends MCNP_Object{

    public enum Orientation{
        NEGATIVE,
        POSITIVE
    }

    Vector<Pair> surfaceGroup;

    public MCNP_SurfaceGroup(){
        this.surfaceGroup = new Vector<Pair>();
    }

    public void addSurface(MCNP_Surface surface, Orientation orientation){
        this.surfaceGroup.add(new Pair(surface, orientation));
    }

    public String toString(){

        String s = "(";
        for(Pair pair : this.surfaceGroup){
            MCNP_Surface surface = (MCNP_Surface) pair.first();
            Orientation orientation = (Orientation) pair.second();

            if(orientation.equals(Orientation.NEGATIVE)){
                s += "-";
            }

            s += surface.getID().toString();

            if(pair.equals(surfaceGroup.lastElement())){
                s += ")";
            }else{
                s += " ";
            }
        }

        return s;

    }
}
