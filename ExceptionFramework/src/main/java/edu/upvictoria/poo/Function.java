package edu.upvictoria.poo;

import edu.upvictoria.poo.DMLProcedures.Where.Tree;
import edu.upvictoria.poo.DMLProcedures.Where.Where;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.util.ArrayList;

public class Function {

    private final String name;
    private String parameter;

    public Function(String name) {
        this.name = name;
    }

    public static String evaluateFunction(String line, ArrayList<Object> row, Table table, boolean generalUsage) throws SQLSyntaxException {
        ArrayList<Function> functions = parseFunctions(line, row, table);
        String result = "";

        for(int i = functions.size() - 1; i >= 0; i--) {
            Function function = functions.get(i);

            switch (function.getName()) {
                case "UPPER":
                    result = handleUPPER(function.getParameter(), row, table);
                    break;
                case "LOWER":
                    result = handleLOWER(function.getParameter(), row, table);
                    break;
                case "FLOOR":
                    result = handleFLOOR(function.getParameter(), row, table);
                    break;
                case "CEIL":
                    result = handleCEIL(function.getParameter(), row, table);
                    break;
                case "ROUND":
                    result = handleROUND(function.getParameter(), row, table);
                    break;
                case "RAND":
                    result = handleRAND(function.getParameter(), row, table);
                    break;
                /*case "MIN":
                    if(generalUsage){
                        throw new SQLSyntaxException("BAD USAGE OF FUNCTION " + function.getName());
                    }
                    break;

                case "MAX":
                    if(generalUsage){
                        throw new SQLSyntaxException("BAD USAGE OF FUNCTION " + function.getName());
                    }
                    break;

                case "SUM":
                    if(generalUsage){
                        throw new SQLSyntaxException("BAD USAGE OF FUNCTION " + function.getName());
                    }
                    break;

                case "AVG":
                    if(generalUsage){
                        throw new SQLSyntaxException("BAD USAGE OF FUNCTION " + function.getName());
                    }
                    break;
                case "COUNT":
                    if(generalUsage){
                        throw new SQLSyntaxException("BAD USAGE OF FUNCTION " + function.getName());
                    }
                    break;
                case "DISTINCT":
                    if(generalUsage){
                        throw new SQLSyntaxException("BAD USAGE OF FUNCTION " + function.getName());
                    }
                    break;*/
                default:
                    throw new SQLSyntaxException("UNSUPPORTED FUNCTION IN STATEMENT");
            }

            if (i >= 1) {
                functions.get(i-1).setParameter(result);
            }
        }

        return result;
    }

    /**
     * Todas las funciones solamente reciben un parámetro, pero pueden ser concatenadas, por lo que esta función
     * parsea las funciones y busca si estas pueden estar concatenadas o no.
     * @param line String sin parsear que contiene a las funciones
     * @return Un array con todas las funciones reconocidas, solamente la ultima función del array tiene su parámetro
     * definido
     * @throws SQLSyntaxException si algún nombre parseado no pertenece a una función definida
     */
    public static ArrayList<Function> parseFunctions(String line, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        ArrayList<Function> functions = new ArrayList<>();
        Function temp;
        do {
            String functionName = line.substring(0, line.indexOf("[")).trim();
            if(!Analyzer.getFunctions().contains(functionName)){
                throw new SQLSyntaxException("UNDEFINED FUNCTION " + functionName);
            }

            temp = new Function(functionName);
            functions.add(temp);
            line = line.substring(line.indexOf("[") + 1);
        } while (line.contains("["));

        // hay que checar si el parámetro es una operacion aritmetica
        String parameter = line.substring(0, line.indexOf("]")).trim();

        ArrayList<String> operators = new ArrayList<>(Analyzer.getOperators());
        operators.add("(");
        operators.add(")");

        ArrayList<String> tokens = Utils.splitByWords(parameter, operators, true);
        if(tokens.size() > 1){
            tokens = Where.infixToPostfix(tokens);
            Tree.Node root = Where.createTree(tokens);
            parameter = Where.evaluateSubTree(root, row, table);
        }

        temp.setParameter(parameter);
        return functions;
    }

    /**
     * Se usa para saber si iniciar o no con el parseo de funciones.
     * @param nodeValue Un nodo
     * @return true si el nodo inicia con una función y false si no.
     */
    public static boolean analyzeNode(String nodeValue){
        for(String functionName : Analyzer.getFunctions()){
            if(nodeValue.startsWith(functionName)){
                return true;
            }
        }

        return false;
    }

    public String getName() {
        return name;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * //////////////////////////////
     * ////////// FUNCTIONS /////////
     * //////////////////////////////
     */

    public static String handleUPPER(String value, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(value);
        value = Where.getString(value, row, table, column);

        try {
            Double.parseDouble(value);
            return value;
        } catch (NumberFormatException e) {
            value = "'" + value + "'";
        }

        return value.toUpperCase();
    }

    public static String handleLOWER(String value, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(value);
        value = Where.getString(value, row, table, column);

        try {
            Double.parseDouble(value);
            return value;
        } catch (NumberFormatException e) {
            value = "'" + value + "'";
        }

        return value.toLowerCase();
    }

    public static String handleFLOOR(String value, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(value);
        Double dValue = Where.getValue(value, row, table, column);

        if(dValue == null){
            return null;
        }

        int iValue = (int) Math.floor(dValue);
        return Integer.toString(iValue);
    }

    public static String handleCEIL(String value, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(value);
        Double dValue = Where.getValue(value, row, table, column);

        if(dValue == null){
            return null;
        }

        int iValue = (int) Math.ceil(dValue);
        return Integer.toString(iValue);
    }

    public static String handleROUND(String value, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(value);
        Double dValue = Where.getValue(value, row, table, column);

        if(dValue == null){
            return null;
        }

        int iValue = (int) Math.round(dValue);
        return Integer.toString(iValue);
    }

    public static String handleRAND(String maxValue, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(maxValue);
        Double dValue = Where.getValue(maxValue, row, table, column);

        if(dValue == null){
            return null;
        }

        int iValue = (int) (Math.random() * (dValue + 1));
        return Integer.toString(iValue);
    }


}
