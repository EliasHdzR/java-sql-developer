package edu.upvictoria.poo.exceptions;

import java.io.IOException;

public class DatabaseNotSetException extends IOException {

    public DatabaseNotSetException (String message) {
        super(message);
    }

}
