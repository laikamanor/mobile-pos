package com.example.atlanticbakery;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;

public class user_class {
    connection_class cc = new connection_class();
    security_class sc = new security_class();
    Connection con;
    public Integer checkUsernamePassword(Activity activity,String colName, String username, String password){
        Connection con = cc.connectionClass(activity);
        int result = 0;
        if(cc == null){
            Toast.makeText(activity, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
        }else{
            try{
                String EncodedPassword = sc.Encode(password);
                String query = "select systemid from tblusers WHERE " + colName + "='" + username + "'AND password='" + EncodedPassword + "';";
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



}
