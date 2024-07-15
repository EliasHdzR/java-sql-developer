package edu.upvictoria.poo.exceptions;

import java.sql.SQLException;

public class DataTypeNotFoundException extends SQLException {

    public DataTypeNotFoundException (String message) {
        super(message);
    }

}
