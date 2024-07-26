package edu.upvictoria.poo.DDLProcedures;

import edu.upvictoria.poo.*;
import edu.upvictoria.poo.exceptions.DataTypeNotFoundException;
import edu.upvictoria.poo.exceptions.DuplicateEntryException;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;
import edu.upvictoria.poo.exceptions.TableNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateTable {
    private String query;
    private Database database;
    private final String keyword = "CREATE TABLE";

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void handle() throws DataTypeNotFoundException, StringIndexOutOfBoundsException, DuplicateEntryException, IOException, TableNotFoundException {
        String cleanedLine = Utils.clean(query, keyword);
        String tableName;

        try {
            tableName = cleanedLine.substring(0, cleanedLine.indexOf("(")).trim();
            cleanedLine = cleanedLine.substring(cleanedLine.indexOf("(")).trim();
        } catch (StringIndexOutOfBoundsException e) {
            throw new SQLSyntaxException("MALFORMED STATEMENT: TABLE NAME NOT FOUND");
        }

        ArrayList<Column> columns = splitValues(cleanedLine);
        ArrayList<String> duplicates = new ArrayList<>();


        for (int i = 0; i < columns.size(); i++) {
            for (int j = i + 1; j < columns.size(); j++) {
                if (columns.get(i).getName().equals(columns.get(j).getName())) {
                    duplicates.add(columns.get(i).getName());
                    break;
                }
            }
        }

        if(!duplicates.isEmpty()){
            throw new DuplicateEntryException("DUPLICATE COLUMNS SPECIFIED IN TABLE CREATION");
        }

        File tableFile = getFile(tableName);
        Table newTable = new Table(tableFile, columns);

        ArrayList<String> rowData = new ArrayList<>();
        for(Column column : newTable.getColumns()){
            String data = column.getName() +" "+ column.getType();
            if(!column.getConstraint().isEmpty()){
                data += " " + column.getConstraint();
            }
            rowData.add(data);
        }

        newTable.writeDataToFile(rowData);
        database.addTable(newTable);
    }

    private ArrayList<Column> splitValues(String line) throws SQLSyntaxException, DataTypeNotFoundException {
        ArrayList<Column> columns = new ArrayList<>();

        if(line.startsWith("(") && line.endsWith(")")){
            line = line.substring(1, line.length()-1);
        } else {
            throw new SQLSyntaxException("MALFORMED STATEMENT");
        }

        // sintaxis de la definicion de una columna: nombre tipo_de_dato(tamaño) [constraint]
        String[] columnsRawDefinitions = line.split(",");
        for(String columnRawDefinition : columnsRawDefinitions){
            String[] columnTokens = columnRawDefinition.trim().split(" ");
            String constraint = "";
            ArrayList<String> arrColumnTokens = new ArrayList<>(Arrays.asList(columnTokens));

            for(int i = 0; i < columnTokens.length; i++){
                columnTokens[i] = columnTokens[i].trim();
            }

            // el tamaño maximo podría ser de 6 palabras: TOTAL DOUBLE(5,2) NOT NULL PRIMARY KEY
            // el tamaño minimo es de 2 palabras: TOTA DOUBLE(5,2)
            if(arrColumnTokens.size() > 6 || arrColumnTokens.size() < 2){
                throw new SQLSyntaxException("MALFORMED COLUMN DEFINITION: " + columnRawDefinition);
            }

            // checamos los constraints que existen y que sean válidos
            if(arrColumnTokens.size() > 2){
                for(int j = 2; j < arrColumnTokens.size(); j++){
                    constraint += arrColumnTokens.get(j) + " ";
                }

                constraint = constraint.trim();
                ArrayList<String> verification = Utils.splitByWords(constraint, Analyzer.getConstraints());
                if(!verification.isEmpty()){
                    throw new SQLSyntaxException("UNDEFINED CONSTRAINT: " + constraint);
                }
            }

            // comprobamos que sean tipos de dato válidos
            // a los tipos de dato BOOLEAN y DATE no se les define un tamaño
            if(!(arrColumnTokens.get(1).startsWith("BOOLEAN") || arrColumnTokens.get(1).startsWith("DATE"))
                    && arrColumnTokens.get(1).contains("(") && arrColumnTokens.get(1).contains(")")){
                arrColumnTokens.set(1, arrColumnTokens.get(1).substring(0, columnTokens[1].indexOf("(")));
            } else {
                throw new SQLSyntaxException("WRONG DEFINITION OF COLUMN " + columnRawDefinition);
            }

            if(!Analyzer.getDataTypes().contains(arrColumnTokens.get(1))){
                throw new DataTypeNotFoundException("UNDEFINED COLUMN TYPE " + columnTokens[1]);
            }

            columns.add(new Column(arrColumnTokens.get(0).trim(),arrColumnTokens.get(1).trim(), constraint.trim()));
        }

        return columns;
    }

    private File getFile(String tableName) throws FileSystemException {
        File tableFile = new File(database.getDbFile().getAbsolutePath() + "/" + tableName + ".csv");

        if(tableFile.exists()){
            throw new FileAlreadyExistsException("NAME ALREADY IN USE: " + tableFile.getAbsolutePath());
        }

        if(!tableFile.getParentFile().canWrite()){
            throw new AccessDeniedException("NO PERMISSION IN GIVEN PATH: " + tableFile.getAbsolutePath());
        }

        try {
            if (!tableFile.createNewFile()) {
                throw new AccessDeniedException("");
            }
        } catch (Exception e) {
            throw new AccessDeniedException("FAILED TO CREATE DIRECTORY AT " + tableFile.getAbsolutePath());
        }

        return tableFile;
    }
}
