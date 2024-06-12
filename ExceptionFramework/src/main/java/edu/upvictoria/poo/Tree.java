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
}
