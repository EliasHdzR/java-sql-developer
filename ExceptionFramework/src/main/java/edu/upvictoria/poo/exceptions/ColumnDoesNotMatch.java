package edu.upvictoria.poo.exceptions;

import java.io.IOException;

public class ColumnDoesNotMatch extends IOException {
    public ColumnDoesNotMatch (String message) {
        super(message);
    }
}
