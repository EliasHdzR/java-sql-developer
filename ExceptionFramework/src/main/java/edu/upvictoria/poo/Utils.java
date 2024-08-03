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
     * @return Separa las condiciones encontradas en la sentencia WHERE y las agrega a un array de strings
     * @throws SQLSyntaxException
     * @throws IndexOutOfBoundsException
     */
    public static ArrayList<String> getWhereTokens(String line) throws SQLSyntaxException, IndexOutOfBoundsException {
        line = line.replaceAll("<>","!=");
        line = line.replaceAll("\\^=","!=");
        int functionCount = 0;

        // si no agrego espacios se van a romper palabras como 'CORREO'
        ArrayList<String> keywords = new ArrayList<>(Analyzer.getComparators());
        keywords.addAll(Analyzer.getOperators());
        keywords.addAll(Analyzer.getFunctions());
        keywords.set(0," AND ");
        keywords.set(1," OR ");

        ArrayList<String> tokens = splitByWords(line, keywords, true);

        for (String token : tokens) {
            if ((!token.startsWith("'") && token.endsWith("'"))
                    || (token.startsWith("'") && !token.endsWith("'"))) {
                throw new SQLSyntaxException("UNMATCHED SINGLE QUOTE (')");
            }

            if(Analyzer.getFunctions().contains(token)) {
                functionCount++;
            }
        }

        if(functionCount > 0){
            tokens = convertFunctionParentheses(tokens, functionCount);
        }

        return tokens;
    }

    /**
     * Una query solo tiene un grupo de KEYWORDS válidas, aquí se comprueba que sólo tenga esas KEYWORDS válidas
     * y que no se repitan
     * @param query
     * @param validKeywords
     * @throws SQLSyntaxException
     */
    public static void hasValidKeywords(String query, ArrayList<String> validKeywords) throws SQLSyntaxException {
        ArrayList<String> keywords = Analyzer.getKeywords();
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

        ArrayList<String> tokensWithoutKeywords = splitByWords(query, keywords, false);
        ArrayList<String> tokenKeywords = splitByWords(query, tokensWithoutKeywords, false);

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
    public static ArrayList<String> splitByWords(String line, ArrayList<String> words, Boolean includeSplittingWords) {
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
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
            }

            // agregar la palabra divisora al array de tokens si includeSplittingWords es verdadero
            if (includeSplittingWords) {
                tokens.add(matcher.group().trim());
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < line.length()) {
            tokens.add(line.substring(lastEnd).trim());
        }

        return tokens;
    }

    /**
     * Las sintaxis de las funciones siempre es nombre(parametro), el problema es que los '()' de la función estorban
     * cuando quiero convertir una operacion de infix a postix debido a la jerarquía de operaciones por lo que las
     * convierto a '[]' para que no haya problemas
     * @param tokens
     * @param functionsCount
     * @return Un array list de string con los paréntesis correspondientes a las funciones convertidos a corchetes.   ( -> [
     * @throws SQLSyntaxException
     */
    public static ArrayList<String> convertFunctionParentheses(ArrayList<String> tokens, int functionsCount) throws SQLSyntaxException {
        ArrayList<String> newTokens = new ArrayList<>();
        String newToken = "";
        int counter = 0, changes = 0;

        for(int i = 1; i < tokens.size(); i++){
            String lastToken = tokens.get(i-1);
            String currentToken = tokens.get(i);

            if(currentToken.equals("(") && Analyzer.getFunctions().contains(lastToken)){
                tokens.set(i,"[");
                newToken += tokens.get(i-1);
                counter++;
                changes++;
            }

            if(currentToken.equals(")")){
                for(int j = 0; j < counter; j++){
                    if(tokens.get(i+j).equals(")")){
                        tokens.set(i+j,"]");
                        changes++;
                        newToken += tokens.get(i+j);
                        i = i+j;
                    } else {
                        throw new SQLSyntaxException("UNMATCHED PARENTHESES");
                    }
                }
                newTokens.add(newToken);
                newToken = "";
                counter = 0;
                continue;
            }

            if(counter == 0 && !Analyzer.getFunctions().contains(currentToken)){
                newTokens.add(tokens.get(i));
            } else if (counter > 0 && !Analyzer.getFunctions().contains(currentToken)){
                newToken += tokens.get(i);
            }
        }

        // siempre va a haber una cantidad de cambios igual al doble de funciones porque cada funcion
        // tiene un par de parentesis que le corresponden
        if(changes/2 != functionsCount){
            throw new SQLSyntaxException("UNMATCHED PARENTHESES");
        }

        return newTokens;
    }
}
