package edu.upvictoria.poo;

import edu.upvictoria.poo.DDLProcedures.CreateTable;
import edu.upvictoria.poo.DDLProcedures.DropTable;
import edu.upvictoria.poo.DMLProcedures.Deletion;
import edu.upvictoria.poo.DMLProcedures.Insertion;
import edu.upvictoria.poo.DMLProcedures.Selection;
import edu.upvictoria.poo.DMLProcedures.Update;
import edu.upvictoria.poo.exceptions.*;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class Analyzer {
    private static final ArrayList<String> keywords = new ArrayList<>(List.of(
            "USE", "SHOW TABLES", "CREATE TABLE", "DROP TABLE",
            "INSERT INTO", "DELETE FROM", "UPDATE", "SELECT",
            "FROM", "WHERE", "SET"
    ));

    private static final ArrayList<String> dataTypes = new ArrayList<>(List.of(
            "VARCHAR","CHAR","BOOLEAN","DATE","INT","DOUBLE"
    ));

    private static ArrayList<String> operators;

    private static final ArrayList<String> constraints = new ArrayList<>(List.of(
            "NOT NULL","PRIMARY KEY","FOREIGN KEY"
    ));

    private Database database = new Database();

    private final Insertion insertion = new Insertion();
    private final Selection selection = new Selection();
    private final Deletion deletion = new Deletion();
    private final Update update = new Update();
    private final CreateTable creator = new CreateTable();
    private final DropTable dropper = new DropTable();

    /**
     * Analiza la línea en busca de la KEYWORD inicial, después efectua diferentes acciones dependiendo de la Keyword.
     * Por cada Keyword:
     *     1.
     * @param line
     * @throws Exception
     */
    public void analyzeSyntax(String line) throws Exception {
        line = line.replaceAll("\n"," ");
        ArrayList<String> validKeywords = new ArrayList<>();

        for(String keyword : keywords){
            if (line.startsWith(keyword)) {
                try {
                    // lets get funky
                    switch (keyword) {
                        case "USE":
                            validKeywords.add("USE");
                            Utils.hasValidKeywords(line, validKeywords);
                            File dbFile = handleUse(line, keyword);
                            refreshDB(dbFile);
                            return;

                        case "SHOW TABLES":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            this.database.printTableNames();
                            return;

                        case "CREATE TABLE":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            creator.setDatabase(database);
                            creator.setQuery(line);
                            creator.handle();
                            refreshDB(this.database.getDbFile());
                            return;

                        case "DROP TABLE":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            dropper.setDatabase(database);
                            dropper.setQuery(line);
                            dropper.handle();
                            refreshDB(this.database.getDbFile());
                            return;

                        case "INSERT INTO":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            insertion.setDatabase(database);
                            insertion.setQuery(line);
                            insertion.handle();
                            refreshDB(this.database.getDbFile());
                            return;

                        case "DELETE FROM":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            deletion.setDatabase(database);
                            deletion.setQuery(line);
                            deletion.handle();
                            refreshDB(this.database.getDbFile());
                            return;

                        case "UPDATE":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            update.setDatabase(database);
                            update.setQuery(line);
                            update.handle();
                            refreshDB(this.database.getDbFile());
                            return;

                        case "SELECT":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            validKeywords.add("SELECT");
                            validKeywords.add("FROM");
                            if(line.contains("WHERE")){
                                validKeywords.add("WHERE");
                            }

                            Utils.hasValidKeywords(line, validKeywords);

                            refreshDB(this.database.getDbFile());
                            selection.setDatabase(database);
                            selection.setQuery(line);
                            selection.handle();
                            refreshDB(this.database.getDbFile());
                            return;
                    }

                } catch (StringIndexOutOfBoundsException e) {
                    throw new StringIndexOutOfBoundsException("ERROR WHILE PARSING: MISSING EXPRESSIONS");

                } catch (FileNotFoundException e) {
                    throw new FileNotFoundException("FILE NOT FOUND: " + e.getMessage());

                } catch (NoSuchFileException e) {
                    throw new NoSuchFileException("NOT A DATABASE: " + e.getMessage());
                }
            }
        }
    }

    public static ArrayList<String> getKeywords() {
        return keywords;
    }

    public static ArrayList<String> getDataTypes() {
        return dataTypes;
    }

    public static ArrayList<String> getConstraints() {
        return constraints;
    }

    public static ArrayList<String> getOperators() {
        operators = new ArrayList<>();
        operators.add("AND");
        operators.add("OR");
        operators.add("<=");
        operators.add(">=");
        operators.add("=");
        operators.add("<");
        operators.add(">");
        operators.add("!=");
        operators.add("(");
        operators.add(")");
        return operators;
    }

    public void refreshDB(File file) throws FileSystemException{
        this.database = new Database();
        this.database.setDbFile(file);
        this.database.retrieveTables();
    }

    public File handleUse(String line, String keyword) throws FileSystemException, StringIndexOutOfBoundsException, FileNotFoundException, DatabaseNotSetException {
        String givenPath = Utils.clean(line,keyword);

        File database = new File(Paths.get("").toAbsolutePath().resolve(givenPath).toString());
        if(!database.exists()){
            throw new FileNotFoundException();
        }

        if(!database.isDirectory()){
            throw new NotDirectoryException("GIVEN PATH IS NOT A DIRECTORY: " + givenPath);
        }

        if(!database.getName().endsWith("_DB")){
            throw new NoSuchFileException(givenPath);
        }

        if(!database.canWrite() &&  !database.canRead()){
            throw new DatabaseNotSetException("NOT PERMISSIONS GIVEN");
        }
        return database;
    }
}
