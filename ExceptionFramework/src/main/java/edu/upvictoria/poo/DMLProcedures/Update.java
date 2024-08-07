package edu.upvictoria.poo.DMLProcedures;

import edu.upvictoria.poo.*;
import edu.upvictoria.poo.DMLProcedures.Where.Tree;
import edu.upvictoria.poo.DMLProcedures.Where.Where;
import edu.upvictoria.poo.exceptions.SQLSyntaxException;
import edu.upvictoria.poo.exceptions.TableNotFoundException;

import java.util.ArrayList;

public class Update {
    private String query;
    private Database database;
    private ArrayList<String> validKeywords;

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private void setValidKeywords(){
        validKeywords = new ArrayList<>();
        validKeywords.add("UPDATE");
        validKeywords.add("SET");

        if (query.contains("WHERE")){
            validKeywords.add("WHERE");
        }
    }

    public void handle() throws TableNotFoundException, SQLSyntaxException {
        setValidKeywords();
        Utils.hasValidKeywords(query, validKeywords);

        String cleanedLine = Utils.clean(query);
        ArrayList<String> tokensWithoutKeywords = Utils.splitByWords(cleanedLine, validKeywords, false);
        ArrayList<String> setTokens, whereTokens;
        ArrayList<ArrayList <Object>> dataToModify, dataToModifyCopy;

        Table table = database.getTableByName(tokensWithoutKeywords.get(0));

        if(validKeywords.contains("WHERE")){
            whereTokens = Utils.getWhereTokens(tokensWithoutKeywords.get(2));
            whereTokens = Where.infixToPostfix(whereTokens);
            Tree.Node root = Where.createTree(whereTokens);
            dataToModify = Where.evaluateTree(root, table.getData(), table);
        } else {
            dataToModify = table.getData();
        }

        dataToModifyCopy = new ArrayList<>(dataToModify);
        String setClausule = tokensWithoutKeywords.get(1);
        ArrayList<Integer> columnsToModifyPos = new ArrayList<>();
        int counter = 0;

        for(ArrayList <Object> rowToModify : dataToModifyCopy){
            setTokens = parseSetClausule(setClausule, rowToModify, table);

            // se obtienen las posiciones en el array de las columnas
            for(int i = 0; i < setTokens.size(); i+=2){
                int aux = table.getColumnPos(setTokens.get(i));
                if(aux == -1){
                    throw new TableNotFoundException("COLUMN " + setTokens.get(i) + " DOES NOT EXISTS");
                }
                columnsToModifyPos.add(aux);
            }

            // segun las columnas a modificar, se actualizan los campos con esas posiciones con los nuevos datos
            int aux = 1;
            for (Integer columnPosition : columnsToModifyPos) {
                rowToModify.set(columnPosition, setTokens.get(aux));
                aux += 2;
            }

            counter++;
            columnsToModifyPos.clear();
        }

        table.updateData(dataToModifyCopy, dataToModify);
        System.out.println(counter + " ROW(S) UPDATED");
    }

    /**
     * Separa la clausula set primero en comas y luego en '=', el lado izquierdo y derecho de la asignación son
     * almacenados
     * @param setClausule String que contiene a la cláusula set
     * @return Array con la columna y el valor a modificar, siempre es un numero par
     * @throws SQLSyntaxException Cuando se haya escrito mal la sintaxis de la clausula
     */
    public ArrayList<String> parseSetClausule (String setClausule, ArrayList <Object> rowToModify, Table table) throws SQLSyntaxException {
        ArrayList<String> setTokens = new ArrayList<>();
        String[] rawAssignations = setClausule.split(",");

        for (String rawAssignation : rawAssignations) {
            String[] splitAssignments = rawAssignation.split("=");
            if (splitAssignments.length != 2) {
                throw new SQLSyntaxException(setClausule + " IS NOT A VALID SET CLAUSULE");
            }

            splitAssignments[0] = splitAssignments[0].trim();
            splitAssignments[1] = splitAssignments[1].trim();

            if(splitAssignments[1].startsWith("'") && splitAssignments[1].endsWith("'")){
                splitAssignments[1] = splitAssignments[1].substring(1, splitAssignments[1].length()-1).trim();
            }

            // por si las comillas simples no matchean
            if((!splitAssignments[1].startsWith("'") && splitAssignments[1].endsWith("'"))
                || (splitAssignments[1].startsWith("'") && !splitAssignments[1].endsWith("'"))){
                throw new SQLSyntaxException("UNMATCHED SINGLE QUOTE (')");
            }

            // comprobar si el valor derecho tiene operadores aritmeticos
            ArrayList<String> tokens = Utils.getWhereTokens(splitAssignments[1]);

            if(tokens.size() > 1){
                tokens = Where.infixToPostfix(tokens);
                Tree.Node root = Where.createTree(tokens);
                splitAssignments[1] = Where.evaluateSubTree(root, rowToModify, table);
            }

            if(tokens.size() == 1){
                if(Function.analyzeNode(tokens.get(0))){
                    splitAssignments[1] = Function.parseFunctions(tokens.get(0));
                }
            }

            setTokens.add(splitAssignments[0]);
            setTokens.add(splitAssignments[1]);
        }

        return setTokens;
    }
}
