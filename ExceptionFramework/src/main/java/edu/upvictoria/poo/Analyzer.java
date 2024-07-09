package edu.upvictoria.poo;

import edu.upvictoria.poo.SQLProcedures.Insertion;
import edu.upvictoria.poo.SQLProcedures.Selection;
import edu.upvictoria.poo.exceptions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.io.File;

public class Analyzer {
    private final ArrayList<String> keywords = new ArrayList<>();
    private final ArrayList<String> dataTypes = new ArrayList<>();
    private final ArrayList<String> operators = new ArrayList<>();
    private final ArrayList<String> constraints = new ArrayList<>();

    private final SQL sql = new SQL();
    private Database database = new Database();

    private final Insertion insertion = new Insertion();
    private final Selection selection = new Selection();
    
    public Analyzer(){
        keywords.add("USE");
        keywords.add("SHOW TABLES");
        keywords.add("CREATE DATABASE");
        keywords.add("CREATE TABLE");
        keywords.add("DROP TABLE");
        keywords.add("INSERT INTO");
        keywords.add("DELETE FROM");
        keywords.add("UPDATE");
        keywords.add("SELECT");

        operators.add("DIV");
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
        operators.add("*");
        operators.add("+");
        operators.add("-");
        operators.add("/");
        operators.add("%");

        constraints.add("NOT NULL");
        constraints.add("PRIMARY KEY");
        constraints.add("FOREIGN KEY");

        dataTypes.add("VARCHAR");
        dataTypes.add("CHAR");
        dataTypes.add("BOOLEAN");
        dataTypes.add("DATE");
        dataTypes.add("INT");
        dataTypes.add("DOUBLE");
    }

    public void analyzeSyntax(String line) throws Exception {
        line = line.replaceAll("\n"," ");
        boolean found = false;

        for(String keyword : keywords){
            if (line.startsWith(keyword)) {
                try {
                    //lets get funky
                    switch (keyword) {
                        case "USE":
                            File dbFile = sql.handleUse(line, keyword);
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
                            sql.handleCreateTable(line, keyword, this.database);
                            refreshDB(this.database.getDbFile());
                            return;

                        case "CREATE DATABASE":
                            sql.handleCreateDatabase(line, keyword);
                            return;

                        case "DROP TABLE":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            sql.handleDropTable(line, keyword, this.database);
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
                            sql.handleDeleteFrom(line, keyword, database);
                            refreshDB(this.database.getDbFile());
                            return;

                        case "UPDATE":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            sql.handleUpdate(line, keyword, this.database);
                            refreshDB(this.database.getDbFile());
                            return;

                        case "SELECT":
                            if (database.getDbFile() == null) {
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

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

                } catch (FileSystemException e) {
                    throw new FileSystemException(e.getMessage());

                } catch (DataTypeNotFoundException e) {
                    throw new DataTypeNotFoundException(e.getMessage());

                } catch (InsuficientDataProvidedException e) {
                    throw new InsuficientDataProvidedException(e.getMessage());

                } catch (ColumnDoesNotMatch e) {
                    throw new ColumnDoesNotMatch(e.getMessage());

                } catch (SQLSyntaxException e) {
                    throw new SQLSyntaxException(e.getMessage());

                } catch (IOException e) {
                    throw new IOException(e.getMessage());

                } catch (Exception e) {
                    throw new Exception("AN ERROR OCURRED WHILE EXECUTING COMMAND: " + e.getMessage());
                }
            }
        }

        if(!found){
            throw new IOException("NOT RECOGNIZABLE KEYWORDS");
        }
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public ArrayList<String> getDataTypes() {
        return dataTypes;
    }

    public ArrayList<String> getConstraints() {
        return constraints;
    }

    public ArrayList<String> getOperators() {
        return operators;
    }

    public void refreshDB(File file) throws FileSystemException{
        this.database = new Database();
        this.database.setDbFile(file);
        this.database.retrieveTables();
    }
}
