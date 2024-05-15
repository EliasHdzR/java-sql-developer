package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.ColumnDoesNotMatch;
import edu.upvictoria.poo.exceptions.DataTypeNotFoundException;
import edu.upvictoria.poo.exceptions.DatabaseNotSetException;
import edu.upvictoria.poo.exceptions.InsuficientDataProvidedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.io.File;

public class Analyzer {
    private final ArrayList<String> keywords = new ArrayList<>();
    private final ArrayList<String> dataTypes = new ArrayList<>();
    private final ArrayList<String> dataModifiers = new ArrayList<>();
    private final ArrayList<String> relations = new ArrayList<>();

    private final SQL sql = new SQL();
    private Database database = new Database();
    
    public Analyzer(){
        keywords.add("USE");
        keywords.add("SHOW TABLES");
        keywords.add("CREATE DATABASE");
        keywords.add("CREATE TABLE");
        keywords.add("DROP TABLE");
        keywords.add("INSERT INTO");
        keywords.add("DELETE FROM");
        keywords.add("UPDATE");
        keywords.add("SET");
        keywords.add("SELECT");
        keywords.add("VALUES");
        keywords.add("FROM");
        keywords.add("WHERE");

        dataModifiers.add("NULL");
        dataModifiers.add("AND");
        dataModifiers.add("OR");
        dataModifiers.add("NOT");
        dataModifiers.add("AS");

        relations.add("PRIMARY KEY");
        relations.add("FOREIGN KEY");

        dataTypes.add("NUMBER");
        dataTypes.add("VARCHAR");
        dataTypes.add("CHAR");
        dataTypes.add("BOOLEAN");
        dataTypes.add("DATE");
        dataTypes.add("INT");
        dataTypes.add("DOUBLE");
        dataTypes.add("FLOAT");
    }

    public void analyzeSyntax(String line) throws Exception {
        boolean found = false;

        for(String keyword : keywords){
            if(line.startsWith(keyword) && (line.charAt(keyword.length()) == ' ' || line.charAt(keyword.length()) == ';')){
                found = true;

                try {
                    //lets get funky
                    switch (keyword) {
                        case "USE":
                            File dbFile = sql.handleUse(line, keyword);
                            refreshDB(dbFile);
                            break;

                        case "SHOW TABLES":
                            if(database.getDbFile() == null){
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            refreshDB(this.database.getDbFile());
                            this.database.printTables();
                            break;

                        case "CREATE TABLE":
                            if(database.getDbFile() == null){
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            sql.handleCreateTable(line, keyword, this.database);
                            refreshDB(this.database.getDbFile());
                            break;

                        case "CREATE DATABASE":
                            sql.handleCreateDatabase(line, keyword);
                            break;

                        case "DROP TABLE":
                            if(database.getDbFile() == null){
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            sql.handleDropTable(line, keyword, this.database);
                            refreshDB(this.database.getDbFile());
                            break;

                        case "INSERT INTO":
                            if(database.getDbFile() == null){
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            sql.handleInsertInto(line, keyword, this.database);
                            refreshDB(this.database.getDbFile());
                            break;

                        case "DELETE FROM":
                            sql.handleDeleteFrom(line, keyword);
                            break;

                        case "UPDATE":
                            sql.handleUpdate(line, keyword);
                            break;

                        case "SELECT":
                            if(database.getDbFile() == null){
                                throw new DatabaseNotSetException("USE COMMAND NOT EXECUTED");
                            }

                            sql.handleSelect(line,keyword,database);
                            break;
                    }

                } catch (StringIndexOutOfBoundsException e) {
                    throw new StringIndexOutOfBoundsException("ERROR WHILE PARSING: MISSING EXPRESSIONS" + e.getMessage());

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

                } catch (IOException e) {
                    throw new IOException(e.getMessage());

                } catch (Exception e){
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

    public void refreshDB(File file){
        this.database = new Database();
        this.database.setDbFile(file);
        this.database.retrieveTables();
    }
}
