package edu.upvictoria.poo.DMLProcedures.Where;

public class Tree {

    public static class Node {
        String value;
        Node left, right;

        Node(String data){
            value = data;
            left = right = null;
        }

        public Node(Node other) {
            if (other != null) {
                this.value = other.value;
                this.left = (other.left != null) ? new Node(other.left) : null;
                this.right = (other.right != null) ? new Node(other.right) : null;
            }
        }
    }
}
