package com.example.atlanticbakery;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import  java.sql.SQLException;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class connection_class {
    public static SharedPreferences sharedPreferences;
    @SuppressLint("NewAPI")
    public Connection connectionClass(Activity activity) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;
        try {
            sharedPreferences = activity.getSharedPreferences("CONFIG",MODE_PRIVATE);
            String IPAddress = Objects.requireNonNull(sharedPreferences.getString("IPAddress", ""));
            String Username = Objects.requireNonNull(sharedPreferences.getString("Username", ""));
            String Password = Objects.requireNonNull(sharedPreferences.getString("Password", ""));
            String Database = Objects.requireNonNull(sharedPreferences.getString("Database", ""));

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + IPAddress + "/" + Database + ";user=" + Username + ";password=" + Password + ";";
//            ConnectionURL = "jdbc:jtds:sqlserver://192.168.42.1/AKPOS;user=admin;password=admin;";
            connection = DriverManager.getConnection(ConnectionURL);
        } catch (SQLException se) {
            Log.e("error here 1: ", Objects.requireNonNull(se.getMessage()));
        } catch (ClassNotFoundException e) {
            Log.e("error here 2: ", e.toString());
        } catch (Exception e) {
            Log.e("error here 3: ", Objects.requireNonNull(e.getMessage()));
        }
        return connection;
    }

}
