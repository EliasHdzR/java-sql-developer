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

    /**
     * Todas las funciones solamente reciben un parámetro, pero pueden ser concatenadas, por lo que esta función
     * parsea las funciones y busca si estas pueden estar concatenadas o no.
     * @param line String sin parsear que contiene a las funciones
     * @return Un array con todas las funciones reconocidas, solamente la ultima función del array tiene su parámetro
     * definido
     * @throws SQLSyntaxException si algún nombre parseado no pertenece a una función definida
     */
    public static String parseFunctions(String line, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        ArrayList<Function> foundFunctions = new ArrayList<>();
        Function temp;
        String result = "";
        boolean hasGroupFunction = false;

        do {
            String functionName = line.substring(0, line.indexOf("[")).trim();
            if(!Analyzer.getFunctions().contains(functionName)){
                throw new SQLSyntaxException("UNDEFINED FUNCTION " + functionName);
            }

            if(Analyzer.getMultipleRowFunctions().contains(functionName)){
                hasGroupFunction = true;
            }

            temp = new Function(functionName);
            foundFunctions.add(temp);
            line = line.substring(line.indexOf("[") + 1);
        } while (line.contains("["));

        if(hasGroupFunction && foundFunctions.size() > 1){
            throw new SQLSyntaxException("BAD USAGE OF GROUP FUNCTIONS");
        }

        for(int i = foundFunctions.size() - 1; i >= 0; i--){
            temp = foundFunctions.get(i);
            String added = line.substring(0, line.indexOf("]")).trim();
            String parameter = result + added;
            line = line.substring(line.indexOf(added)+added.length()+1).trim();

            ArrayList<String> operators = new ArrayList<>(Analyzer.getOperators());
            operators.add("(");
            operators.add(")");

            // hay que checar si el parámetro es una operacion aritmetica
            ArrayList<String> tokens = Utils.splitByWords(parameter, operators, true);
            if(tokens.size() > 1){
                tokens = Where.infixToPostfix(tokens);
                Tree.Node root = Where.createTree(tokens);
                parameter = Where.evaluateSubTree(root, row, table);
            }

            temp.setParameter(parameter);
            result = evaluateFunction(temp, row, table);
            if(i >= 1){
                foundFunctions.get(i-1).setParameter(result);
            }
        }

        return result;
    }

    public static String evaluateFunction(Function function, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        String result;

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
            case "MIN":
                result = handleMIN(function.getParameter(), table);
                break;
            case "MAX":
                result = handleMAX(function.getParameter(), table);
                break;
            case "SUM":
               result = handleSUM(function.getParameter(), table);
                break;
            case "AVG":
                result = handleAVG(function.getParameter(), table);
                break;
            case "DISTINCT":
                throw new SQLSyntaxException("DISTINCT FUNCTION IMPLEMENTATION COMING SOON");
            case "COUNT":
                result = handleCOUNT(function.getParameter(), table);
                break;
            default:
                throw new SQLSyntaxException("UNSUPPORTED FUNCTION IN STATEMENT");
            }

        return result;
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

    public static String handleMIN(String parameter, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(parameter);

        if(column == null){
            throw new SQLSyntaxException("UNDEFINED COLUMN " + parameter);
        }

        int colPos = table.getColumnPos(parameter);
        ArrayList<String> values = extractColumn(table.getData(), colPos);
        ArrayList<String> result;
        
        switch (column.getType()) {
            case "INT":
                ArrayList<Integer> intValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }
                    
                    try{
                        intValues.add(Integer.parseInt(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE INTEGER FOUND:" + value);
                    }
                }

                result = sortArray(intValues);
                return result.get(result.size() - 1);
                
            case "DOUBLE":
                ArrayList<Double> doubleValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }

                    try{
                        doubleValues.add(Double.parseDouble(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE DOUBLE FOUND:" + value);
                    }
                }

                result = sortArray(doubleValues);
                return result.get(result.size() - 1);
                
            case "STRING": case "CHAR":
                ArrayList<String> stringValues = extractColumn(table.getData(), colPos);
                result = sortArray(stringValues);
                return result.get(result.size() - 1);
        }

        throw new SQLSyntaxException("UNSUPPORTED FUNCTION FOR " + column.getType() + " DATA TYPE");
    }

    public static String handleMAX(String parameter, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(parameter);

        if(column == null){
            throw new SQLSyntaxException("UNDEFINED COLUMN " + parameter);
        }

        int colPos = table.getColumnPos(parameter);
        ArrayList<String> values = extractColumn(table.getData(), colPos);
        ArrayList<String> result;
        
        switch (column.getType()) {
            case "INT":
                ArrayList<Integer> intValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }

                    try{
                        intValues.add(Integer.parseInt(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE INTEGER FOUND: " + value);
                    }
                }

                result = sortArray(intValues);
                return result.get(0);

            case "DOUBLE":
                ArrayList<Double> doubleValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }

                    try{
                        doubleValues.add(Double.parseDouble(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE DOUBLE FOUND: " + value);
                    }
                }

                result = sortArray(doubleValues);
                return result.get(0);

            case "STRING": case "CHAR":
                ArrayList<String> stringValues = extractColumn(table.getData(), colPos);
                result = sortArray(stringValues);
                return result.get(0);
        }

        throw new SQLSyntaxException("UNSUPPORTED FUNCTION FOR " + column.getType() + " DATA TYPE");
    }

    public static String handleSUM(String parameter, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(parameter);

        if(column == null){
            throw new SQLSyntaxException("UNDEFINED COLUMN " + parameter);
        }

        int colPos = table.getColumnPos(parameter);
        ArrayList<String> values = extractColumn(table.getData(), colPos);
        
        switch (column.getType()){
            case "INT":
                ArrayList<Integer> intValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }

                    try{
                        intValues.add(Integer.parseInt(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE INTEGER FOUND: " + value);
                    }
                }

                int total = 0;
                for(int value : intValues){
                    total += value;
                }
                return Integer.toString(total);

            case "DOUBLE":
                ArrayList<Double> doubleValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }

                    try{
                        doubleValues.add(Double.parseDouble(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE DOUBLE FOUND: " + value);
                    }
                }

                double dTotal = 0;
                for(double value : doubleValues){
                    dTotal += value;
                }
                return Double.toString(dTotal);
        }

        throw new SQLSyntaxException("UNSUPPORTED FUNCTION FOR " + column.getType() + " DATA TYPE");
    }

    public static String handleAVG(String parameter, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(parameter);

        if(column == null){
            throw new SQLSyntaxException("UNDEFINED COLUMN " + parameter);
        }

        int colPos = table.getColumnPos(parameter);
        ArrayList<String> values = extractColumn(table.getData(), colPos);

        switch (column.getType()){
            case "INT":
                ArrayList<Integer> intValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }

                    try {
                        intValues.add(Integer.parseInt(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE INTEGER FOUND: " + value);
                    }
                }

                int total = 0;
                for(int value : intValues){
                    total += value;
                }

                double avg = (double) total / intValues.size();
                return Double.toString(avg);
            case "DOUBLE":
                ArrayList<Double> doubleValues = new ArrayList<>();
                for(String value : values){
                    if(value.isEmpty()){
                        continue;
                    }

                    try {
                        doubleValues.add(Double.parseDouble(value));
                    } catch (NumberFormatException e){
                        throw new NumberFormatException("UNPARSEABLE DOUBLE FOUND: " + value);
                    }
                }

                double dtotal = 0;
                for(double value : doubleValues){
                    dtotal += value;
                }

                dtotal /= doubleValues.size();
                return Double.toString(dtotal);
        }

        throw new SQLSyntaxException("UNSUPPORTED FUNCTION FOR " + column.getType() + " DATA TYPE");
    }

    public static ArrayList<String> handleDISTINCT(String parameter, Table table) throws SQLSyntaxException {
        Column column = table.getColumnByName(parameter);

        if(column == null){
            throw new SQLSyntaxException("UNDEFINED COLUMN " + parameter);
        }

        int colPos = table.getColumnPos(parameter);
        ArrayList<String> values = extractColumn(table.getData(), colPos);
        ArrayList<String> distinctValues = new ArrayList<>();
        boolean distinct = true;

        for(int i = 0; i < values.size(); i++){
            String value = values.get(i);
            for(int j = 0; j < values.size(); j++){
                if(j == i){
                    continue;
                }

                String otherValue = values.get(j);
                if(otherValue.equals(value)){
                    distinct = false;
                    break;
                }
            }

            if(distinct){
                distinctValues.add(value);
            }
            distinct = true;
        }

        return distinctValues;
    }

    public static String handleCOUNT(String parameter, Table table) throws SQLSyntaxException {
        if(parameter.equals("*")){
            return Integer.toString(table.getData().size());
        }

        Column column = table.getColumnByName(parameter);
        if(column == null){
            throw new SQLSyntaxException("UNDEFINED COLUMN " + parameter);
        }

        int colPos = table.getColumnPos(parameter);
        int count = 0;
        for(ArrayList<Object> row : table.getData()){
            String value = row.get(colPos).toString();
            if(value != null && !value.isBlank()){
                count++;
            }
        }

        return Integer.toString(count);
    }

    /**
     * Esta es una funcion de ordenamiento que admite genericos, ordena usando insert sort
     * @param array Un array genérico
     * @return El array proporcionado ordenado de mayor a menor
     * @param <T>
     */
    public static <T extends Comparable<T>> ArrayList<String> sortArray(ArrayList<T> array) {
        ArrayList<String> result = new ArrayList<>();
        int pivote;

        for(int i = 0; i < array.size(); i++){
            pivote = i;
            T temp = array.get(i);
            for(int j = pivote; j < array.size(); j++){
                T value = array.get(j);
                if(temp.compareTo(value) < 0){
                    temp = value;
                    pivote = j;
                }
            }

            array.remove(pivote);
            array.add(i, temp);
        }

        for(T value : array){
            result.add(value.toString());
        }

        return result;
    }

    /**
     * Esta funcion me permite transformar una matriz de objects a una matriz de string para la función
     * función de arriba
     * @param data Los datos recuperados de la tabla después del WHERE
     * @param columnPos La posicion de la columna que contiene los datos a recuperar
     * @return El mismo array pero como genérico T
     */
    public static ArrayList<String> extractColumn(ArrayList<ArrayList<Object>> data, int columnPos) {
        ArrayList<String> columnData = new ArrayList<>();
        for (ArrayList<Object> row : data) {
            try {
                String value = row.get(columnPos).toString();
                columnData.add(value);
            } catch (IndexOutOfBoundsException e) {
                columnData.add("");
            }
        }
        return columnData;
    }

    /*
      ////////////////////////////
      //// OTRAS FUNCIONES ///////
      ////////////////////////////
      Estas funciones no contemplan el uso de columnas de la tabla ni de
      group functions
     */

    /**
     * Todas las funciones solamente reciben un parámetro, pero pueden ser concatenadas, por lo que esta función
     * parsea las funciones y busca si estas pueden estar concatenadas o no.
     * @param line String sin parsear que contiene a las funciones
     * @return Un array con todas las funciones reconocidas, solamente la ultima función del array tiene su parámetro
     * definido
     * @throws SQLSyntaxException si algún nombre parseado no pertenece a una función definida
     */
    public static String parseFunctions(String line) throws SQLSyntaxException {
        ArrayList<Function> foundFunctions = new ArrayList<>();
        Function temp;
        String result = "";

        do {
            String functionName = line.substring(0, line.indexOf("[")).trim();
            if(!Analyzer.getNumericFunctions().contains(functionName)){
                throw new SQLSyntaxException("UNSUPORTED FUNCTION " + functionName);
            }

            temp = new Function(functionName);
            foundFunctions.add(temp);
            line = line.substring(line.indexOf("[") + 1);
        } while (line.contains("["));

        for(int i = foundFunctions.size() - 1; i >= 0; i--){
            temp = foundFunctions.get(i);
            String added = line.substring(0, line.indexOf("]")).trim();
            String parameter = result + added;
            line = line.substring(line.indexOf(added)+added.length()+1).trim();

            ArrayList<String> operators = new ArrayList<>(Analyzer.getOperators());
            operators.add("(");
            operators.add(")");

            // hay que checar si el parámetro es una operacion aritmetica
            ArrayList<String> tokens = Utils.splitByWords(parameter, operators, true);
            if(tokens.size() > 1){
                tokens = Where.infixToPostfix(tokens);
                Tree.Node root = Where.createTree(tokens);
                parameter = Where.evaluateSubTree(root);
            }

            temp.setParameter(parameter);
            result = evaluateFunction(temp);
            if(i >= 1){
                foundFunctions.get(i-1).setParameter(result);
            }
        }

        return result;
    }

    public static String evaluateFunction(Function function) throws SQLSyntaxException {
        String result;

        switch (function.getName()) {
            case "UPPER":
                result = handleUPPER(function.getParameter());
                break;
            case "LOWER":
                result = handleLOWER(function.getParameter());
                break;
            case "FLOOR":
                result = handleFLOOR(function.getParameter());
                break;
            case "CEIL":
                result = handleCEIL(function.getParameter());
                break;
            case "ROUND":
                result = handleROUND(function.getParameter());
                break;
            case "RAND":
                result = handleRAND(function.getParameter());
                break;
            default:
                throw new SQLSyntaxException("UNSUPPORTED FUNCTION IN STATEMENT");
        }

        return result;
    }

    public static String handleUPPER(String value) {
        if(value.startsWith("'") && value.endsWith("'")){
            value = value.substring(1, value.length()-1);
        }

        return value.toUpperCase();
    }

    public static String handleLOWER(String value) {
        if(value.startsWith("'") && value.endsWith("'")){
            value = value.substring(1, value.length()-1);
        }

        return value.toLowerCase();
    }

    public static String handleFLOOR(String value) throws SQLSyntaxException {
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new SQLSyntaxException("UNPARSEABLE NUMBER FOUND: " + value);
        }

        int iValue = (int) Math.floor(doubleValue);
        return Integer.toString(iValue);
    }

    public static String handleCEIL(String value) throws SQLSyntaxException {
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new SQLSyntaxException("UNPARSEABLE NUMBER FOUND: " + value);
        }

        int iValue = (int) Math.ceil(doubleValue);
        return Integer.toString(iValue);
    }

    public static String handleROUND(String value) throws SQLSyntaxException {
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new SQLSyntaxException("UNPARSEABLE NUMBER FOUND: " + value);
        }

        int iValue = (int) Math.round(doubleValue);
        return Integer.toString(iValue);
    }

    public static String handleRAND(String maxValue) throws SQLSyntaxException {
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(maxValue);
        } catch (NumberFormatException e) {
            throw new SQLSyntaxException("UNPARSEABLE NUMBER FOUND: " + maxValue);
        }

        int iValue = (int) (Math.random() * (doubleValue + 1));
        return Integer.toString(iValue);
    }
}
