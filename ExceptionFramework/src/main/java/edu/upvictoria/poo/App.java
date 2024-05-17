/**
 * Perfect Support
 */

package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.io.IOException;
import java.text.ParseException;
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
            try {
                line = reader.consoleReader(analyzer);
            } catch (Exception e){
                System.out.println("ERR: " + e.getMessage() + "\n");
                continue;
            }

            try{
                analyzer.analyzeSyntax(line);
            } catch (Exception e){
                System.out.println("ERR: " + e.getMessage() + "\n");
            }
        }
    }
}
