package edu.upvictoria.poo;

import edu.upvictoria.poo.exceptions.ColumnDoesNotMatch;
import edu.upvictoria.poo.exceptions.DataTypeNotFoundException;
import edu.upvictoria.poo.exceptions.InsuficientDataProvidedException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;

public class SQL {
    public String clean(String line, String keyword) throws StringIndexOutOfBoundsException {
        int endOfKeyword = line.indexOf(keyword) + keyword.length();
        int semicolon = line.indexOf(";");

        try {
            line = line.substring(endOfKeyword + 1, semicolon).trim();
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException();
        }

        return line;
    }

    public File handleUse(String line, String keyword) throws FileSystemException, StringIndexOutOfBoundsException, FileNotFoundException {
        String givenPath;

        try {
            givenPath = clean(line,keyword);
        } catch (StringIndexOutOfBoundsException e){
            throw new StringIndexOutOfBoundsException(e.getMessage());
        }

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

                Column column = new Column(cName,cDataType,cConstraint);
                columns.add(column);
            }
        } catch (StringIndexOutOfBoundsException e){
            throw new StringIndexOutOfBoundsException("TABLE NAME NOT FOUND, MISSING VALUES OR MISSING EXPRESSION");
        }

        return columns;
    }

    public void handleCreateTable(String line, String keyword, Database database) throws IOException {
        String cleanedLine;
        ArrayList<Column> columns;

        try {
            cleanedLine = clean(line, keyword);
            columns = splitValues(cleanedLine);
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException(e.getMessage());
        } catch (DataTypeNotFoundException e) {
            throw new DataTypeNotFoundException(e.getMessage());
        }

        File tableFile = getFile(database, columns);
        columns.remove(0);
        Table newTable = new Table(tableFile, columns);

        database.addTable(newTable);

        ArrayList<String> rows = new ArrayList<>();

        for(Column column : columns){
            String data = column.getName() +" "+ column.getType() +" "+ column.getConstraint();
            rows.add(data);
        }

        newTable.appendData(rows);
    }

    private static File getFile(Database database, ArrayList<Column> columns) throws IOException {
        File tableFile = new File(database.getDbFile().getAbsolutePath() + "/" + columns.get(0).getName() + ".csv");

        if(tableFile.exists()){
            throw new FileAlreadyExistsException("NAME ALREADY IN USE: " + tableFile.getAbsolutePath());
        }

        if(!tableFile.getParentFile().canWrite()){
            throw new AccessDeniedException("NO PERMISSION IN GIVEN PATH: " + tableFile.getAbsolutePath());
        }

        try{
            if (!tableFile.createNewFile()) {
                throw new SecurityException("FAILED TO CREATE DIRECTORY AT " + tableFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }

        return tableFile;
    }

    public void handleCreateDatabase(String line, String keyword) throws FileSystemException {
        String givenPath;

        try {
            givenPath = clean(line,keyword);
        } catch (StringIndexOutOfBoundsException e){
            throw new StringIndexOutOfBoundsException(e.getMessage());
        }

        File database = new File(Paths.get("").toAbsolutePath().resolve(givenPath).toString());

        if(database.exists()){
            throw new FileAlreadyExistsException("NAME ALREADY IN USE: " + givenPath);
        }

        if(!database.getName().endsWith("_DB")){
            throw new NoSuchFileException("NOT DABASE GIVEN: " + givenPath);
        }

        if(!database.getParentFile().canWrite()){
            throw new AccessDeniedException("NO PERMISSION IN GIVEN PATH: " + givenPath);
        }

        if(!database.mkdir()){
            throw new SecurityException("FAILED TO CREATE DIRECTORY AT " + givenPath);
        }
    }

    public void handleDropTable(String line, String keyword, Database database) throws IOException {
        String givenName;

        try {
            givenName = clean(line, keyword);
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException(e.getMessage());
        }

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

        throw new FileNotFoundException("FILE NOT FOUND");
    }

    public ArrayList<String> splitInsertionColumns (String line, boolean splittingCols) throws StringIndexOutOfBoundsException {
        ArrayList<String> columns = new ArrayList<>();
        String tableName;

        if(splittingCols){
            try {
                tableName = line.substring(0, line.indexOf("(")).trim();
                line = line.substring(tableName.length()).trim();

                if(!(line.startsWith("(") && line.endsWith(")"))){
                    throw new StringIndexOutOfBoundsException();
                }

                line = line.substring(line.indexOf("(")+1,line.indexOf(")"));
                columns.add(tableName);

                String[] values = line.split(",");
                columns.addAll(Arrays.asList(values));

                return columns;
            } catch (StringIndexOutOfBoundsException e){
                throw new StringIndexOutOfBoundsException();
            }
        }

        try {
            if(!(line.startsWith("(") && line.endsWith(")"))){
                throw new StringIndexOutOfBoundsException();
            }

            line = line.substring(line.indexOf("(") + 1,line.indexOf(")"));

            String[] values = line.split(",");
            columns.addAll(Arrays.asList(values));

            return columns;
        } catch (Exception e){
            throw new StringIndexOutOfBoundsException();
        }
    }

    public void handleInsertInto(String line, String keyword, Database database) throws IOException, StringIndexOutOfBoundsException  {
        String cleanedLine, cleanedLine_v2, cleanedLine_v3;
        ArrayList<String> insertionColumns, insertionData;
        boolean tableExists = false;

        try {
            cleanedLine = clean(line, keyword);
            int VALUEIndex = cleanedLine.indexOf("VALUES");

            if(VALUEIndex == -1){
                throw new StringIndexOutOfBoundsException("MISSING EXPRESSION");
            }

            cleanedLine_v2 = cleanedLine.substring(0,VALUEIndex-1);
            insertionColumns = splitInsertionColumns(cleanedLine_v2, true);

            cleanedLine_v3 = cleanedLine.substring(VALUEIndex + "VALUES ".length());
            insertionData = splitInsertionColumns(cleanedLine_v3, false);

        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException(e.getMessage());
        }

        if(insertionColumns.size()-1 != insertionData.size()){
            throw new InsuficientDataProvidedException("INSUFICIENT DATA PROVIDED");
        }

        ArrayList<Table> tables = database.getTables();
        for(Table table : tables) {
            if(table.getTableName().equals(insertionColumns.get(0))){
                tableExists = true;
                insertionColumns.remove(0);

                if(table.getColumnsName().size() != insertionColumns.size() && table.getColumnsName().size() != insertionData.size()){
                    throw new InsuficientDataProvidedException("INSUFICIENT DATA PROVIDED");
                }

                for(int i = 0; i < table.getColumnsName().size(); i++){
                    if(!(table.getColumnsName().get(i).equals(insertionColumns.get(i)))){
                        throw new ColumnDoesNotMatch("COLUMN DOES NOT MATCH:" + insertionColumns.get(i));
                    }
                }

                table.appendData(insertionData);
                break;
            }
        }

        if(!tableExists){
            throw new NoSuchFileException("TABLE DOES NOT EXISTS");
        }
    }

    public void handleDeleteFrom(String line, String keyword){
    }

    public void handleUpdate(String line, String keyword){
    }

    public void handleSelect(String line, String keyword, Database database) throws StringIndexOutOfBoundsException, NoSuchFileException, ColumnDoesNotMatch {
        ArrayList<String> columns = new ArrayList<>();
        ArrayList<String> showingCol = new ArrayList<>();
        String cleanedLine, selectedColumns, selectedTable;
        boolean tableExists = false;

        try{
            cleanedLine = clean(line, keyword);
            selectedColumns = cleanedLine.substring(0,cleanedLine.indexOf("FROM")-1).trim();

            if(cleanedLine.contains("WHERE")){
                selectedTable = cleanedLine.substring(cleanedLine.indexOf("FROM ") + "FROM".length() + 1, cleanedLine.indexOf(" WHERE"));
            } else {
                selectedTable = cleanedLine.substring(cleanedLine.indexOf("FROM ") + "FROM".length() + 1).trim();
            }

            if(!selectedColumns.equals("*")){
                selectedColumns = "(" + selectedColumns + ")";
                columns = splitInsertionColumns(selectedColumns, false);
            }

        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException();
        }

        ArrayList<Table> tables = database.getTables();
        for(Table table : tables) {
            if (table.getTableName().equals(selectedTable)) {
                tableExists = true;

                if(!selectedColumns.equals("*")){
                    for(String tableCol : table.getColumnsName()){
                        for(String selecCol : columns) {
                            if(tableCol.equals(selecCol)){
                                showingCol.add(tableCol);
                            }
                        }
                    }

                    if(showingCol.size() < columns.size()){
                        throw new ColumnDoesNotMatch("COLUMN DOES NOT MATCH");
                    }

                    table.printData(showingCol);

                } else {
                    table.printData();
                }
                break;
            }
        }

        if(!tableExists){
            throw new NoSuchFileException("TABLE DOES NOT EXISTS");
        }
    }
}


// TODO: CREAR EL WHERE
// TODO: CHECAR EL SELECT