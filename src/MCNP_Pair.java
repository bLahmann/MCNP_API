/**
 * Created by lahmann on 06/04/2015.
 */
public class MCNP_Pair {

    private MCNP_Object firstObject;
    private MCNP_Object secondObject;

    protected MCNP_Pair(MCNP_Object firstObject, MCNP_Object secondObject){
        this.firstObject = firstObject;
        this.secondObject = secondObject;
    }

    protected MCNP_Object first(){
        return this.firstObject;
    }

    protected MCNP_Object second(){
        return this.secondObject;
    }
}
