/**
 * Created by lahmann on 06/04/2015.
 */
public class Pair {

    private Object firstObject;
    private Object secondObject;

    protected Pair(Object firstObject, Object secondObject){
        this.firstObject = firstObject;
        this.secondObject = secondObject;
    }

    protected Object first(){
        return this.firstObject;
    }

    protected Object second(){
        return this.secondObject;
    }
}
