package com.example.atlanticbakery;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;
public class item_class {
    connection_class cc = new connection_class();
    Connection con;
    public boolean checkItemName(Activity activity, String value){
        boolean result = false;
        con = cc.connectionClass(activity);

        if(con == null){
            Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
        }else{
            try{
                String query = "SELECT itemid FROM tblitems WHERE itemname='" + value + "' AND status=1;";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                result = rs.next();
                con.close();
            }catch (Exception ex){
                Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        return  result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Boolean checkItemNameStock(Activity activity, String itemname){
        boolean result = false;
        try {
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query = "SELECT ISNULL(endbal,0) [endbal] FROM tblinvitems WHERE itemname='" + itemname + "' AND" +
                        " invnum=(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC);";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if(rs.next()){
                    if(rs.getDouble("endbal") > 0){
                        result = true;
                    }
                }
                if(result){
                    SharedPreferences sharedPreferences = activity.getSharedPreferences("LOGIN",MODE_PRIVATE);
                    int userid = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("userid", "")));

                    String query2 = "SELECT ISNULL(postype,'') FROM tblusers WHERE systemid=" + userid + " AND postype !='Coffee Shop';";
                    Statement stmt2 = con.createStatement();
                    ResultSet rs2 = stmt2.executeQuery(query2);
                    if (rs2.next()) result = true;
                    else {
                        result = false;
                    }
                    con.close();
                }
            }
        }
        catch (Exception ex){
            Toast.makeText(activity, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public double returnItemNamePrice(Activity activity, String itemName){
        double result = 0.00;
        try {
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
                result = 0.00;
            } else {
                String query = "SELECT price FROM tblitems WHERE status=1 AND itemname='" + itemName + "'";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if(rs.next()){
                    result = Double.parseDouble(rs.getString("price"));
                }
                con.close();
            }
        }
        catch (Exception ex){
            result = 0.00;
        }
        return result;
    }

}
