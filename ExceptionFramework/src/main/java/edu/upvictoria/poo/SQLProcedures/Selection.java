package edu.upvictoria.poo.SQLProcedures;

import edu.upvictoria.poo.Database;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.Column;
import edu.upvictoria.poo.Utils;

import edu.upvictoria.poo.SQLProcedures.Where.*;

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

    public void handle() throws NoSuchFileException, ColumnDoesNotMatch, SQLSyntaxException, IndexOutOfBoundsException {
        String cleanedLine = Utils.clean(query, keyword);
        String rawColumns = cleanedLine.substring(0,cleanedLine.indexOf("FROM")-1).trim();
        String selectedTable, whereLine = null;

        ArrayList<Column> columns;
        ArrayList<String> whereTokens;
        ArrayList<ArrayList<Object>> wheredData;

        // Si la sentencia select contiene una clausula WHERE
        if(cleanedLine.contains("WHERE")){
            String[] splitted = cleanedLine.split("WHERE");
            String[] splitted2 = splitted[0].split("FROM");
            selectedTable = splitted2[1].trim();
            System.out.println(selectedTable);
            whereLine = splitted[1].trim();
            System.out.println(whereLine);
        } else {
            String[] splitted = cleanedLine.split("FROM");
            selectedTable = splitted[1].trim();
        }

        // Obtener tabla
        Table table = database.getTableByName(selectedTable);
        if(table == null){
            throw new NoSuchFileException("TABLE DOES NOT EXISTS");
        }

        // Si selecciona columnas específicas
        if(!rawColumns.equals("*")){
            columns = getSelectedColumns(table, rawColumns);
        } else {
            columns = table.getColumns();
        }

        // Si existe una clasula WHERE
        if(whereLine != null){
            // Evaluamos los datos y obtenemos aquellos que cumplan las condiciones escritras
            whereTokens = Utils.getWhereTokens(whereLine,table);
            whereTokens = Where.infixToPostfix(whereTokens);
            Tree.Node root = Where.createTree(whereTokens);
            wheredData = Where.evaluateTree(root, table.getData(), table);
        } else {
            // Agarramos todos los datos
            wheredData = table.getData();
        }

        table.printData(columns, wheredData);
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

            Column column = table.getColumnByName(values[i]);
            if(column == null){
                throw new SQLSyntaxException("COLUMN DOES NOT EXISTS");
            } else {
                columns.add(column);
            }
        }

        return columns;
    }
}


// todo: permitir la seleccion de columnas desordenadas e imprimirlas en ese orden