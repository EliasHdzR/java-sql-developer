package edu.upvictoria.poo.exceptions;

import java.sql.SQLSyntaxErrorException;

public class DuplicateEntryException extends SQLSyntaxErrorException {

    public DuplicateEntryException(String message) {
        super(message);
    }

}
