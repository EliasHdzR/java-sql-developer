package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reader {
    /**
     * Este consoleReader lee continuamente el teclado desde la consola hasta que se ingrese un punto y coma
     * @return
     * @throws SQLSyntaxException
     */
    public String consoleReader() throws SQLSyntaxException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try{
            String input;
            StringBuffer inputLines = new StringBuffer();

            while ((input = reader.readLine()) != null) {
                if(input.trim().isEmpty()){
                    continue;
                }

                input = getString(input, inputLines);

                if(input.endsWith(";")) {
                    break;
                }
            }

            inputLines = formatInput(inputLines);
            return inputLines.toString();

        } catch (IOException e) {
            throw new SQLSyntaxException("SYNTAX ERROR FOUND: " + e.getMessage());
        }
    }

    /**
     * Esta otra version de consoleReader recibe un input completo para poder parsearlo
     * @param input
     * @return
     * @throws SQLSyntaxException
     */
    public String consoleReader(String input) throws SQLSyntaxException {
        StringBuffer inputLines = new StringBuffer();

        if(input.trim().isEmpty()){
            throw new SQLSyntaxException("INVALID STATEMENT IN SELECTION");
        }

        try{
            input = getString(input, inputLines);

            if(!input.endsWith(";")) {
                throw new SQLSyntaxException("MISSING SEMICOLON AT THE END");
            }

            inputLines = formatInput(inputLines);
            return inputLines.toString();
        } catch (Exception e) {
            throw new SQLSyntaxException("UNPARSEABLE STATEMENT");
        }
    }

    private String getString(String input, StringBuffer inputLines) throws SQLSyntaxException {
        if(input.toUpperCase().startsWith("USE")){
            String[] tokens = input.split(" ");
            tokens[0] = tokens[0].toUpperCase();
            StringBuilder builder = new StringBuilder();

            for (String token : tokens) {
                builder.append(token).append(" ");
            }

            inputLines.append(builder.toString().trim());
        } else {
            input = formatInput2(input);
            inputLines.append(input.trim());
        }

        int index = input.indexOf(";");
        int index2 = input.indexOf(";",index + 1);
        if(index2 != -1){
            throw new SQLSyntaxException("SYNTAX ERROR AT COLUMN: " + index);
        }
        return input;
    }

    public StringBuffer formatInput(StringBuffer input) throws SQLSyntaxException {
        int lastIndex = 0;
        ArrayList<String> foundKeywords = new ArrayList<>();

        for(int i = 0; i < Analyzer.getKeywords().size(); i++){
            String keyword = Analyzer.getKeywords().get(i);
            int index = input.indexOf(keyword + " ", lastIndex);

            if(index != -1){
                if(foundKeywords.contains(keyword)){
                    throw new SQLSyntaxException("REPEATED KEYWORD " + keyword);
                }

                if(!foundKeywords.isEmpty()) { input.insert(index,"\n"); }
                foundKeywords.add(keyword);
                lastIndex =  index + keyword.length() + 1;
                i = 0;
            }
        }

        return input;
    }

    public String formatInput2(String query) throws SQLSyntaxException {
        int quoteCount = 0;
        for (char c : query.toCharArray()) {
            if (c == '\'') {
                quoteCount++;
            }
        }

        if (quoteCount % 2 != 0) {
            throw new SQLSyntaxException("UNMATCHED SINGLE QUOTE (')");
        }

        Pattern pattern = Pattern.compile("'([^']*)'");
        Matcher matcher = pattern.matcher(query);

        int lastEnd = 0;

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            result.append(query.substring(lastEnd, matcher.start()).toUpperCase());
            result.append(matcher.group());
            lastEnd = matcher.end();
        }
        result.append(query.substring(lastEnd).toUpperCase());

        return result.toString();
    }
}
