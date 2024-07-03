package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQL {
    public File handleUse(String line, String keyword) throws FileSystemException, StringIndexOutOfBoundsException, FileNotFoundException {
        String givenPath = Utils.clean(line,keyword);

        File database = new File(Paths.get("").toAbsolutePath().resolve(givenPath).toString());
        if(!database.exists()){
            throw new FileNotFoundException();
        }

        if(!database.isDirectory()){
            throw new NotDirectoryException("GIVEN PATH IS NOT A DIRECTORY: " + givenPath);
        }

        if(!database.getName().endsWith("_DB")){
            throw new NoSuchFileException(givenPath);
        }

        return database;
    }

    public ArrayList<Column> splitValues(String line) throws DataTypeNotFoundException, StringIndexOutOfBoundsException {
        Analyzer analyzer = new Analyzer();
        ArrayList<String> dataTypes = analyzer.getDataTypes();
        ArrayList<String> constraints = analyzer.getConstraints();

        ArrayList<Column> columns = new ArrayList<>();
        String tableName, cName, cDataType = null, cConstraint = null;
        boolean foundDataType;
        boolean foundConstraint;

        try {
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
        } catch (StringIndexOutOfBoundsException e){
            throw new StringIndexOutOfBoundsException("TABLE NAME NOT FOUND OR MISSING VALUES OR MISSING EXPRESSION");
        }

        return columns;
    }

    public void handleCreateTable(String line, String keyword, Database database) throws IOException {
        String cleanedLine = Utils.clean(line, keyword);
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

        File tableFile = getFile(database, columns);
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

    private static File getFile(Database database, ArrayList<Column> columns) throws FileSystemException {
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

    public void handleCreateDatabase(String line, String keyword) throws FileSystemException {
        String givenPath = Utils.clean(line,keyword);
        File database = new File(Paths.get("").toAbsolutePath().resolve(givenPath).toString());

        if(database.exists()){
            throw new FileAlreadyExistsException("NAME ALREADY IN USE: " + givenPath);
        }

        if(!database.getName().endsWith("_DB")){
            throw new NoSuchFileException("NOT DATABASE GIVEN: " + givenPath);
        }

        if(!database.getParentFile().canWrite()){
            throw new AccessDeniedException("NO PERMISSION IN GIVEN PATH: " + givenPath);
        }

        if(!database.mkdir()){
            throw new SecurityException("FAILED TO CREATE DATABASE AT " + givenPath);
        }
    }

    public void handleDropTable(String line, String keyword, Database database) throws IOException {
        String givenName = Utils.clean(line, keyword);

        for (Table table : database.getTables()) {
            if (table.getTableName().equals(givenName)) {

                System.out.print("DO YOU REALLY WANT TO DELETE THE TABLE? Y/N\n ~ ");

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String decision = reader.readLine().toUpperCase();

                    if(!decision.equals("Y")){
                        throw new IOException("USER REFUSED DROP TABLE PROCESS");
                    }
                } catch (IOException e) {
                    throw new IOException(e.getMessage());
                }

                if (!table.getTableFile().delete()) {
                    throw new FileSystemException("COULD NOT DELETE THE FILE AT: " + table.getTableFile().getAbsolutePath());
                } else {
                    return;
                }
            }
        }

        throw new FileNotFoundException("TABLE NOT FOUND");
    }

    public void handleDeleteFrom(String line, String keyword, Database database) throws IOException, StringIndexOutOfBoundsException {
        String cleanedLine = Utils.clean(line, keyword), selectedTable, whereLine = null;
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
                    whereTokens = getWhereTokens(whereLine,table);
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

    public void handleUpdate(String line, String keyword, Database database) throws SQLSyntaxException, NoSuchFileException {
        String cleanedLine = Utils.clean(line,keyword), selectedTable, whereLine = null, rawUpdateData;
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
                    whereTokens = getWhereTokens(whereLine,table);
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

    public void handleSelect(String line, String keyword, Database database) throws SQLSyntaxException, StringIndexOutOfBoundsException, NoSuchFileException, ColumnDoesNotMatch {
        ArrayList<String> columns = new ArrayList<>();
        ArrayList<String> showingCol = new ArrayList<>();

        String cleanedLine = Utils.clean(line, keyword);
        String selectedColumns = cleanedLine.substring(0,cleanedLine.indexOf("FROM")-1).trim();
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

        if(!selectedColumns.equals("*")){
            selectedColumns = "(" + selectedColumns + ")";
            columns = splitInsertionColumns(selectedColumns, false);
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

    public ArrayList<String> getWhereTokens(String line, Table table) throws SQLSyntaxException, IndexOutOfBoundsException {
        Analyzer analyzer = new Analyzer();
        ArrayList<String> whereTokens = new ArrayList<>();
        String value;

        String format1 = "^'.+'";
        String format2 = "^\\d*\\s+";
        String format3 = "^\\d*$";

        Pattern pattern1 = Pattern.compile(format1);
        Pattern pattern2 = Pattern.compile(format2);
        Pattern pattern3 = Pattern.compile(format3);
        Matcher matcher1, matcher2, matcher3;

        line = line.replaceAll("<>","!=");
        line = line.replaceAll("^=","!=");

        try {
            while(!line.isBlank()){
                String initStateLine = line;
                for(int i = 0; i < analyzer.getOperators().size(); i++){
                    String operator = analyzer.getOperators().get(i);

                    if(line.startsWith(operator)){
                        if(operator.equals("(") || operator.equals(")") || operator.equals("AND") || operator.equals("OR")){
                            whereTokens.add(operator);
                        } else {
                            String lastToken = whereTokens.get(whereTokens.size()-1);

                            if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                                throw new SQLSyntaxException("UNEXPECTED OPERATOR: " + operator);
                            }

                            lastToken += (" " + operator);
                            whereTokens.set(whereTokens.size()-1, lastToken);
                        }

                        line = line.substring(line.indexOf(operator) + operator.length()).trim();
                        break;
                    }
                }

                for(int i = 0; i < table.getColumnsName().size(); i++){
                    String columnName = table.getColumnsName().get(i);

                    if(line.startsWith(columnName)){
                        whereTokens.add(columnName);
                        line = line.substring(line.indexOf(columnName) + columnName.length()).trim();
                        break;
                    }
                }

                if(line.startsWith("NULL")){
                    String lastToken = whereTokens.get(whereTokens.size()-1);

                    if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                        throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                    }

                    lastToken += " \0";
                    whereTokens.set(whereTokens.size()-1, lastToken);
                    line = line.substring(line.indexOf("NULL") + "NULL".length()).trim();
                }

                matcher1 = pattern1.matcher(line);
                if(matcher1.find()){
                    String lastToken = whereTokens.get(whereTokens.size()-1);

                    if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                        throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                    }

                    line = line.substring(1);
                    value = line.substring(0, line.indexOf("'")).trim();
                    lastToken += (" " + value);
                    whereTokens.set(whereTokens.size()-1, lastToken);
                    line = line.substring(line.indexOf(value) + value.length() + 1).trim();

                    continue;
                }

                matcher2 = pattern2.matcher(line);
                if(matcher2.find()) {
                    String lastToken = whereTokens.get(whereTokens.size()-1);

                    if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                        throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                    }

                    value = line.substring(0, line.indexOf(" ")).trim();
                    lastToken += (" " + value);
                    whereTokens.set(whereTokens.size()-1, lastToken);
                    line = line.substring(line.indexOf(value) + value.length() + 1).trim();

                    continue;
                }

                matcher3 = pattern3.matcher(line);
                if(matcher3.find()) {
                    String lastToken = whereTokens.get(whereTokens.size()-1);

                    if(!table.getColumnsName().contains(lastToken.split(" ")[0])){
                        throw new SQLSyntaxException("UNEXPECTED OPERATOR: NULL");
                    }

                    value = line.trim();
                    lastToken += (" " + value);
                    whereTokens.set(whereTokens.size()-1, lastToken);
                    line = line.substring(line.indexOf(value) + value.length()).trim();
                }

                if(initStateLine.equals(line)){
                    throw new SQLSyntaxException("MALFORMED STATEMENT");
                }
            }
        } catch (SQLSyntaxException e) {
            throw new SQLSyntaxException(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("WHERE STATEMENT MALFORMED AT > " + line);
        }

        for(String token : whereTokens){
            System.out.println(token);
        }

        return whereTokens;
    }
}

// TODO: COMPROBAR QUE SE INGRESA UN TIPO DE DATO CORRECTO AL CREAR UN REGISTRO
// TODO: COMPROBAR QUE NO SE ELIGEN DOS COLUMNAS IGUALES A LAS QUE INGRESAR UN REGISTRO
// TODO: EN INSERCION DE DATOS DELIMITAR LOS STRINGS CON COMILLAS SIMPLES