package edu.upvictoria.poo;

import java.util.ArrayList;
import java.util.Stack;

public class Where {

    public static int getHierarchy(String keyword){
        switch(keyword){
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
                case "AND":
                case "OR":
                case "=":
                case ">":
                case ">=":
                case "<":
                case "<=":
                case "!=":
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

        for(String token : postfix) {
            System.out.println(token);
        }

        return postfix;
    }
}
