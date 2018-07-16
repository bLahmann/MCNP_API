package MCNP_API;

/**
 * Created by Brandon Lahmann on 5/31/2015.
 * TODO: A lot of the toString() methods use this copy and pasted bit to ensure lines don't exceed 80 characters. Consider a method here instead
 * TODO: Combining the Vector<String> to a single String can be made into a method
 */
public class MCNP_API_Utilities {

    protected static final String commentLine = "C ******************************************************************************";

    public static double[] linspace(double a, double b, int N){

        double[] values = new double[N];
        for (int i = 0; i < values.length; i++){
            values[i] = a + (b - a) * i / (N-1);
        }
        return values;

    }

    public static double[] linspace(double a, double b, double dx){
        int N = (int) Math.round((b-a)/dx) + 1;
        return linspace(a, b, N);
    }

    protected static String formatCardEnd(String card){
        while(card.length() < 79){
            card += " ";
        }

        return card;
    }

    protected static String formatCardEnd(String card, String comment){

        card = formatCardEnd(card);
        card += "$ " + comment;

        return card;
    }

    protected static String centerString(String s, Integer characterLimit){
        String centeredString = new String();
        Integer blankCharacters = characterLimit - s.length();

        for(int i = 0; i < blankCharacters/2; i++){
            centeredString += " ";
        }

        blankCharacters -= (int) Math.floor(blankCharacters/2);

        centeredString += s;
        for(int i = 0; i < blankCharacters; i++){
            centeredString += " ";
        }

        return centeredString;
    }
}
