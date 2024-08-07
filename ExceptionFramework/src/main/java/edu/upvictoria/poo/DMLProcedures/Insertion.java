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