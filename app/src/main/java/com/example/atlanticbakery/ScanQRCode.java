package com.example.atlanticbakery;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
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
//    public  static  final String SHARED_PREFS = "sharedPrefs";
//    String fromPrefs;

    Connection con;
    DatabaseHelper myDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myDb = new DatabaseHelper(this);


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
                try {
                    saveData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public  void saveData() throws SQLException {
        if (resultText.getText().equals("Result: N/A")){
            Toast.makeText(this, "Scan item first", Toast.LENGTH_SHORT).show();
        }
        else if(!checkItemID(Integer.parseInt(resultText.getText().toString()))){
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
        }
        else{
            double price = getItemPrice(Integer.parseInt(resultText.getText().toString()));
            boolean isInserted = myDb.insertData(Integer.parseInt(resultText.getText().toString()),1,0.00,price, 0);
            if(isInserted){
                Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show();
                viewAll();
            }else{
                Toast.makeText(this, "Item not added", Toast.LENGTH_SHORT).show();
            }
            resultText.setText("Result: N/A");
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

    public boolean checkItemID(Integer value) throws SQLException {
        boolean result = false;
        con = connectionClass();
        if (con == null) {
            Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();

        } else {
            String query = "SELECT itemname FROM tblitems WHERE itemid=" + value + " AND status=1;";
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                result = true;
                con.close();
            } else {
                result = false;
            }
        }
        return result;
    }

    public double getItemPrice(Integer id){
        double result = 0.00;
        try {
            con = connectionClass();
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
                result = 0.00;
            } else {
                String query = "SELECT price FROM tblitems WHERE status=1 AND itemid=" + id;
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

    public void viewAll(){
        Cursor result = myDb.getAllData();
        if(result.getCount() < 0){
            showMessage("Error","Not found");
        }else{
            StringBuilder buffer = new StringBuilder();
            while (result.moveToNext()){
                buffer.append("ID: ").append(result.getString(0)).append("\n");
                buffer.append("ITEM ID: ").append(result.getString(1)).append("\n");
                buffer.append("PRICE: ").append(result.getString(2)).append("\n");
                buffer.append("DISCOUNT: ").append(result.getString(3)).append("\n");
                buffer.append("TOTAL PRICE: ").append(result.getString(4)).append("\n");
                buffer.append("FREE: ").append(result.getString(5)).append("\n");
            }
//        Show all data
            showMessage("Data", buffer.toString());
        }
    }

    public  void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}