package com.example.atlanticbakery;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class inventory_class {

    connection_class cc = new connection_class();
    Connection con;

    public ArrayAdapter<String> fillAdapter(Activity activity, List<String> names){
        return new ArrayAdapter<>(activity, android.R.layout.simple_expandable_list_item_1, names);
    }

    public List<String> returnTransactionNumbers(Activity activity,String transactionNumber){
        final List<String>  result = new ArrayList<>();
        try {
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query2 = "SELECT DISTINCT transaction_number FROM " +
                        "tblproduction WHERE type IN ('Received Item','Transfer Item')" +
                        " AND CAST(date AS date)=(SELECT CAST(GETDATE() AS DATE)) AND status='Completed' AND " +
                        "transaction_number LIKE '%" + transactionNumber + "%' ORDER BY transaction_number";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query2);
                while (rs2.next()){
                    result.add(rs2.getString("transaction_number"));
                }
                con.close();
            }
        }catch (Exception ex){
            Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  result;
    }

    public List<String> returnAvailableItems(Activity activity, String datecreated){
        final List<String>  result = new ArrayList<>();
        try {
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query2 = "SELECT * FROM funcLoadInventoryItems('" + datecreated + "','','All')";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query2);
                while (rs2.next()){
                    result.add(rs2.getString("itemname"));
                }
                con.close();
            }
        }catch (Exception ex){
            Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  result;
    }

    public String returnTransactionType(Activity activity,String transactionNumber){
        String result = "";
        try {
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query2 = "SELECT type2 FROM tblproduction WHERE transaction_number='" + transactionNumber + "';";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query2);
                if(rs2.next()){
                    result = rs2.getString("type2");
                }
                con.close();
            }
        }catch (Exception ex){
            Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  result;
    }

    public void cancelRecTrans(Activity activity,String transactionNumber){
        String resultcolumnName = returnTransactionType(activity, transactionNumber);
        String columnName = "";
        switch (resultcolumnName){
            case "Received from Production" :
                columnName = "productionin";
                break;
            case "Received from Other Branch":
                columnName = "itemin";
                break;
            case  "Transfer Item":
                columnName = "transfer";
                break;
            case  "Received from Direct Supplier":
                columnName = "supin";
                break;
        }
        try {
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                CallableStatement callableStatement;
                callableStatement = con.prepareCall("{call cancelRecTrans(?)}");
                callableStatement.setString(1,transactionNumber);
                callableStatement.executeUpdate();
                callableStatement.close();

                String query = "SELECT item_name,quantity,charge FROM tblproduction WHERE transaction_number='" + transactionNumber + "';";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query);
                while (rs2.next()){
                    double quantity = rs2.getDouble("quantity"),
                            charge = rs2.getDouble("charge");
                    String item_name = rs2.getString("item_name");

                    String query2 = "UPDATE tblinvitems SET " + columnName + "-=" + quantity + ",charge-=" + charge + "," +
                            "archarge-=" + charge + ""
                            + (columnName.equals("transfer") ? "" : ",totalav-=" + quantity + "") + ",endbal" + (columnName.equals("transfer") ? "+" : "-")
                            + "=" + quantity + ",variance" + (columnName.equals("transfer") ? "-" : "+")  + "=" + quantity + " WHERE " +
                            "invnum=(SELECT TOP 1 inv_id FROM tblproduction WHERE transaction_number='" + transactionNumber + "') " +
                            "AND itemname='" + item_name + "';";
                    System.out.println(query2);
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(query2);
                }
                con.close();
                Toast.makeText(activity, "Transaction Completed", Toast.LENGTH_SHORT).show();

            }
        }catch (Exception ex){
            Toast.makeText(activity, "CancelRecTrans() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String checkStock(Activity activity, String transactionNumber){
        StringBuilder result = new StringBuilder();
        try{
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query = "SELECT a.item_name,a.quantity,b.endbal FROM tblproduction a " +
                        "JOIN tblinvitems b ON a.item_name = b.itemname  AND a.inv_id = b.invnum " +
                        "WHERE a.transaction_number='" + transactionNumber + "'  GROUP BY a.item_name,a.quantity,b.endbal " +
                        "HAVING SUM(a.quantity) > SUM(b.endbal);";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query);
                while (rs2.next()) {
                    result.append(rs2.getString("item_name")).append("/Ending Balance: (").append(rs2.getInt("endbal")).append(")/ ").append("Received Item: (").append(rs2.getInt("quantity")).append(")\n");
                }
            }
        }catch (Exception ex){
            Toast.makeText(activity, "checkStock() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result.toString();
    }

    public boolean checkEndingBalance(Activity activity, String itemName, Integer quantity){
        boolean result = false;
        try{
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query = "SELECT invid FROM tblinvitems a WHERE itemname='" + itemName + "' AND " +
                        "invnum=(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invid DESC) " +
                        "GROUP BY a.invid " +
                        "HAVING SUM(" + quantity + ") <=" +
                        " SUM(a.endbal)";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query);
                result = (rs2.next());
            }
        }catch (Exception ex){
            Toast.makeText(activity, "checkStock() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public String returnLatestInventoryDate(Activity activity){
        String result = "";
        try{
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query = "SELECT TOP 1 CAST(a.datecreated AS date) [date] FROM tblinvsum a ORDER BY a.invsumid DESC";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query);
                if(rs2.next()){
                    result = rs2.getString("date");
                }
            }
        }catch (Exception ex){
            Toast.makeText(activity, "checkStock() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

}
