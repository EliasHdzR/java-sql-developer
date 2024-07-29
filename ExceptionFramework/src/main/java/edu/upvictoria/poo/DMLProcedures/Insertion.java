package edu.upvictoria.poo.DMLProcedures;

import edu.upvictoria.poo.*;

import edu.upvictoria.poo.exceptions.ColumnDoesNotMatch;
import edu.upvictoria.poo.exceptions.InsuficientDataProvidedException;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;
import edu.upvictoria.poo.exceptions.TableNotFoundException;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;

public class Insertion {
    private String query;
    private Database database;
    private final String keyword = "INSERT INTO";

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void handle() throws TableNotFoundException, SQLSyntaxException, InsuficientDataProvidedException, NoSuchFileException, ColumnDoesNotMatch, StringIndexOutOfBoundsException {
        String cleanedLine, cleanedLine_v2, cleanedLine_v3;
        ArrayList<String> insertionColumns, insertionData;
        ArrayList<Column> tableColumns = new ArrayList<>();
        Table table = null;
        boolean tableExists = false, columnFound = false;

        cleanedLine = Utils.clean(query, keyword);
        int VALUEIndex = cleanedLine.indexOf("VALUES");

        if(VALUEIndex == -1){
            throw new StringIndexOutOfBoundsException("MISSING EXPRESSION");
        }

        cleanedLine_v2 = cleanedLine.substring(0,VALUEIndex-1);
        insertionColumns = splitInsertionColumns(cleanedLine_v2);

        cleanedLine_v3 = cleanedLine.substring(VALUEIndex + "VALUES ".length());
        insertionData = splitInsertionValues(cleanedLine_v3);

        if(insertionColumns.size()-1 != insertionData.size()){
            throw new InsuficientDataProvidedException("DATA PROVIDED MISMATCH");
        }

        ArrayList<Table> tables = database.getTables();
        for(Table tableF : tables) {
            if(tableF.getTableName().equals(insertionColumns.get(0))){
                tableExists = true;
                insertionColumns.remove(0);
                table = tableF;
                tableColumns = tableF.getColumns();
                break;
            }
        }

        if(!tableExists){
            throw new NoSuchFileException("TABLE DOES NOT EXISTS");
        }

        for(String insertionColumn : insertionColumns){
            for(Column tableColumn : tableColumns){
                if(tableColumn.getName().equals(insertionColumn)){
                    columnFound = true;
                    break;
                }
            }

            if(!columnFound){
                throw new ColumnDoesNotMatch("COLUMN DOES NOT MATCH: " + insertionColumn);
            }

            columnFound = false;
        }

        table.appendDataToTable(insertionData,insertionColumns);
    }

    private ArrayList<String> splitInsertionColumns(String query) throws SQLSyntaxException {
        ArrayList<String> columns = new ArrayList<>();
        String tableName;

        tableName = query.substring(0, query.indexOf("(")).trim();
        query = query.substring(tableName.length()).trim();

         if(!(query.startsWith("(") && query.endsWith(")"))){
             throw new SQLSyntaxException("SYNTAX ERROR AT INSERTION COLUMNS");
         }

         query = query.substring(query.indexOf("(")+1,query.indexOf(")"));
         columns.add(tableName.trim());

         String[] values = query.split(",");
         for(int i = 0; i < values.length; i++){
             values[i] = values[i].trim();
         }

         columns.addAll(Arrays.asList(values));
         return columns;
    }

    private ArrayList<String> splitInsertionValues(String query) throws SQLSyntaxException{
        if(!(query.startsWith("(") && query.endsWith(")"))){
            throw new SQLSyntaxException("SYNTAX ERROR AT INSERTION VALUES");
        }

        query = query.substring(query.indexOf("(") + 1,query.indexOf(")")).trim();
        String[] values = query.split(",");

        for(int i = 0; i < values.length; i++){
                values[i] = values[i].replace("'", "");
                values[i] = values[i].trim();
        }

        return new ArrayList<>(Arrays.asList(values));
    }
}