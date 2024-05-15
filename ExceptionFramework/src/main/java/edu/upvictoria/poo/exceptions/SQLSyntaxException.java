package edu.upvictoria.poo.exceptions;

import java.io.IOException;

public class SQLSyntaxException extends IOException {
    public SQLSyntaxException(String message) {
        super(message);
    }
}
