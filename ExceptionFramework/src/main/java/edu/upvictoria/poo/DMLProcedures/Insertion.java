package edu.upvictoria.poo.DMLProcedures;

import edu.upvictoria.poo.*;

import edu.upvictoria.poo.DMLProcedures.Where.Tree;
import edu.upvictoria.poo.DMLProcedures.Where.Where;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;
import edu.upvictoria.poo.exceptions.TableNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class Insertion {
    private String query;
    private Database database;
    private final ArrayList<String> validKeywords = new ArrayList<>(List.of(
            "INSERT INTO", "VALUES"
    ));

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void handle() throws SQLSyntaxException, TableNotFoundException {
        Utils.hasValidKeywords(query, validKeywords);
        setQuery(Utils.clean(query));

        ArrayList<String> tokens = Utils.splitByWords(query, validKeywords, false);
        if(tokens.size() != 2) {
            throw new SQLSyntaxException("MALFORMED INSERTION STATEMENT");
        }

        // hay que separar el nombre de la tabla
        ArrayList<String> temp = Utils.splitByWords(tokens.get(0), database.getTableNames(), true);
        Table table = database.getTableByName(temp.get(0));
        ArrayList<Column> insertionColumns = parseColumns(temp.get(1), table);
        ArrayList<String> insertionValues = parseValues(tokens.get(1));

        if(insertionColumns.size() != insertionValues.size()) {
            throw new SQLSyntaxException("DATA PROVIDED MISMATCH");
        }

        table.appendDataToTable(insertionValues,insertionColumns);
    }

    /*public void handle() throws TableNotFoundException, SQLSyntaxException, InsuficientDataProvidedException, NoSuchFileException, ColumnDoesNotMatch, StringIndexOutOfBoundsException {
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
    }*/

    private ArrayList<Column> parseColumns(String query, Table table) throws SQLSyntaxException {
        ArrayList<Column> columns = new ArrayList<>();

         if(!(query.startsWith("(") && query.endsWith(")"))){
             throw new SQLSyntaxException("SYNTAX ERROR AT INSERTION COLUMNS");
         }

         query = query.substring(query.indexOf("(")+1,query.indexOf(")"));

         String[] values = query.split(",");
         for(int i = 0; i < values.length; i++){
             values[i] = values[i].trim();
             if(table.getColumnsName().contains(values[i])){
                 columns.add(table.getColumnByName(values[i]));
             } else {
                 throw new SQLSyntaxException("COLUMN " + values[i] + " DOES NOT EXIST IN TABLE " + table.getTableName());
             }
         }


         return columns;
    }

    private ArrayList<String> parseValues(String query) throws SQLSyntaxException{
        if(!(query.startsWith("(") && query.endsWith(")"))){
            throw new SQLSyntaxException("SYNTAX ERROR AT INSERTION VALUES");
        }

        query = query.substring(query.indexOf("(") + 1,query.indexOf(")")).trim();
        String[] rawValues = query.split(",");
        ArrayList<String> values = new ArrayList<>();

        for (String rawValue : rawValues) {
            if(Utils.splitByWords(rawValue, Analyzer.getComparators(), false).size() > 1){
                throw new SQLSyntaxException("COMPARATORS NOT SUPPORTED IN INSERTION VALUES");
            }

            if(rawValue.startsWith("'") && rawValue.endsWith("'")){
                rawValue = rawValue.substring(1,rawValue.length()-1);
            }

            ArrayList<String> tokens = Utils.getWhereTokens(rawValue);

            if (tokens.size() == 1) {
                values.add(tokens.get(0));
                continue;
            }

            tokens = Where.infixToPostfix(tokens);
            Tree.Node root = Where.createTree(tokens);
            values.add(Where.evaluateSubTree(root));
        }

        return values;
    }
}