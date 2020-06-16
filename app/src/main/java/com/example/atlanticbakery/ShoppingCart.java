package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingCart extends AppCompatActivity {
    Connection con;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("text","1,2,3,4").apply();
        loadData();
    }

    @SuppressLint({"SetTextI18n", "ResourceType"})
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs",MODE_PRIVATE);
        String fromPrefs = sharedPreferences.getString("text","");
        assert fromPrefs != null;
        if(fromPrefs.isEmpty()){
            Toast.makeText(this, "No item fetch", Toast.LENGTH_SHORT).show();
        }else{
            String ids = fromPrefs.substring(0,fromPrefs.length()-1);
            String[] words;
            words = ids.split(",");


            LinearLayout layout = findViewById(R.id.parentLayout);
            layout.removeAllViews();

            TextView headerText = new TextView(this);
            headerText.setText("Shopping Cart");

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 150);
            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            LinearLayout.LayoutParams lpQuantity = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams lpDiscount = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams lpDiscountType = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpQuantity.setMargins(20,0,20,20);
            lpDiscountType.setMargins(20,20,20,20);
            lpDiscount.setMargins(20,0,20,20);
            layout.setOrientation(LinearLayout.VERTICAL);
            headerText.setLayoutParams(lp);
            headerText.setTextColor(Color.BLACK);
            headerText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            headerText.setGravity(Gravity.CENTER_HORIZONTAL);
            headerText.setTextSize(50);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20,20,20,20);
            layout.addView(headerText);

            for (String word: words){
//                Toast.makeText(this, word, Toast.LENGTH_LONG).show();

                try {
                    con = connectionClass();
                    if (con == null) {
                        Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();

                    } else {
                        String query = "SELECT itemname,itemid,price FROM tblitems WHERE itemid=" + word + " AND status=1;";
                        Statement stmt = con.createStatement();

                        ResultSet rs = stmt.executeQuery(query);

                        if (rs.next()) {
                            LinearLayout layout1 = new LinearLayout(this);
                            lp1.setMargins(20,20,20,20);
                            layout1.setLayoutParams(lp1);
                            layout1.setOrientation(LinearLayout.VERTICAL);
                            layout1.setTag("layout" + rs.getString("itemid"));
                            layout.addView(layout1);

                            TextView itemname = new TextView(this);
                            itemname.setText(rs.getString("itemname") + "(" + rs.getString("price") + ")");
                            itemname.setPadding(20,20,20,20);
                            itemname.setLayoutParams(lp1);
                            itemname.setTag("itemid" + rs.getString("itemid"));

                            itemname.setLayoutParams(lp);
                            itemname.setTextColor(Color.BLACK);

                            itemname.setTextSize(30);
                            layout1.addView(itemname);

//                            EditText quantity = new EditText(this);
//                            quantity.setLayoutParams(lpQuantity);
//                            quantity.setBackgroundColor(Color.BLACK);
//                            quantity.setTextColor(Color.WHITE);
//                            quantity.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//                            quantity.setText("0");
//                            quantity.setPadding(10,10,10,10);
//                            layout1.addView(quantity);

                            ElegantNumberButton quantity = new ElegantNumberButton(this);
                            quantity.setLayoutParams(lpQuantity);
                            quantity.setTag("quantity" + rs.getString("itemid"));
                            layout1.addView(quantity);

                            EditText txtDiscount = new EditText(this);
                            txtDiscount.setLayoutParams(lpDiscount);
                            txtDiscount.setTag("discount" + rs.getString("itemid"));
                            txtDiscount.setText("0");
                            txtDiscount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                            txtDiscount.setBackgroundColor(Color.GRAY);
                            txtDiscount.setTextColor(Color.BLACK);
                            txtDiscount.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            layout1.addView(txtDiscount);

                            EditText txtTotalPrice = new EditText(this);
                            txtTotalPrice.setLayoutParams(lpDiscount);
                            txtTotalPrice.setTag("totalprice" + rs.getString("itemid"));
                            txtTotalPrice.setText("0.00");
                            txtTotalPrice.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                            txtTotalPrice.setBackgroundColor(Color.GRAY);
                            txtTotalPrice.setTextColor(Color.BLACK);
                            txtTotalPrice.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            layout1.addView(txtTotalPrice);

                            View view = new View(this);
                            view.setLayoutParams(lp2);
                            view.setBackgroundColor(Color.BLACK);
                            layout1.addView(view);



                            con.close();
                        } else {
                            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception ex) {
                    Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            LinearLayout layoutPay = new LinearLayout(this);
            layoutPay.setLayoutParams(lp);

            Spinner cmbDiscountType = new Spinner(this);
            cmbDiscountType.setLayoutParams(lpDiscountType);
            List<String> discounts = new ArrayList<>();
            try {
                con = connectionClass();
                if (con == null) {
                    Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();

                } else {
                    String query = "SELECT disname FROM tbldiscount WHERE status=1;";
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()){
                        discounts.add(rs.getString("disname"));
                    }
                    con.close();
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,discounts);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbDiscountType.setAdapter(adapter);
                }
            }
            catch (Exception ex){
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
            }


            layoutPay.addView(cmbDiscountType);

            layout.addView(layoutPay);
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