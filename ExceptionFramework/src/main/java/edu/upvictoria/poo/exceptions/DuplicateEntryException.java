package edu.upvictoria.poo.exceptions;

import java.io.IOException;

public class DuplicateEntryException extends IOException {

    public DuplicateEntryException(String message) {
        super(message);
    }

}
