package edu.upvictoria.poo;

// OBJECT IS THE WAY

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Table {
    private final String tableName;
    private final File tableFile;
    private final ArrayList<Column> columns;
    private final ArrayList<ArrayList<Object>> data =  new ArrayList<>();

    //para crear nueva tabla
    public Table(File tableFile, ArrayList<Column> columns){
        this.tableFile = tableFile;
        this.columns = columns;
        int i = tableFile.getName().indexOf('.');
        this.tableName = tableFile.getName().substring(0,i);
    }

    //para recuperar tabla ya existente
    public Table(File tableFile) throws FileSystemException {
        this.tableFile = tableFile;
        int a = tableFile.getName().indexOf('.');
        this.tableName = tableFile.getName().substring(0,a);

        this.columns = new ArrayList<>();
        boolean columnsId = true;
        Charset charset = StandardCharsets.UTF_8;

        try {
            BufferedReader br = new BufferedReader(new FileReader(tableFile, charset));
            String line;

            while((line = br.readLine()) != null){
                String[] rowValues = line.split(",");

                if(columnsId){
                    for (String rowValue : rowValues) {
                        String[] columnData = rowValue.trim().split(" ");
                        String constraint = null;

                        if (columnData.length > 2) {
                            constraint = columnData[2] + " " + columnData[3];
                        }
                        Column column = new Column(columnData[0], columnData[1], constraint);
                        columns.add(column);
                    }

                    columnsId = false;
                } else {
                    ArrayList<Object> objectValues = new ArrayList<>(Arrays.asList(rowValues));
                    this.data.add(objectValues);
                }
            }
        } catch (IOException e){
            throw new FileSystemException("TABLE DOES NOT EXIST");
        } catch (RuntimeException e){
            throw new FileSystemException("TABLE FORMAT ERROR");
        }
    }

    public File getTableFile() {
        return tableFile;
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public ArrayList<ArrayList<Object>> getData() {
        return data;
    }

    public void appendDataToTable(ArrayList<String> data, ArrayList<String> columnNames) throws IOException {
        ArrayList<Object> newData = new ArrayList<>();

        for(int i = 0; i < this.columns.size(); i++){
            newData.add("\0");
        }

        int j = 0;

        for(String columnName : columnNames){
            for(int i = 0; i < this.columns.size(); i++){
                Column aux = this.columns.get(i);
                if(columnName.equals(aux.getName())){
                    newData.set(i,data.get(j));
                    j++;
                }
            }

            if(j == data.size()){
                break;
            }
        }

        this.data.add(newData);
        writeDataToFile();
    }

    public void writeDataToFile() throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        StringBuilder line = new StringBuilder();

        try{
            FileWriter fw = new FileWriter(this.tableFile, charset);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            for(int i = 0; i < this.columns.size(); i++){
                Column column = this.columns.get(i);
                String constraint;
                if(column.getConstraint() == null){
                    constraint = "\0";
                } else {
                    constraint = column.getConstraint();
                }

                line.append(column.getName()).append(" ").append(column.getType()).append(" ").append(constraint);

                if(i != this.columns.size()-1){
                    line.append(",");
                }

            }

            out.println(line);
            out.flush();
            line = new StringBuilder();

            for(ArrayList<Object> row : this.data){
                for(int i = 0; i < row.size(); i++){
                    line.append(row.get(i));

                    if(i != row.size()-1){
                        line.append(",");
                    }
                }
                out.println(line);
                out.flush();
                line = new StringBuilder();
            }
        } catch (IOException e) {
            throw new IOException("TABLE DOES NOT EXISTS");
        }
    }

    //PARA LA PRIMERA INSERCION DE NOMBRES DE COLUMNA EN LA TABLA
    public void writeDataToFile(ArrayList<String> rowData) throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        StringBuilder line = new StringBuilder();

        try{
            FileWriter fw = new FileWriter(this.tableFile, charset);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            for(int i = 0; i < rowData.size(); i++){
                line.append(rowData.get(i));

                if(i != (rowData.size() - 1)){
                    line.append(",");
                }
            }

            out.println(line);
            out.flush();
        } catch (IOException e) {
            throw new IOException("TABLE DOES NOT EXISTS");
        }
    }

    public void printData(ArrayList<ArrayList<Object>> data){
        for(Column col : columns){
            System.out.print("| " + col.getName() + "\t");
        }
        System.out.println("|");

        for (ArrayList<Object> datum : data) {
            for (Object object : datum) {
                System.out.print("| " + object.toString() + "\t");
            }
            System.out.println("|");
            for (int i = 0; i < datum.size(); i++) {
                System.out.print("+------------");
            }
            System.out.println("+");
        }
    }

    public void printData(ArrayList<String> columns, ArrayList<ArrayList<Object>> data) {
        for(String colName : columns){
            System.out.print("| " + colName + "\t");
        }
        System.out.println("|");
        for (int i = 0; i < columns.size(); i++) {
            System.out.print("+------------");
        }
        System.out.println("+");

        // imprimirmos datos
        for (ArrayList<Object> datum : data) {
            int j = 0;
            for (int i = 0; i < datum.size(); i++) {
                if(this.columns.get(i).getName().equals(columns.get(j))){
                    Object object = datum.get(i);
                    System.out.print("| " + object.toString() + "\t");
                    j++;

                    if(j == columns.size()){
                        break;
                    }
                }
            }
            System.out.println("|");
            for (int i = 0; i < columns.size(); i++) {
                System.out.print("+------------");
            }
            System.out.println("+");
        }
    }

    public ArrayList<String> getColumnsName(){
        ArrayList<String> columnsName = new ArrayList<>();

        for(Column column : columns){
            columnsName.add(column.getName());
        }

        return columnsName;
    }

    public int getColumnPos(String columnName){
        ArrayList<String> columnsName = getColumnsName();

        for(int i = 0; i < columnsName.size(); i++){
            if(columnName.equals(columnsName.get(i))){
                return i;
            }
        }

        return -1;
    }
}
