package edu.upvictoria.poo;

public class Column {
    private final String name;
    private final String type;
    private final String constraint;

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
        return constraint;
    }
}
