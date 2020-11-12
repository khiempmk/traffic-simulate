package simulator;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class VisualizeHelper {
    private static String input = "80_GA.out";

    public static void main(String[] args) {
        try {
            double slot = 0.25 ;
            FileWriter fileWriter = new FileWriter("slot_" +slot+ "_" +input);
             Scanner scanner = new Scanner(new File(input)) ;
            double startTime1 = 5.0 ;
            double delayAll = 0 ;

            double num = 0;
            while (scanner.hasNext()){
                double startTime = scanner.nextDouble() ;
                scanner.nextDouble() ;
                double delay = scanner.nextDouble() ;
                String s = scanner.nextLine();
                if (startTime - startTime1 <= slot ){
                    delayAll += delay ;
                    num ++ ;
                } else {
                    if (delayAll > 0 ) {
                        fileWriter.write(startTime1 + "\t" + delayAll / num + "\n");
                    }
                    startTime1 += slot ;
                    delayAll = delay ;
                    num = 1 ;
                }
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e){
        }
    }

}
