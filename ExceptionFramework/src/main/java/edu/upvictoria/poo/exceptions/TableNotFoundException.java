package edu.upvictoria.poo.exceptions;

import java.sql.SQLException;

public class TableNotFoundException extends SQLException {

    public TableNotFoundException(String message) {
        super(message);
    }
}
