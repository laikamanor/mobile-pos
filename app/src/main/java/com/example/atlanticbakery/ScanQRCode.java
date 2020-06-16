package com.example.atlanticbakery;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import  java.sql.Connection;
import  java.sql.DriverManager;
import  java.sql.ResultSet;
import  java.sql.SQLException;
import  java.sql.Statement;
import java.util.Objects;

public class ScanQRCode extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static TextView resultText;
    public  static  final String SHARED_PREFS = "sharedPrefs";
    String fromPrefs;

    Connection con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_q_r_code);
        resultText =  findViewById(R.id.lblResult);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnCart = findViewById(R.id.btnAddCart);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ScanCode.class));
            }
        });
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public  void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (resultText.getText().equals("Result: N/A")){
            Toast.makeText(this, "Scan item first", Toast.LENGTH_SHORT).show();
        }
        else{
            loadData();
            if(fromPrefs.isEmpty()){
                editor.putString("text",resultText.getText() + ",").apply();
            }else{
                editor.putString("text",fromPrefs + resultText.getText() + ",").apply();
            }
            Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show();
            resultText.setText("Result: N/A");
        }
    }
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        fromPrefs = sharedPreferences.getString("text","");
        assert fromPrefs != null;
        if(fromPrefs.isEmpty()){
            Toast.makeText(this, "No item fetch", Toast.LENGTH_SHORT).show();
        }else{
            String ids = fromPrefs.substring(0,fromPrefs.length()-1);
            String[] words;
            words = ids.split(",");


            for (String word: words){
//                Toast.makeText(this, word, Toast.LENGTH_LONG).show();

                try {
                    con = connectionClass();
                    if (con == null) {
                        Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();

                    } else {
                        String query = "SELECT itemname FROM tblitems WHERE itemid=" + word + " AND status=1;";
                        Statement stmt = con.createStatement();

                        ResultSet rs = stmt.executeQuery(query);
                        if (rs.next()) {
                            Toast.makeText(this, rs.getString("itemname"), Toast.LENGTH_SHORT).show();
                            con.close();
                        } else {
                            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception ex) {
                    Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public  void deleteData(){
        loadData();

        if(fromPrefs.isEmpty()){
            Toast.makeText(this, "No item to pay", Toast.LENGTH_SHORT).show();
        }else{
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("text").apply();
            Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NewAPI")
    public Connection connectionClass() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://192.168.137.1/AKPOS;user=admin;password=admin;";
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