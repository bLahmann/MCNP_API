import java.util.Vector;

/**
 * Created by lahmann on 06/04/2015.
 */
public class MCNP_SurfaceGroup extends MCNP_Object{

    private Vector<Pair> surfaceGroup;

    public MCNP_SurfaceGroup(){
        this.surfaceGroup = new Vector<Pair>();
    }

    public void addSurface(MCNP_Surface surface, Orientation orientation){
        this.surfaceGroup.add(new Pair(surface, orientation));
    }

    protected Vector<MCNP_Surface> getSurfaces(){
        Vector<MCNP_Surface> surfaces = new Vector<MCNP_Surface>();

        for(Pair pair : surfaceGroup){
            surfaces.add((MCNP_Surface) pair.first());
        }

        return surfaces;
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
