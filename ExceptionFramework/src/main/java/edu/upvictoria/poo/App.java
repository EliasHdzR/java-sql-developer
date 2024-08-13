/**
 * Perfect Support
 */

package edu.upvictoria.poo;

import java.util.ArrayList;

public class App {
    public static void main( String[] args ) {
        App app = new App();
        app.run();
    }

    public final Analyzer analyzer = new Analyzer();
    public final Reader reader = new Reader();

    public void run(){
        String line;

        while(true){
            System.out.print("$> ");
            try{
                line = reader.consoleReader();
                ArrayList<ArrayList<String>> printableTable = analyzer.analyzeSyntax(line);

                if (printableTable != null) {
                    print(printableTable);
                }
            } catch (Exception e){
                System.out.println("ERR: " + e.getMessage() + "\n");
            }
        }
    }

    private void print(ArrayList<ArrayList<String>> printableTable){
        for (ArrayList<String> row : printableTable) {
            for (String value : row){
                System.out.print("| " + value + "\t");
            }
            System.out.println("|");
            for (int i = 0; i < row.size(); i++) {
                System.out.print("+------------");
            }
            System.out.println("+");
        }
    }
}
