package edu.upvictoria.poo.DMLProcedures.Where;

import edu.upvictoria.poo.Analyzer;
import edu.upvictoria.poo.Column;
import edu.upvictoria.poo.Function;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.util.ArrayList;
import java.util.Stack;

public class Where {

    public static int getHierarchy(String keyword){
        switch(keyword){
            case "*": case "/": case "DIV": case "%": case "MOD":
                return 5;
            case "+": case "-":
                return 4;
            case "=": case "<": case ">": case ">=": case "<=": case "!=":
                return 3;
            case "AND":
                return 2;
            case "OR":
                return 1;
        }

        return 0;
    }

    public static ArrayList<String> infixToPostfix(ArrayList<String> tokens) {
        Stack<String> stack = new Stack<>();
        ArrayList<String> postfix = new ArrayList<>();
        tokens.add(tokens.size(),")");
        stack.push("(");

        for(String token : tokens) {
            switch (token) {
                case "(":
                    stack.push(token);
                    break;
                case ")":
                    while (!stack.isEmpty() && !stack.peek().equals("(")) {
                        postfix.add(stack.pop());
                    }
                    stack.pop();
                    break;
                case "AND": case "OR": case "=": case ">": case ">=": case "<": case "<=": case "!=":
                case "+": case "-": case "*": case "/": case "%": case "DIV": case "MOD":
                    while (!stack.isEmpty() && getHierarchy(stack.peek()) >= getHierarchy(token)) {
                        postfix.add(stack.pop());
                    }
                    stack.push(token);
                    break;
                default:
                    postfix.add(token);
                    break;
            }
        }

        while (!stack.isEmpty() && !stack.peek().equals("(")) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

    public static Tree.Node createTree(ArrayList<String> tokens) {
        ArrayList<String> operators = new ArrayList<>(Analyzer.getComparators());
        operators.addAll(Analyzer.getOperators());
        Stack<Tree.Node> stack = new Stack<>();

        for (String token : tokens) {
            if (operators.contains(token)) {
                Tree.Node right = stack.pop();
                Tree.Node left = stack.pop();
                Tree.Node node = new Tree.Node(token);
                node.left = left;
                node.right = right;
                stack.push(node);
            } else {
                stack.push(new Tree.Node(token));
            }
        }

        return stack.pop();
    }

    public static ArrayList<ArrayList<Object>> evaluateTree(Tree.Node root, ArrayList<ArrayList<Object>> data, Table table) throws SQLSyntaxException, UnsupportedOperationException {
        if(!Analyzer.getComparators().contains(root.value)){
            throw new SQLSyntaxException("MALFORMED WHERE STATEMENT");
        }

        ArrayList<ArrayList<Object>> results = new ArrayList<>();
        Tree.Node auxRoot = new Tree.Node(root);
        for(ArrayList<Object> row : data) {
            if(evaluateRowInTree(root, row, table)){
                results.add(row);
            }
            root = new Tree.Node(auxRoot);
        }

        return results;
    }

    private static boolean evaluateRowInTree(Tree.Node root, ArrayList<Object> row, Table table) throws UnsupportedOperationException, SQLSyntaxException {
        if(root == null) return true;

        if(Analyzer.getOperators().contains(root.left.value)){
            root.left.value = evaluateSubTree(root.left, row, table);
        }

        if(Analyzer.getOperators().contains(root.right.value)){
            root.right.value = evaluateSubTree(root.right, row, table);
        }

        if(Function.analyzeNode(root.left.value)){
            root.left.value = Function.parseFunctions(root.left.value, row, table);
        }

        if(Function.analyzeNode(root.right.value)){
            root.right.value = Function.parseFunctions(root.right.value, row, table);
        }

        if(!Analyzer.getComparators().contains(root.left.value) && !Analyzer.getComparators().contains(root.right.value)
            && !Analyzer.getOperators().contains(root.left.value) && !Analyzer.getOperators().contains(root.right.value)){
            return evaluateCondition(root.value, root.left.value, root.right.value, row, table);
        }

        boolean left = evaluateRowInTree(root.left, row, table);
        boolean right = evaluateRowInTree(root.right, row, table);

        switch(root.value){
            case "AND":
                return left && right;
            case "OR":
                return left || right;
            default:
                throw new UnsupportedOperationException("UNSUPPORTED OPERATION");
        }
    }

    private static boolean evaluateCondition(String comparator, String left, String right, ArrayList<Object> row, Table table) throws SQLSyntaxException, UnsupportedOperationException {
        Column leftCol = null, rightCol = null;
        if(!left.startsWith("'") && !left.endsWith("'")){
            leftCol = table.getColumnByName(left);
        }

        if(!right.startsWith("'") && !right.endsWith("'")){
            rightCol = table.getColumnByName(right);
        }

        String leftValue = getString(left, row, table, leftCol);
        String rightValue = getString(right, row, table, rightCol);

        if (leftValue.equals("NULL") || rightValue.equals("NULL")) {
            return evaluateNullCondition(comparator, leftValue, rightValue);
        }

        double leftDouble, rightDouble;

        switch(comparator){
            case "=":
                return leftValue.equals(rightValue);
            case "<":
                try {
                    leftDouble = Double.parseDouble(leftValue);
                    rightDouble = Double.parseDouble(rightValue);
                    return leftDouble < rightDouble;
                } catch (NumberFormatException e) {
                    throw new SQLSyntaxException("UNDEFINED VALUE");
                }

            case ">":
                try {
                    leftDouble = Double.parseDouble(leftValue);
                    rightDouble = Double.parseDouble(rightValue);
                    return leftDouble > rightDouble;
                } catch (NumberFormatException e) {
                    throw new SQLSyntaxException("UNDEFINED VALUE");
                }

            case "<=":
                try {
                    leftDouble = Double.parseDouble(leftValue);
                    rightDouble = Double.parseDouble(rightValue);
                    return leftDouble <= rightDouble;
                } catch (NumberFormatException e) {
                    throw new SQLSyntaxException("UNDEFINED VALUE");
                }

            case ">=":
                try {
                    leftDouble = Double.parseDouble(leftValue);
                    rightDouble = Double.parseDouble(rightValue);
                    return leftDouble >= rightDouble;
                } catch (NumberFormatException e) {
                    throw new SQLSyntaxException("UNDEFINED VALUE");
                }

            case "!=":
                return !leftValue.equals(rightValue);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static String getString(String nodeValue, ArrayList<Object> row, Table table, Column col) throws SQLSyntaxException {
        if(nodeValue.startsWith("'") && nodeValue.endsWith("'")){
            return nodeValue.substring(1, nodeValue.length() - 1);
        }

        // jajaj no se entiede que chingados ando haciendo
        try {
            double valueDouble = Double.parseDouble(nodeValue);
            return Double.toString(valueDouble);
        } catch (NumberFormatException e) {
            try {
                if(col != null){
                    String value = row.get(table.getColumnPos(col.getName())).toString();
                    double valueDouble = Double.parseDouble(value);
                    return Double.toString(valueDouble);
                } else {
                    if(nodeValue.equals("NULL")){
                        return nodeValue;
                    }
                    throw new SQLSyntaxException("UNDEFINED COLUMN: " + nodeValue);
                }
            } catch (IndexOutOfBoundsException e1){
                return "NULL";
            } catch (NumberFormatException e2){
                String value = row.get(table.getColumnPos(col.getName())).toString();
                if(value.isEmpty()){
                    return "NULL";
                }
                return value;
            }

        }
    }

    private static boolean evaluateNullCondition(String comparator, String left, String right){
        switch (comparator){
            case "=":
                return left.equals(right);
            case "!=":
                return !left.equals(right);
            default:
                return false;
        }
    }

    public static String evaluateSubTree(Tree.Node root, ArrayList<Object> row, Table table) throws SQLSyntaxException, UnsupportedOperationException {
        if (root == null) {
            return "NULL";
        }

        if(root.left == null || root.right == null){
            if(Function.analyzeNode(root.value)){
                return Function.parseFunctions(root.value, row, table);
            } else {
                return root.value;
            }
        }

        Double aux = null;
        root.left.value = evaluateSubTree(root.left, row, table);
        root.right.value = evaluateSubTree(root.right, row, table);

        if(!Analyzer.getOperators().contains(root.left.value) && !Analyzer.getOperators().contains(root.right.value)){
            aux = evaluateOperation(root.value, root.left.value, root.right.value, row, table);
        }

        if(aux != null){
            if (aux == aux.intValue()) {
                int value = aux.intValue();
                return Integer.toString(value);
            }
            return aux.toString();
        } else {
            return "NULL";
        }
    }

    private static Double evaluateOperation(String operator, String left, String right, ArrayList<Object> row, Table table) throws SQLSyntaxException, UnsupportedOperationException {
        Column leftCol = table.getColumnByName(left);
        Column rightCol = table.getColumnByName(right);
        Double leftValue = getValue(left, row, table, leftCol);
        Double rightValue = getValue(right, row, table, rightCol);
        double value;

        if(leftValue == null || rightValue == null){
            return null;
        }

        switch (operator){
            case "DIV":
                if(rightValue == 0){
                    throw new ArithmeticException("DIVISION BY ZERO");
                }
                value = leftValue / rightValue;
                return Math.floor(value);
            case "/":
                if(rightValue == 0){
                    throw new ArithmeticException("DIVISION BY ZERO");
                }
                return leftValue / rightValue;
            case "-":
                return leftValue - rightValue;
            case "+":
                return leftValue + rightValue;
            case "*":
                return leftValue * rightValue;
            case "%": case "MOD":
                return leftValue % rightValue;
            default:
                throw new UnsupportedOperationException("OPERATION NOT SUPPORTED");
        }
    }

    public static Double getValue(String nodeValue, ArrayList<Object> row, Table table, Column col) throws SQLSyntaxException {
        try {
            return Double.parseDouble(nodeValue);
        } catch (NumberFormatException e){
            try {
                if(col != null){
                    String aux = row.get(table.getColumnPos(col.getName())).toString();

                    if(aux.isEmpty()){
                        return null;
                    }

                    return Double.parseDouble(aux);
                }
            } catch (IndexOutOfBoundsException e1){
                return null;
            } catch (NumberFormatException e2){
                throw new SQLSyntaxException("OPERATION NOT SUPPORTED FOR " + col.getType() + " DATA TYPE");
            }
        }

        throw new SQLSyntaxException("UNRECOGNIZED VALUES IN WHERE STATEMENT: " + nodeValue);
    }


    /**
     * ///////////////////////////////////////////////////////////////
     * ///////////////////////////////////////////////////////////////
     * ///// FUNCTIONS FOR STATEMENTS OTHER THAN SELECT OR WHERE /////
     * ///////////////////////////////////////////////////////////////
     * ///////////////////////////////////////////////////////////////
     * These functions do not contemplate the usage of table fields, only literals.
     */

    public static String evaluateSubTree(Tree.Node root) throws SQLSyntaxException, UnsupportedOperationException {
        if (root == null) {
            return "NULL";
        }

        if(root.left == null || root.right == null){
            if(Function.analyzeNode(root.value)){
                return Function.parseFunctions(root.value);
            } else {
                return root.value;
            }
        }

        Double aux = null;
        root.left.value = evaluateSubTree(root.left);
        root.right.value = evaluateSubTree(root.right);

        if(!Analyzer.getOperators().contains(root.left.value) && !Analyzer.getOperators().contains(root.right.value)){
            aux = evaluateOperation(root.value, root.left.value, root.right.value);
        }

        if(aux != null){
            if (aux == aux.intValue()) {
                int value = aux.intValue();
                return Integer.toString(value);
            }
            return aux.toString();
        } else {
            return "NULL";
        }
    }

    private static double evaluateOperation(String operator, String left, String right) throws SQLSyntaxException, UnsupportedOperationException {
        Double leftValue, rightValue;
        try {
            leftValue = Double.parseDouble(left);
            rightValue = Double.parseDouble(right);
        } catch (NumberFormatException e){
            throw new SQLSyntaxException("NUMERIC VALUES ONLY");
        }

        switch (operator){
            case "DIV":
                if(rightValue == 0){
                    throw new ArithmeticException("DIVISION BY ZERO");
                }
                double value = leftValue / rightValue;
                return Math.floor(value);
            case "/":
                if(rightValue == 0){
                    throw new ArithmeticException("DIVISION BY ZERO");
                }
                return leftValue / rightValue;
            case "-":
                return leftValue - rightValue;
            case "+":
                return leftValue + rightValue;
            case "*":
                return leftValue * rightValue;
            case "%": case "MOD":
                return leftValue % rightValue;
            default:
                throw new UnsupportedOperationException("OPERATION NOT SUPPORTED");
        }
    }
}
