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
            } catch (SQLSyntaxException e){
                System.out.println(e.getMessage() + "\n");
                continue;
            }

            /*try{
                for(String line : lines){
                    analyzer.analyzeSyntax(line, lines.size());
                }
            } catch (IOException e){
                System.out.println("ERR: Error en la Sentencia: " + e.getMessage() + "\n");
            } catch (Exception e){
                System.out.println("ERR: " + e.getMessage() + "\n");
            }*/
        }
    }
}
