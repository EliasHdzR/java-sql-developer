package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Reader {
    public String consoleReader() throws SQLSyntaxException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try{
            String input;
            StringBuffer inputLines = new StringBuffer();

            while ((input = reader.readLine()) != null) {
                if(input.trim().isEmpty()){
                    continue;
                }

                if(input.toUpperCase().startsWith("USE")){
                    inputLines.append(input.trim());
                } else {
                    inputLines.append(input.toUpperCase().trim());
                }

                int index = input.indexOf(";");
                int index2 = input.indexOf(";",index + 1);
                if(index2 != -1){
                    throw new SQLSyntaxException("SYNTAX ERROR AT COLUMN: " + index);
                }

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
}
