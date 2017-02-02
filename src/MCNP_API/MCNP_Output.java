package MCNP_API;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by lahmann on 2016-11-24.
 */
public class MCNP_Output {

    protected ArrayList<String> warnings;

    public MCNP_Output(File outputFile){
        try {
            Scanner s = new Scanner(outputFile);

            while (s.hasNext()){

                String temp = s.next();

                if (temp.equals("warning.")){
                    warnings.add(s.nextLine());
                }





            }

        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }
}
