package edu.upvictoria.poo;

public class Tree {
    Node root;

    public static class Node {
        String value;
        Node left, right;

        Node(String data){
            value = data;
            left = right = null;
        }
    }

    public static Double getDouble(String str) throws NumberFormatException{
        return Double.parseDouble(str);
    }

    public static void printTree(Node node) {
        if (node == null) {
            return;
        }
        printTree(node.left);
        System.out.print(node.value + " ");
        printTree(node.right);
    }
}
