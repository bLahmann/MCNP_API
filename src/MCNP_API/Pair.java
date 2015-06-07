package MCNP_API;

/**
 * Created by lahmann on 06/04/2015.
 */
public class Pair {

    private Object firstObject;
    private Object secondObject;

    public Pair(Object firstObject, Object secondObject){
        this.firstObject = firstObject;
        this.secondObject = secondObject;
    }

    public Object first(){
        return this.firstObject;
    }

    public Object second(){
        return this.secondObject;
    }
}
