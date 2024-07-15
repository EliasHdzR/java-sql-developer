package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     *
     * @param line
     * @param keyword
     * @return Una query sin la KEYWORD de inicio ni el punto y coma. Ej "* FROM ALUMNOS"
     * @throws StringIndexOutOfBoundsException
     */
    public static String clean(String line, String keyword) throws StringIndexOutOfBoundsException {
        int endOfKeyword = line.indexOf(keyword) + keyword.length();
        int semicolon = line.indexOf(";");

        line = line.substring(endOfKeyword + 1, semicolon);
        return line.trim();
    }

    public static String clean(String line) {
        int semicolon = line.indexOf(";");

        line = line.substring(0, semicolon);
        return line.trim();
    }

    /**
     *
     * @param line
     * @param table
     * @return Separa las condiciones encontradas en la sentencia WHERE y las agrega a un array de strings
     * @throws SQLSyntaxException
     * @throws IndexOutOfBoundsException
     */
    public static ArrayList<String> getWhereTokens(String line, Table table) throws SQLSyntaxException, IndexOutOfBoundsException {
        Analyzer analyzer = new Analyzer();
        ArrayList<String> whereTokens = new ArrayList<>();
        String value;

        String format1 = "^'.+'";
        String format2 = "^\\d*\\s+";
        String format3 = "^\\d*$";

        Pattern pattern1 = Pattern.compile(format1);
        Pattern pattern2 = Pattern.compile(format2);
        Pattern pattern3 = Pattern.compile(format3);
        Matcher matcher1, matcher2, matcher3;

        line = line.replaceAll("<>","!=");
        line = line.replaceAll("^=","!=");

        while(!line.isBlank()){
            String initStateLine = line;

            for(int i = 0; i < analyzer.getOperators().size(); i++){
                String operator = analyzer.getOperators().get(i);

                if(line.startsWith(operator)){
                    if(operator.equals("(") || operator.equals(")") || operator.equals("AND") || operator.equals("OR")){
                        whereTokens.add(operator);
                    } else {
                        String lastToken = whereTokens.get(whereTokens.size()-1);

                        if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                            throw new SQLSyntaxException("UNEXPECTED OPERATOR: " + operator);
                        }

                        lastToken += (" " + operator);
                        whereTokens.set(whereTokens.size()-1, lastToken);
                    }

                    line = line.substring(line.indexOf(operator) + operator.length()).trim();
                    break;
                }
            }

            for(int i = 0; i < table.getColumnsName().size(); i++){
                String columnName = table.getColumnsName().get(i);

                if(line.startsWith(columnName)){
                    whereTokens.add(columnName);
                    line = line.substring(line.indexOf(columnName) + columnName.length()).trim();
                    break;
                }
            }

            if(line.startsWith("NULL")){
                String lastToken = whereTokens.get(whereTokens.size()-1);

                if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                    throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                }

                lastToken += " \0";
                whereTokens.set(whereTokens.size()-1, lastToken);
                line = line.substring(line.indexOf("NULL") + "NULL".length()).trim();
            }

            matcher1 = pattern1.matcher(line);
            if(matcher1.find()){
                String lastToken = whereTokens.get(whereTokens.size()-1);

                if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                    throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                }

                line = line.substring(1);
                value = line.substring(0, line.indexOf("'")).trim();
                lastToken += (" " + value);
                whereTokens.set(whereTokens.size()-1, lastToken);
                line = line.substring(line.indexOf(value) + value.length() + 1).trim();

                continue;
            }

            matcher2 = pattern2.matcher(line);
            if(matcher2.find()) {
                String lastToken = whereTokens.get(whereTokens.size()-1);

                if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                    throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                }

                value = line.substring(0, line.indexOf(" ")).trim();
                lastToken += (" " + value);
                whereTokens.set(whereTokens.size()-1, lastToken);
                line = line.substring(line.indexOf(value) + value.length() + 1).trim();

                continue;
            }

            matcher3 = pattern3.matcher(line);
            if(matcher3.find()) {
                String lastToken = whereTokens.get(whereTokens.size()-1);

                if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                    throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                }

                value = line.trim();
                lastToken += (" " + value);
                whereTokens.set(whereTokens.size()-1, lastToken);
                line = line.substring(line.indexOf(value) + value.length()).trim();
            }

            if(initStateLine.equals(line)){
                throw new SQLSyntaxException("MALFORMED STATEMENT AT > " + line);
            }
        }

        return whereTokens;
    }

    /**
     * Una query solo tiene un grupo de KEYWORDS válidas, aquí se comprueba que sólo tenga esas KEYWORDS válidas
     * y que no se repitan
     * @param query
     * @param validKeywords
     * @throws SQLSyntaxException
     */
    public static void hasValidKeywords(String query, ArrayList<String> validKeywords) throws SQLSyntaxException {
        Analyzer analyzer = new Analyzer();
        ArrayList<String> keywords = analyzer.getKeywords();
        Set<String> foundKeywords = getQueryKeywords(query, validKeywords, keywords);

        String[] fKeywords = foundKeywords.toArray(new String[foundKeywords.size()]);

        try {
            for (int i = 0; i < validKeywords.size(); i++) {
                if (!validKeywords.get(i).equals(fKeywords[i])) {
                    throw new SQLSyntaxException("KEYWORDS OUT OF ORDER");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SQLSyntaxException("KEYWORDS NOT FOUND OR NOT VALIDATED");
        }
    }

    /**
     * Devuelve todas las keywords enconradas en el query asegurandose de que no se repitan
     * @param query
     * @param validKeywords
     * @param keywords
     * @return Un LinkedHashSet con las Keywords encontradas en el query
     * @throws SQLSyntaxException
     */
    private static LinkedHashSet<String> getQueryKeywords(String query, ArrayList<String> validKeywords, ArrayList<String> keywords) throws SQLSyntaxException {
        LinkedHashSet<String> foundKeywords = new LinkedHashSet<>();

        ArrayList<String> tokensWithoutKeywords = splitByWords(query, keywords);
        ArrayList<String> tokenKeywords = splitByWords(query, tokensWithoutKeywords);

        for(String token : tokenKeywords){
            if(keywords.contains(token)){
                if(!validKeywords.contains(token)){
                    throw new SQLSyntaxException("UNEXPECTED KEYWORD '"+ token +"'");
                } else {
                    if(!foundKeywords.add(token)){
                        throw new SQLSyntaxException("UNEXPECTED KEYWORD '"+ token +"'");
                    }
                }
            }

        }
        return foundKeywords;
    }

    /**
     * Le hace split a una línea según las palabras dadas.
     * @param line la linea a splittear
     * @param words las palabras que van a dividir a la linea
     * @return Un ArrayList<String> con las palabras divididas sin contener a los divisores
     * @throws SQLSyntaxException
     */
    public static ArrayList<String> splitByWords(String line, ArrayList<String> words) throws SQLSyntaxException {
        ArrayList<String> tokens = new ArrayList<>();
        String token;

        // Crear un regex pattern a partir de las palabras divisoras
        StringBuilder patternBuilder = new StringBuilder();
        for (String word : words) {
            if (patternBuilder.length() > 0) {
                patternBuilder.append("|");
            }
            patternBuilder.append(Pattern.quote(word));
        }

        Pattern pattern = Pattern.compile(patternBuilder.toString());
        Matcher matcher = pattern.matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                token = line.substring(lastEnd, matcher.start()).trim();
                if(!token.isEmpty()){
                    tokens.add(token);
                }
            }
            lastEnd = matcher.end();
        }

        if (lastEnd < line.length()) {
            tokens.add(line.substring(lastEnd).trim());
        }

        return tokens;
    }
}
