package com.example.atlanticbakery;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class user_class {
    connection_class cc = new connection_class();
    security_class sc = new security_class();
    Connection con;
    public Integer checkUsernamePassword(Activity activity,String colName, String username, String password){
        int result = 0;
        Connection con = cc.connectionClass(activity);
        if(con == null){
            Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
        }else{
            try{
                String EncodedPassword = sc.Encode(password);

                String query = "select systemid from tblusers WHERE " + colName + "='" + username + "' AND password='" + EncodedPassword + "';";
                Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                    result = Integer.parseInt(rs.getString("systemid"));
                }
            }catch (SQLException ex){
                Toast.makeText(activity,ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String returnWorkgroup(Activity activity){
        String result = null;
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("LOGIN",MODE_PRIVATE);
            int userid = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("userid", "")));
            con = cc.connectionClass(activity);
            if (con == null) {
                Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query = "SELECT workgroup FROM tblusers WHERE systemid=" + userid + ";";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                   result = rs.getString("workgroup");
                }
            }
        }catch (Exception ex){
            Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  result;
    }

    public Date getServerDate(){
        Date result = new Date();
        try{
            String query = "SELECT GETDATE();";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                result = rs.getDate(0);
            }
            con.close();
        }catch (Exception ignored){
        }
        return  result;
    }

    public void checkCutOff(Activity activity, String username){
        boolean hasLoggedIn = false;
        String status = null;
        Date dateFrom = new Date();
        Date currentDate = getServerDate();
        con = cc.connectionClass(activity);
        if(con == null){
            Toast.makeText(activity, "Check Your Internet Connection",Toast.LENGTH_SHORT).show();
        }else{
            try{
                String query = "SELECT status,date FROM tblcutoff WHERE userid=(SELECT systemid FROM tblusers WHERE username='" + username + "') ORDER BY cid DESC;";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                    hasLoggedIn = true;
                    status = rs.getString("status");
                    dateFrom = rs.getDate("date");
                }
                con.close();

                if(hasLoggedIn){
                    if(status.equals("In Active") && dateFrom != currentDate){
                        insertCutOff(activity,username);
                    }
                }else{
                    insertCutOff(activity,username);
                }

            }catch (Exception ex){
                Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void insertCutOff(Activity activity, String username){
        con = cc.connectionClass(activity);
        if(con == null){
            Toast.makeText(activity, "Check Your Internet Connection",Toast.LENGTH_SHORT).show();
        }else{
            try{
                String query = "INSERT INTO tblcutoff (userid,status,date) VALUES ((SELECT systemid FROM tblusers WHERE username='" + username + "'), 'Active',GETDATE());";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.close();
            }catch (Exception ex){
                Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void insetLoginLogs(Activity activity, String username){
        con = cc.connectionClass(activity);
        if(con == null){
            Toast.makeText(activity, "Check Your Internet Connection",Toast.LENGTH_SHORT).show();
        }else {
            try {
                String query = "INSERT INTO tbllogin (username,login,logout,datelogin) VALUES ('" + username + "'," +
                        "(SELECT CONVERT(VARCHAR, GETDATE(),8)),(SELECT CONVERT(VARCHAR, GETDATE(),8))," +
                        "(SELECT GETDATE()));";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.close();
            }catch (Exception ex){
                Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
