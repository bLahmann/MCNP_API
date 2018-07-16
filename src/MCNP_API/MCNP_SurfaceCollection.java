package MCNP_API;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by lahmann on 06/04/2015.
 */
public class MCNP_SurfaceCollection extends MCNP_Object{

    private ArrayList<MCNP_Object> objects = new ArrayList<>();
    private boolean unionGroup;

    public MCNP_SurfaceCollection(boolean unionGroup){
        this.unionGroup = unionGroup;
    }

    public void addSurface(MCNP_Surface surface, MCNP_Volume.Orientation orientation){
        objects.add(new MCNP_Volume(surface, orientation));
    }

    public void addSubCollection(MCNP_SurfaceCollection collection){
        objects.add(collection);
    }

    protected ArrayList<MCNP_Surface> getSurfaces(){
        ArrayList<MCNP_Surface> surfaces = new ArrayList<>();

        for (MCNP_Object object : objects){
            if (object.getClass().equals(MCNP_Volume.class)){
                MCNP_Volume volume = (MCNP_Volume) object;
                surfaces.add(volume.getSurface());
            }else{
                MCNP_SurfaceCollection collection = (MCNP_SurfaceCollection) object;
                surfaces.addAll(collection.getSurfaces());
            }
        }

        return surfaces;
    }



    public String toString(){
        return toString(true);
    }

    public String toString(Boolean encapsulate){

        String string = "";
        if (encapsulate) string = string.concat("(");

        for(int i = 0; i < objects.size(); i++){
            MCNP_Object object = objects.get(i);

            string = string.concat(object.toString());

            if(i == objects.size() - 1){
                if (encapsulate)    string = string.concat(")");
            }else if (unionGroup){
                string = string.concat(":");
            }else{
                string = string.concat(" ");
            }
        }

        return string;
    }
}
