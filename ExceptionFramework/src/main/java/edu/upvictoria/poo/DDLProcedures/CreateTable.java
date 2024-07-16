package edu.upvictoria.poo.DDLProcedures;

import edu.upvictoria.poo.*;
import edu.upvictoria.poo.exceptions.DataTypeNotFoundException;
import edu.upvictoria.poo.exceptions.DuplicateEntryException;
import edu.upvictoria.poo.exceptions.TableNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;

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

        File tableFile = getFile(columns);
        columns.remove(0);
        Table newTable = new Table(tableFile, columns);

        ArrayList<String> rowData = new ArrayList<>();
        for(Column column : newTable.getColumns()){
            String data = column.getName() +" "+ column.getType() +" "+ column.getConstraint();
            rowData.add(data);
        }

        newTable.writeDataToFile(rowData);
        database.addTable(newTable);
    }

    private ArrayList<Column> splitValues(String line) throws DataTypeNotFoundException, StringIndexOutOfBoundsException {
        ArrayList<String> dataTypes = Analyzer.getDataTypes();
        ArrayList<String> constraints = Analyzer.getConstraints();

        ArrayList<Column> columns = new ArrayList<>();
        String tableName, cName, cDataType = null, cConstraint = null;
        boolean foundDataType;
        boolean foundConstraint;

        tableName = line.substring(0, line.indexOf("(")).trim();
        Column KKColumn = new Column(tableName,null,null);
        columns.add(KKColumn);

        line = line.substring(tableName.length()).trim();
        if(!(line.startsWith("(") && line.endsWith(")"))){
            throw new StringIndexOutOfBoundsException();
        }
        line = line.substring(line.indexOf("(")+1,line.indexOf(")")).trim();
        line = line.replaceAll(", ", ",");

        String[] values = line.split(",");

        for(String value : values){
            foundDataType = false;
            String[] parts = value.split(" ");
            cName = parts[0].trim();

            //BUSCAMOS EL TIPO DE DATO AQUI
            for(String dataType : dataTypes){
                if(value.contains(dataType)){
                    foundDataType = true;
                    cDataType = dataType;
                    break;
                }
            }

            if(!foundDataType){
                throw new DataTypeNotFoundException("DATA TYPE NOT FOUND IN LINE " + value);
            }

            //BUSCAMOS SI TIENE CONSTRAINT DEFINIDA AQUI
            if(parts.length > 2){
                foundConstraint = false;

                for(String constraint : constraints){
                    if(value.contains(constraint)){
                        foundConstraint = true;
                        cConstraint = constraint;
                        break;
                    }
                }

                if(!foundConstraint){
                    throw new DataTypeNotFoundException("CONSTRAINT NOT FOUND IN LINE " + value);
                }
            }

            Column column = new Column(cName.trim(),cDataType.trim(),cConstraint);
            columns.add(column);
            cConstraint = "\0";
        }

        return columns;
    }

    private File getFile(ArrayList<Column> columns) throws FileSystemException {
        File tableFile = new File(database.getDbFile().getAbsolutePath() + "/" + columns.get(0).getName() + ".csv");

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
