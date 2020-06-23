package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
    String ginagawaMo;
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
                        final String query = "SELECT itemname,itemid,price FROM tblitems WHERE itemid=" + word + " AND status=1;";
                        Statement stmt = con.createStatement();

                        ResultSet rs = stmt.executeQuery(query);

                        if (rs.next()) {
                            LinearLayout layout1 = new LinearLayout(this);
                            lp1.setMargins(20,20,20,20);
                            layout1.setLayoutParams(lp1);
                            layout1.setOrientation(LinearLayout.VERTICAL);
                            layout1.setTag("layout" + rs.getString("itemid"));
                            layout.addView(layout1);

                            final TextView itemname = new TextView(this);
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

                            final ElegantNumberButton quantity = new ElegantNumberButton(this);
                            quantity.setLayoutParams(lpQuantity);
                            quantity.setTag("quantity" + rs.getString("itemid"));
                            quantity.setNumber("1");
                            final int generatedQuantityID = View.generateViewId();
                            quantity.setId(generatedQuantityID);
                            quantity.setOnClickListener(new ElegantNumberButton.OnClickListener() {
                                @Override
                                public void onClick(View view) {

//                                    Dim priceBefore As Double = CDbl(CInt(txtquantity.Text) * price)
//                                    txtamount.Text = CDbl(priceBefore - (CDbl(txtdiscount.Text) / 100) * priceBefore).ToString("n2")
                                    String itemid = quantity.getTag().toString().replace("quantity","");
                                    View root = getWindow().getDecorView().getRootView();
                                    EditText currentAmount = root.findViewWithTag("totalprice" + itemid);
                                    EditText currentDiscount = root.findViewWithTag("discount" + itemid);
                                    if(Double.parseDouble(currentAmount.getText().toString()) < 0){
                                        double final_amount = (Double.parseDouble(quantity.getNumber()) * getItemPrice(Integer.parseInt(itemid)));
                                        currentAmount.setText(Double.toString(final_amount));
                                    }else{
                                        double priceBefore = Double.parseDouble(quantity.getNumber()) * getItemPrice(Integer.parseInt(itemid));
                                        currentAmount.setText(Double.toString(priceBefore));
                                    }
                                }
                            });
                            layout1.addView(quantity);

                            final EditText txtDiscount = new EditText(this);
                            txtDiscount.setLayoutParams(lpDiscount);
                            txtDiscount.setTag("discount" + rs.getString("itemid"));
                            txtDiscount.setText("0.00");
                            txtDiscount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            txtDiscount.setBackgroundColor(Color.GRAY);
                            txtDiscount.setTextColor(Color.BLACK);
                            txtDiscount.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            final int generatedDiscountPercentID = View.generateViewId();
                            txtDiscount.setId(generatedDiscountPercentID);
                            txtDiscount.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    String currentItemID = txtDiscount.getTag().toString().replace("discount","");
                                    View root = getWindow().getDecorView().getRootView();
                                    ElegantNumberButton currentQuantity = root.findViewWithTag("quantity" + currentItemID);
                                    EditText currentAmount = root.findViewWithTag("totalprice" + currentItemID);
                                    double discountPercent = 0.00;
                                    double currentPrice = getItemPrice(Integer.parseInt(currentItemID));
                                    double priceBefore = Double.parseDouble(currentQuantity.getNumber()) * currentPrice;
                                        try{
                                            if (Integer.parseInt(currentQuantity.getNumber().toString()) < 0){
                                                txtDiscount.setText("0");
                                            }
                                            else if(Integer.parseInt(currentQuantity.getNumber().toString()) > 0){

                                                if(Double.parseDouble(txtDiscount.getText().toString()) < 25){
                                                    discountPercent = Double.parseDouble(txtDiscount.getText().toString());

                                                    double totalAmount = (priceBefore - (discountPercent / 100) * priceBefore);

                                                    currentAmount.setText(Double.toString(totalAmount));
                                                }else if(Double.parseDouble(txtDiscount.getText().toString()) >= 25){
                                                    discountPercent = Double.parseDouble(txtDiscount.getText().toString());

                                                    double totalAmount = (priceBefore - (discountPercent / 100) * priceBefore);

                                                    currentAmount.setText(Double.toString(totalAmount));
                                                }
                                            }
                                        }catch (Exception ex){
                                            if(txtDiscount.getText().equals("")){
                                                currentAmount.setText(Double.toString(priceBefore));
                                            }
                                        }
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            });
                            layout1.addView(txtDiscount);

                            final EditText txtTotalPrice = new EditText(this);
                            txtTotalPrice.setLayoutParams(lpDiscount);
                            txtTotalPrice.setTag("totalprice" + rs.getString("itemid"));
                            txtTotalPrice.setText("0.00");
                            txtTotalPrice.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                            txtTotalPrice.setBackgroundColor(Color.GRAY);
                            txtTotalPrice.setTextColor(Color.BLACK);
                            txtTotalPrice.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            final int generatedTotalPriceID = View.generateViewId();
                            txtTotalPrice.setId(generatedTotalPriceID);
                            txtTotalPrice.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    try {

                                        String currentItemID = txtTotalPrice.getTag().toString().replace("totalprice","");
                                        double currentPrice = getItemPrice(Integer.parseInt(currentItemID));
                                        View root = getWindow().getDecorView().getRootView();
                                        ElegantNumberButton currentQuantity = root.findViewWithTag("quantity" + currentItemID);
                                        EditText currentDiscount = root.findViewWithTag("discount" + currentItemID);
                                        double discountPercent = Double.parseDouble(currentDiscount.getText().toString());
                                        double priceBefore = Double.parseDouble(currentQuantity.getNumber()) * currentPrice;
                                        if(Double.parseDouble(txtTotalPrice.getText().toString()) > priceBefore){
                                            double final_discount = ((priceBefore - Double.parseDouble(txtTotalPrice.getText().toString())) /  priceBefore) * 100;
                                            currentDiscount.setText(Double.toString(final_discount));
                                        }else{
                                            double final_discount = ((priceBefore - Double.parseDouble(txtTotalPrice.getText().toString())) /  priceBefore) * 100;
                                            currentDiscount.setText(Double.toString(final_discount));
                                        }
                                    }catch (Exception ex){

                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            });

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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, discounts);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbDiscountType.setAdapter(adapter);
                }
            }
            catch (Exception ex){
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
            }

            layoutPay.addView(cmbDiscountType);
            layout.addView(layoutPay);

//            LinearLayout layoutSubmit = new LinearLayout(this);
//
//            Button btnPay = new Button(this);
//            btnPay.setLayoutParams(lp1);
//            btnPay.setText("PLACE ORDER");
//            btnPay.setBackgroundColor(Color.GREEN);
//            btnPay.setTextColor(Color.WHITE);
//
//            layoutSubmit.setLayoutParams(lp);

        }
    }

    public void toastMsg(String v){
        Toast.makeText(this,v, Toast.LENGTH_SHORT).show();
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

    @SuppressLint("NewAPI")
    public Connection connectionClass() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
//            ConnectionURL = "jdbc:jtds:sqlserver://192.168.137.1/AKPOS;user=admin;password=admin;";
            ConnectionURL = "jdbc:jtds:sqlserver://192.168.42.1/AKPOS;user=admin;password=admin;";
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