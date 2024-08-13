package edu.upvictoria.poo.DMLProcedures;

import edu.upvictoria.poo.*;

import edu.upvictoria.poo.DMLProcedures.Where.*;

import edu.upvictoria.poo.exceptions.SQLSyntaxException;
import edu.upvictoria.poo.exceptions.TableNotFoundException;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Objects;

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

    public ArrayList<ArrayList<String>> handle() throws NoSuchFileException, TableNotFoundException, SQLSyntaxException, IndexOutOfBoundsException {
        String cleanedLine = Utils.clean(query, keyword);
        String rawColumns = cleanedLine.substring(0,cleanedLine.indexOf("FROM")-1).trim();
        String selectedTable, whereLine = null;

        ArrayList<Column> columns;
        ArrayList<String> whereTokens;
        ArrayList<ArrayList<Object>> wheredData;

        // Si la sentencia select contiene una clausula WHERE
        if(cleanedLine.contains("WHERE")) {
            String[] splitted = cleanedLine.split("WHERE");
            String[] splitted2 = splitted[0].split("FROM");

            selectedTable = splitted2[1].trim();
            whereLine = splitted[1].trim();
        } else {
            String[] splitted = cleanedLine.split("FROM");
            selectedTable = splitted[1].trim();
        }

        // Obtener tabla
        Table table = database.getTableByName(selectedTable);

        // Si existe una clasula WHERE
        if(whereLine != null){
            // Evaluamos los datos y obtenemos aquellos que cumplan las condiciones escritras
            whereTokens = Utils.getWhereTokens(whereLine);
            whereTokens = Where.infixToPostfix(whereTokens);
            Tree.Node root = Where.createTree(whereTokens);
            wheredData = Where.evaluateTree(root, table.getData(), table);
        } else {
            // Agarramos todos los datos
            wheredData = table.getData();
        }

        // Si selecciona columnas específicas
        if(rawColumns.equals("*")){
            columns = table.getColumns();
        } else {
            columns = getSelectedColumns(table, rawColumns);
        }

        return table.printData(columns, wheredData);
    }

    /**
     * @param table
     * @param rawColumns
     * @return Una lista de columnas obtenida del parseo de un string con sus nombres
     * @throws SQLSyntaxException
     */
    private ArrayList<Column> getSelectedColumns(Table table, String rawColumns) throws SQLSyntaxException{
        ArrayList<Column> columns = new ArrayList<>();
        String[] values = rawColumns.split(",");

        for(int i = 0; i < values.length; i++){
            values[i] = values[i].trim();
            Column column = parseColumn(values[i], table);
            columns.add(column);
        }

        return columns;
    }

    private Column parseColumn(String definition, Table table) throws SQLSyntaxException{
        Column column;
        String alias = null;
        String temp;

        if(definition.contains(" AS ")){
            String[] columnInfo = definition.split(" AS ");
            temp = columnInfo[0].trim();
            column = table.getColumnByName(temp);
            alias = columnInfo[1].trim();

            if(alias.startsWith("'") && alias.endsWith("'")){
                alias = alias.substring(1,alias.length()-1);
            } else {
                throw new SQLSyntaxException("INVALID COLUMN ALIAS: " + alias);
            }

            if(column != null){
                column.setAlias(alias);
            }
        } else {
            temp = definition;
            column = table.getColumnByName(temp);
        }

        if(column != null){
            return column;
        }

        ArrayList<String> operationTokens = Utils.getWhereTokens(temp);
        operationTokens = Where.infixToPostfix(operationTokens);
        Tree.Node root = Where.createTree(operationTokens);

        column = new Column(temp,null,null);
        column.setAlias(alias);
        column.setOperation(root);
        return column;
    }
}
