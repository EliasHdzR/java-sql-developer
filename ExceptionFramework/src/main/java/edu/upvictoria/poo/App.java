/**
 * Perfect Support
 */

package edu.upvictoria.poo;

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
                analyzer.analyzeSyntax(line);
            } catch (Exception e){
                System.out.println("ERR: " + e.getMessage() + "\n");
            }
        }
    }
}
