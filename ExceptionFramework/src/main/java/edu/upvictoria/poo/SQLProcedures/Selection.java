package edu.upvictoria.poo.SQLProcedures;


import edu.upvictoria.poo.Database;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.Column;
import edu.upvictoria.poo.Utils;

import edu.upvictoria.poo.exceptions.ColumnDoesNotMatch;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;

public class Selection {
    private String query;
    private Database database;
    private final String keyword = "SELECT";

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void handle() throws NoSuchFileException, ColumnDoesNotMatch {
        ArrayList<Column> columns = new ArrayList<>();
        ArrayList<String> showingCol = new ArrayList<>();

        String cleanedLine = Utils.clean(query, keyword);
        String rawColumns = cleanedLine.substring(0,cleanedLine.indexOf("FROM")-1).trim();
        String selectedTable, whereLine = null;

        ArrayList<String> whereTokens;
        ArrayList<ArrayList<Object>> wheredData;
        boolean tableExists = false;

        if(cleanedLine.contains("WHERE")){
            selectedTable = cleanedLine.substring(cleanedLine.indexOf("FROM ") + "FROM".length() + 1, cleanedLine.indexOf(" WHERE")).trim();
            whereLine = cleanedLine.substring(cleanedLine.indexOf("WHERE") + "WHERE".length() + 1).trim();
        } else {
            selectedTable = cleanedLine.substring(cleanedLine.indexOf("FROM ") + "FROM".length() + 1).trim();
        }

        if(!rawColumns.equals("*")){
            columns = getSelectedColumns(rawColumns, );
        }

        ArrayList<Table> tables = database.getTables();
        for(Table table : tables) {
            if (table.getTableName().equals(selectedTable)) {
                tableExists = true;

                if(whereLine != null){
                    whereTokens = getWhereTokens(whereLine,table);
                    whereTokens = Where.infixToPostfix(whereTokens);
                    Tree.Node root = Where.createTree(whereTokens);
                    wheredData = Where.evaluateTree(root, table.getData(), table);
                } else {
                    wheredData = table.getData();
                }

                if(!selectedColumns.equals("*")){
                    for(String tableColName : table.getColumnsName()){
                        for(String selectColName : columns) {
                            if(tableColName.equals(selectColName)){ showingCol.add(tableColName); }
                        }
                    }

                    if(showingCol.size() < columns.size()){
                        throw new ColumnDoesNotMatch("COLUMN DOES NOT MATCH");
                    }

                    table.printData(showingCol, wheredData);

                } else {
                    table.printData(wheredData);
                }
                break;
            }
        }

        if(!tableExists){
            throw new NoSuchFileException("TABLE DOES NOT EXISTS");
        }
    }

    private ArrayList<Column> getSelectedColumns(String rawColumns) throws SQLSyntaxException{
        String[] values = rawColumns.split(",");

        for(int i = 0; i < values.length; i++){
            if(values[i].startsWith("'") && values[i].endsWith("'")){

            }
            values[i] = values[i].trim();
            values[i] = values[i].replace("'", "");
        }

        return new ArrayList<>(Arrays.asList(values));
    }
}
