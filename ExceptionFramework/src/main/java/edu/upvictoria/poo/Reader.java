package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class Reader {
    public String consoleReader(Analyzer analyzer) throws SQLSyntaxException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try{
            String input;
            StringBuffer inputLines = new StringBuffer();

            while ((input = reader.readLine()) != null) {
                if(input.trim().isEmpty()){
                    continue;
                }

                inputLines.append(input.toUpperCase().trim());

                int index = input.indexOf(";");
                int index2 = input.indexOf(";",index + 1);
                if(index2 != -1){
                    throw new SQLSyntaxException("SYNTAX ERROR AT COLUMN: " + index);
                }

                if(input.endsWith(";")) {
                    break;
                }
            }

            inputLines = formatInput(inputLines, analyzer);
            return inputLines.toString();

        } catch (IOException e) {
            throw new SQLSyntaxException("SYNTAX ERROR FOUND: " + e.getMessage());
        }
    }

    public StringBuffer formatInput(StringBuffer input, Analyzer analyzer) throws SQLSyntaxException {
        int lastIndex = 0;
        ArrayList<String> foundKeywords = new ArrayList<>();

        for(int i = 0; i < analyzer.getKeywords().size(); i++){
            String keyword = analyzer.getKeywords().get(i);
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
