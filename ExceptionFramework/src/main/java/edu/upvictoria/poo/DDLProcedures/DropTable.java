package edu.upvictoria.poo.DDLProcedures;

import edu.upvictoria.poo.Database;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystemException;

public class DropTable {
    private String query;
    private Database database;
    private final String keyword = "DROP TABLE";

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void handle() throws IOException {
        String givenName = Utils.clean(query, keyword);

        for (Table table : database.getTables()) {
            if (table.getTableName().equals(givenName)) {
                if (!table.getTableFile().delete()) {
                    throw new FileSystemException("COULD NOT DELETE THE FILE AT: " + table.getTableFile().getAbsolutePath());
                } else {
                    return;
                }
            }
        }

        throw new FileNotFoundException("TABLE NOT FOUND");
    }
}
