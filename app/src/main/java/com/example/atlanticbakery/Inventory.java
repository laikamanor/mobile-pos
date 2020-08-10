package com.example.atlanticbakery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Inventory extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    long mLastClickTime = 0;
    connection_class cc = new connection_class();
    Connection con;
    TextView txtDate;
    AutoCompleteTextView itemName;
    TextView lblBeginningBalance;
    TextView lblProductionIn;
    TextView lblBranchIn;
    TextView lblSupplierIn;
    TextView lblAdjustmentIn;
    TextView lblConversionIn;
    TextView lblTotalAvailable;
    TextView lblTransferOut;
    TextView lblCounterOut;
    TextView lblARCharge;
    TextView lblARSales;
    TextView lblAdjustmentOut;
    TextView lblConversionOut;
    TextView lblEndingBalance;
    TextView lblActualEndingBalance;
    TextView lblVariance;
    TextView lblShortOver;
    TextView lblShortOverAmount;
    TextView lblCounterOutAmount;
    TextView lblARChargeAmount;
    TextView lblARSalesAmount;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    ui_class uic = new ui_class();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);


        NavigationView navigationView = findViewById(R.id.nav);
        drawerLayout = findViewById(R.id.navDrawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                boolean result = false;
                Intent intent;
                switch (menuItem.getItemId()){
                    case R.id.nav_scanItem :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(Inventory.this, QRCode.class));
                        finish();
                        break;
                    case R.id.nav_exploreItems :
                        Toast.makeText(Inventory.this, "Event is clicked", Toast.LENGTH_SHORT).show();
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        break;
                    case R.id.nav_shoppingCart :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(Inventory.this, ShoppingCart.class));
                        finish();
                        break;
                    case R.id.nav_receivedProduction :
                        result = true;
                        intent = new Intent(getBaseContext(), Received.class);
                        intent.putExtra("type", "Received from Production");
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_receivedBranch :
                        result = true;
                        intent = new Intent(getBaseContext(), Received.class);
                        intent.putExtra("type", "Received from Other Branch");
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_receivedSupplier :
                        result = true;
                        intent = new Intent(getBaseContext(), Received.class);
                        intent.putExtra("type", "Received from Direct Supplier");
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_transferOut :
                        result = true;
                        intent = new Intent(getBaseContext(), Received.class);
                        intent.putExtra("type", "Transfer Out");
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_adjusmentIn :
                        result = true;
                        intent = new Intent(getBaseContext(), Received.class);
                        intent.putExtra("type", "Received from Adjustment");
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_adjustmentOut :
                        result = true;
                        intent = new Intent(getBaseContext(), Received.class);
                        intent.putExtra("type", "Adjustment Out");
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_inventory :
                        result = true;
                        intent = new Intent(getBaseContext(), Inventory.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                return result;
            }
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Inventory</font>"));
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
        itemName = findViewById(R.id.txtItemName);
        lblBeginningBalance = findViewById(R.id.txtBegBal);
        lblProductionIn = findViewById(R.id.txtProductionIn);
        lblBranchIn = findViewById(R.id.txtBranchIn);
        lblSupplierIn = findViewById(R.id.txtSupplierIn);
        lblAdjustmentIn = findViewById(R.id.AdjustmentIn);
        lblConversionIn = findViewById(R.id.ConversionIn);
        lblTotalAvailable = findViewById(R.id.TotalAvailable);
        lblTransferOut = findViewById(R.id.TransferOut);
        lblCounterOut = findViewById(R.id.CounterOut);
        lblARCharge = findViewById(R.id.ARCharge);
        lblARSales = findViewById(R.id.ARSales);
        lblAdjustmentOut = findViewById(R.id.AdjustmentOut);
        lblConversionOut = findViewById(R.id.ConversionOut);
        lblEndingBalance = findViewById(R.id.EndingBalance);
        lblActualEndingBalance = findViewById(R.id.ActualEndingBalance);
        lblVariance = findViewById(R.id.Variance);
        lblShortOver = findViewById(R.id.ShortOver);
        lblShortOverAmount = findViewById(R.id.ShortOverAmount);
        lblCounterOutAmount = findViewById(R.id.CounterOutAmount);
        lblARChargeAmount = findViewById(R.id.ARChargeAmount);
        lblARSalesAmount = findViewById(R.id.ARSalesAmount);
        final Button btnSearch = findViewById(R.id.btnSearch);
        txtDate = findViewById(R.id.txtDate);
        itemName.setAdapter(fillInventory(returnInventory()));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                showDatePickerDialog();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayAdapter<String> fillInventory(List<String> names){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, names);
    }

    public List<String> returnInventory(){
        final List<String>  result = new ArrayList<>();
        String datee = latestInventoryNumber();
        try {
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                txtDate.setText(datee);
                String query2 = "SELECT itemname FROM tblinvitems WHERE invnum=(SELECT TOP 1 invnum FROM tblinvsum WHERE CAST(datecreated AS date)='" + txtDate.getText().toString() + "')";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query2);
                while (rs2.next()){
                    result.add(rs2.getString("itemname"));
                }
                con.close();
            }
        }catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  result;
    }

    public Double returnPrice(String itemeName){
        double result = 0;
        try {
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query2 = "SELECT price FROM tblitems WHERE itemname='" + itemeName + "';";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query2);
                if(rs2.next()){
                    result = rs2.getDouble("price");
                }
                con.close();
            }
        }catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  result;
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public void loadData(){
        try {
            con = cc.connectionClass(Inventory.this);
            if (con == null) {
                Toast.makeText(Inventory.this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else  {
                int hasStock = 0;
                String query2 = "SELECT * FROM tblinvitems WHERE itemname='" + itemName.getText().toString() + "' AND " +
                        "invnum=(SELECT TOP 1 invnum FROM tblinvsum WHERE CAST(datecreated AS date)='" + txtDate.getText().toString() + "');";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query2);
                while (rs2.next()){
                    hasStock += 1;
                    lblBeginningBalance.setText("Beginning Balance: " + rs2.getDouble("begbal"));
                    lblProductionIn.setText("Production In: " + rs2.getDouble("productionin"));
                    lblBranchIn.setText("Branch In: " + rs2.getDouble("itemin"));
                    lblSupplierIn.setText("Supplier In: " + rs2.getDouble("supin"));
                    lblAdjustmentIn.setText("Adjustment In: " + rs2.getDouble("adjustmentin"));
                    lblConversionIn.setText("Conversion In: " + rs2.getDouble("convin"));
                    lblTotalAvailable.setText("Total Qty. Available: " + rs2.getDouble("totalav"));

                    lblTransferOut.setText("Transfer Out: " + rs2.getDouble("transfer"));
                    lblCounterOut.setText("Counter Out: " + rs2.getDouble("ctrout"));
                    lblARCharge.setText("A.R Charge: " + rs2.getDouble("archarge"));
                    lblARSales.setText("A.R Sales: " + rs2.getDouble("arsales"));
                    lblAdjustmentOut.setText("Adjustment Out: " + rs2.getDouble("pullout"));
                    lblConversionOut.setText("Conversion Out: " + rs2.getDouble("convout"));
                    lblEndingBalance.setText("Ending Balance: " + rs2.getDouble("endbal"));
                    lblActualEndingBalance.setText("Actual Ending Balance: " + rs2.getDouble("actualendbal"));
                    int variance = rs2.getInt("actualendbal") - rs2.getInt("endbal");
                    lblVariance.setText("Variance: " + variance);
                    double price = returnPrice(itemName.getText().toString());
                    double shortOverAmount = price * variance;
                    double counterOutAmount = price * rs2.getDouble("ctrout");
                    double arSalesAmount = price * rs2.getDouble("arsales");
                    double arChargeAmount = price * rs2.getDouble("archarge");
                    String shortOver = "";
                    if(variance > 0){
                        shortOver = "Over";
                    }else if(variance == 0){
                        shortOver = "";
                    }else{
                        shortOver = "Short";
                    }
                    lblShortOver.setText("Short/Over: " + shortOver);
                    lblShortOverAmount.setText("Short/Over Amount: " + shortOverAmount);
                    lblCounterOutAmount.setText("Counter Out Amount: " + counterOutAmount);
                    lblARSalesAmount.setText("A.R Sales Amount: " + arSalesAmount);
                    lblARChargeAmount.setText("A.R Charge Amount: " + arChargeAmount);
                }
                con.close();
                if(hasStock <= 0){
                    Toast.makeText(this, "Item Not found", Toast.LENGTH_SHORT).show();
                    lblBeginningBalance.setText("Beginning Balance: 0");
                    lblProductionIn.setText("Production In: 0");
                    lblBranchIn.setText("Branch In: 0");
                    lblSupplierIn.setText("Supplier In: 0");
                    lblAdjustmentIn.setText("Adjustment In: 0");
                    lblConversionIn.setText("Conversion In: 0");
                    lblTotalAvailable.setText("Total Qty. Available: 0");
                    lblTransferOut.setText("Transfer Out: 0");
                    lblCounterOut.setText("Counter Out: 0");
                    lblARCharge.setText("A.R Charge: 0");
                    lblARSales.setText("A.R Sales: 0");
                    lblAdjustmentOut.setText("Adjustment Out: 0");
                    lblConversionOut.setText("Conversion Out: 0");
                    lblEndingBalance.setText("Ending Balance: 0");
                    lblActualEndingBalance.setText("Actual Ending Balance: 0");
                    lblVariance.setText("Variance: 0");
                    lblShortOver.setText("Short/Over:");
                    lblShortOverAmount.setText("Short/Over Amount: 0");
                    lblCounterOutAmount.setText("Counter Out Amount: 0");
                    lblARSalesAmount.setText("A.R Sales Amount: 0");
                    lblARChargeAmount.setText("A.R Charge Amount: 0");
                }
            }
        }catch (Exception ex){
            Toast.makeText(Inventory.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month += 1;
        txtDate.setText(month + "/" + dayOfMonth + "/" + year);
        loadData();
    }

    public String latestInventoryNumber(){
        String result = "";
        try {
            con = cc.connectionClass(Inventory.this);
            if (con == null) {
                Toast.makeText(Inventory.this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {

                String query2 = "SELECT CAST(datecreated AS date) [datecreated] FROM tblinvsum ORDER BY invsumid DESC;";
                Statement stmt2 = con.createStatement();
                ResultSet rs = stmt2.executeQuery(query2);
                if(rs.next()){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    result = dateFormat.format(rs.getDate("datecreated"));
                }else{
                    result = new SimpleDateFormat("MM/d/yyyy", Locale.getDefault()).format(new Date());
                }
            }
        }catch (Exception ex){
            Toast.makeText(this,"latestInventoryNumber() " + ex.getMessage(), Toast.LENGTH_SHORT ).show();
        }
        return result;
    }
}