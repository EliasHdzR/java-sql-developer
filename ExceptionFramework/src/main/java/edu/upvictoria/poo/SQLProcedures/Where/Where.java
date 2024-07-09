package edu.upvictoria.poo.SQLProcedures.Where;

import edu.upvictoria.poo.Analyzer;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;
import java.util.ArrayList;
import java.util.Stack;

public class Where {

    public static int getHierarchy(String keyword){
        switch(keyword){
            case "*": case "/": case "DIV": case "%":
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
                case "+": case "-": case "*": case "/": case "%": case "DIV":
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
        Analyzer analyzer = new Analyzer();
        ArrayList<String> operators = analyzer.getOperators();
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

    public static ArrayList<ArrayList<Object>> evaluateTree(Tree.Node root, ArrayList<ArrayList<Object>> data, Table table) throws SQLSyntaxException {
        ArrayList<ArrayList<Object>> results = new ArrayList<>();

        for(ArrayList<Object> row : data) {
            if(evaluateRowInTree(root, row, table)){
                results.add(row);
            }
        }

        return results;
    }

    public static boolean evaluateRowInTree(Tree.Node root, ArrayList<Object> row, Table table) throws SQLSyntaxException{
        if(root == null) return true;

        Analyzer analyzer = new Analyzer();
        ArrayList<String> operators = analyzer.getOperators();

        if(operators.contains(root.value)){
            boolean left = evaluateRowInTree(root.left, row, table);
            boolean right = evaluateRowInTree(root.right, row, table);

            switch(root.value){
                case "AND":
                    return left && right;
                case "OR":
                    return left || right;
                default:
                    throw new UnsupportedOperationException();
            }
        } else {
            return evaluateCondition(root.value,row, table);
        }
    }

    public static boolean evaluateCondition(String condition, ArrayList<Object> row, Table table) throws SQLSyntaxException {
        Object[] conditionValues = condition.split(" ");

        int columnPos = table.getColumnPos((String)conditionValues[0]);
        String columnType = table.getColumns().get(columnPos).getType();

        String rowValue = row.get(columnPos).toString();
        String operator = (String) conditionValues[1];
        Double rowDoubleValue = null, columnDoubleValue = null;
        Integer rowIntegerValue = null, columnIntegerValue = null;

        try {
            if(columnType.equals("INT")){
                rowIntegerValue = Integer.parseInt(rowValue);
                columnIntegerValue = Integer.parseInt(conditionValues[2].toString());
            }

            if(columnType.equals("DOUBLE") || columnType.equals("FLOAT")){
                rowDoubleValue = Double.parseDouble(rowValue);
                columnDoubleValue = Double.parseDouble(conditionValues[2].toString());
            }
        } catch (NumberFormatException e) {
            throw new SQLSyntaxException("INCORRECT DATA TYPE GIVEN");
        }

        switch(operator){
            case "=":
                return rowValue.equals(conditionValues[2]);

            case "<":
                if(rowDoubleValue != null){
                    return rowDoubleValue < columnDoubleValue;
                }

                if(rowIntegerValue != null){
                   return rowIntegerValue < columnIntegerValue;
                }
                throw new UnsupportedOperationException();

            case ">":
                if(rowDoubleValue != null){
                    return rowDoubleValue > columnDoubleValue;
                }

                if(rowIntegerValue != null){
                    return rowIntegerValue > columnIntegerValue;
                }
                throw new UnsupportedOperationException();

            case "<=":
                if(rowDoubleValue != null){
                    return rowDoubleValue <= columnDoubleValue;
                }

                if(rowIntegerValue != null){
                    return rowIntegerValue <= columnIntegerValue;
                }
                throw new UnsupportedOperationException();

            case ">=":
                if(rowDoubleValue != null){
                    return rowDoubleValue >= columnDoubleValue;
                }

                if(rowIntegerValue != null){
                    return rowIntegerValue >= columnIntegerValue;
                }
                throw new UnsupportedOperationException();

            case "!=":
                return !rowValue.equals(conditionValues[2]);
            default:
                throw new UnsupportedOperationException();
        }
    }
}
