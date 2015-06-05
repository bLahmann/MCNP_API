/**
 * Created by Brandon Lahmann on 5/31/2015.
 */
public class MCNP_API_Utilities {

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
}
