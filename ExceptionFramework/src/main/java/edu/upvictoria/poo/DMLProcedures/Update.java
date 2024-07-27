package edu.upvictoria.poo.DMLProcedures;

import edu.upvictoria.poo.DMLProcedures.Where.Tree;
import edu.upvictoria.poo.DMLProcedures.Where.Where;
import edu.upvictoria.poo.Database;
import edu.upvictoria.poo.Table;
import edu.upvictoria.poo.Utils;
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
        ArrayList<Integer> columnsToModify = new ArrayList<>();
        ArrayList<ArrayList <Object>> dataToModify, dataToModifyCopy;

        Table table = database.getTableByName(tokensWithoutKeywords.get(0));
        String setClausule = tokensWithoutKeywords.get(1);
        setTokens = parseSetClausule(setClausule);

        if(validKeywords.contains("WHERE")){
            whereTokens = Utils.getWhereTokens(tokensWithoutKeywords.get(2));
            whereTokens = Where.infixToPostfix(whereTokens);
            Tree.Node root = Where.createTree(whereTokens);
            dataToModify = Where.evaluateTree(root, table.getData(), table);
        } else {
            dataToModify = table.getData();
        }

        dataToModifyCopy = new ArrayList<>(dataToModify);

        // se obtienen las posiciones en el array de las columnas
        for(int i = 0; i < setTokens.size(); i+=2){
            columnsToModify.add(table.getColumnPos(setTokens.get(i)));
        }

        int i = 1, counter = 0;
        for(ArrayList<Object> datum : dataToModify){
            for(Integer columnPosition : columnsToModify){
                datum.set(columnPosition, setTokens.get(i));
                i += 2;
            }
            i = 1;
            counter++;
        }

        table.updateData(dataToModifyCopy, dataToModify);
        System.out.println(counter + " ROWS UPDATED");
    }

    /**
     * Separa la clausula set primero en comas y luego en '=', el lado izquierdo y derecho de la asignación son
     * almacenados
     * @param setClausule String que contiene a la cláusula set
     * @return Array con la columna y el valor a modificar, siempre es un numero par
     * @throws SQLSyntaxException Cuando se haya escrito mal la sintaxis de la clausula
     */
    public ArrayList<String> parseSetClausule (String setClausule) throws SQLSyntaxException {
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

            setTokens.add(splitAssignments[0]);
            setTokens.add(splitAssignments[1]);
        }

        return setTokens;
    }
}
