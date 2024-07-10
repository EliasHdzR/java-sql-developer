package edu.upvictoria.poo.DMLProcedures;

import edu.upvictoria.poo.Database;
import edu.upvictoria.poo.DMLProcedures.Where.Tree;
import edu.upvictoria.poo.DMLProcedures.Where.Where;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.Utils;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;

public class Deletion {
    private String query;
    private Database database;
    private final String keyword = "DELETE FROM";

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void handle() throws IOException, StringIndexOutOfBoundsException {
        String cleanedLine = Utils.clean(query, keyword), selectedTable, whereLine = null;
        boolean tableExists = false;
        ArrayList<String> whereTokens;
        ArrayList<ArrayList<Object>> wheredData;

        if(cleanedLine.contains("WHERE")){
            selectedTable = cleanedLine.substring(0,cleanedLine.indexOf(" ")).trim();
            whereLine = cleanedLine.substring(cleanedLine.indexOf("WHERE") + "WHERE".length() + 1).trim();
        } else {
            selectedTable = cleanedLine;
        }

        ArrayList<Table> tables = database.getTables();
        for(Table table : tables){
            if(table.getTableName().equals(selectedTable)){
                tableExists = true;

                if(whereLine != null){
                    whereTokens = Utils.getWhereTokens(whereLine,table);
                    whereTokens = Where.infixToPostfix(whereTokens);
                    Tree.Node root = Where.createTree(whereTokens);
                    wheredData = Where.evaluateTree(root, table.getData(), table);
                } else {
                    wheredData = table.getData();
                }

                ArrayList<ArrayList<Object>> pureData = new ArrayList<>(table.getData());
                pureData.removeAll(wheredData);
                table.setData(pureData);
                table.writeDataToFile();
                break;
            }
        }

        if(!tableExists){
            throw new NoSuchFileException("TABLE DOES NOT EXISTS");
        }
    }
}
