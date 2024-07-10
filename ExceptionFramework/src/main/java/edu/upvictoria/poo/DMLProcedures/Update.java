package edu.upvictoria.poo.DMLProcedures;

import edu.upvictoria.poo.Database;
import edu.upvictoria.poo.DMLProcedures.Where.Tree;
import edu.upvictoria.poo.DMLProcedures.Where.Where;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.Utils;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Update {
    private String query;
    private Database database;
    private final String keyword = "UPDATE";

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void handle() throws SQLSyntaxException, NoSuchFileException {
        String cleanedLine = Utils.clean(query,keyword), selectedTable, whereLine = null, rawUpdateData;
        boolean tableExists = false;
        ArrayList<String> whereTokens;
        ArrayList<ArrayList<Object>> wheredData;
        ArrayList<String> updateData = new ArrayList<>();

        String format1 = "^'.+'";
        String format2 = "^\\d*";

        Pattern pattern1 = Pattern.compile(format1);
        Pattern pattern2 = Pattern.compile(format2);
        Matcher matcher1, matcher2;


        if(!cleanedLine.contains("SET")){
            throw new SQLSyntaxException("MISSING 'SET' KEYWORD");
        }

        selectedTable = cleanedLine.substring(0,cleanedLine.indexOf("SET")).trim();

        try {
            if(cleanedLine.contains("WHERE")){
                rawUpdateData = cleanedLine.substring(cleanedLine.indexOf("SET") + "SET".length(), cleanedLine.indexOf("WHERE")).trim();
                whereLine = cleanedLine.substring(cleanedLine.indexOf("WHERE") + "WHERE".length() + 1).trim();
            } else {
                rawUpdateData = cleanedLine.substring(cleanedLine.indexOf("SET") + "SET".length()).trim();
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new SQLSyntaxException("MISSING KEYWORDS");
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
            }
        }

        if(!tableExists){
            throw new NoSuchFileException("TABLE DOES NOT EXISTS");
        }
    }
}
