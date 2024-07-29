package edu.upvictoria.poo;

import edu.upvictoria.poo.DMLProcedures.Where.Tree;

public class Column {
    private final String name;
    private final String type;
    private final String constraint;
    private String alias = null;
    private Tree.Node operation;

    public Column(String name, String type, String constraint) {
        this.name = name;
        this.type = type;
        this.constraint = constraint;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getConstraint() {
        if(constraint == null){
            return "";
        }
        return constraint;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setOperation(Tree.Node operation) {
        this.operation = operation;
    }

    public Tree.Node getOperation() {
        return operation;
    }
}
