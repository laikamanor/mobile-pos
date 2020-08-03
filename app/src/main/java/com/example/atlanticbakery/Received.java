package com.example.atlanticbakery;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

public class Received extends AppCompatActivity {
    Connection con;
    connection_class cc = new connection_class();
    inventory_class ic = new inventory_class();
    Received_SQLite myDb;
    long mLastClickTime = 0;
    AutoCompleteTextView txtSearch;
    String selectedBranch = "";
    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received);
        myDb = new Received_SQLite(this);
        myDb.truncateTable();
        String TransactionType = getIntent().getStringExtra("type");
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>" + TransactionType + " </font>"));
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        String latestInventoryDate = ic.returnLatestInventoryDate(Received.this);
        txtSearch = findViewById(R.id.txtSearch);
        txtSearch.setAdapter(ic.fillAdapter(Received.this, ic.returnAvailableItems(Received.this, latestInventoryDate)));

        loadItems(latestInventoryDate,"");
        loadSelectedItem();

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                String latestInventoryDate = ic.returnLatestInventoryDate(Received.this);
                AutoCompleteTextView txtSearch = findViewById(R.id.txtSearch);
                loadItems(latestInventoryDate, txtSearch.getText().toString());
            }
        });

        Button btnProceed = findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData(false, "+", "productionin");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveData(boolean hasCb, String operator, String columnName){
        try {
            String branchCode = returnBranchCode();
            String transactionNumber = returnTransactionNumber(columnName);
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                Cursor cursor = myDb.getAllData();
                while (cursor.moveToNext()){
                    double quantity = cursor.getDouble(2);
                    String itemName = cursor.getString(1);
                    String query1 = "UPDATE tblinvitems SET " + columnName + "+=" +  quantity + (operator.equals("+") ? ",totalav+=" + quantity : "") + ",endbal" + operator + "=" + quantity + ",variance" + (operator.equals("+") ? "-" : "+") + "=" + quantity + " WHERE itemname='" + itemName + "' AND invnum=(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC) AND area='Sales';" ;
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(query1);

                    String sapNumber = "To Follow";
                    String remarks = "done";

                    SharedPreferences sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
                    String procby = Objects.requireNonNull(sharedPreferences.getString("username", ""));
                    String type1 = "Received Item";
                    String fromBranch = (!selectedBranch.equals("") ? selectedBranch : branchCode + "(SLS)");
                    String toBranch = branchCode + (columnName == "pullout" ? "(PRD)" : "(SLS)");
                    String sapdoc = "IT";
                    String type2 = "Received from Production";

                    String query2 = "INSERT INTO tblproduction (transaction_number,inv_id,item_code,item_name,category,quantity,reject,charge,sap_number,remarks,date,processed_by,type,area,status,transfer_from,transfer_to,typenum,type2) VALUES ('" + transactionNumber + "',(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC),(SELECT itemcode FROM tblitems WHERE itemname='" + itemName + "'),'" + itemName + "',(SELECT category FROM tblitems WHERE itemname='" + itemName + "')," + quantity + ",0,0,'" + sapNumber + "','" + remarks + "',(SELECT GETDATE()),'" + procby + "','" + type1 + "','Sales','Completed','" + fromBranch + "','" + toBranch + "','" + sapdoc + "','" + type2 + "');" ;
                    Statement stmt2 = con.createStatement();
                    stmt2.executeUpdate(query2);
                    con.close();

                    Toast.makeText(Received.this, "Transaction Completed", Toast.LENGTH_SHORT).show();
                    myDb.truncateTable();
                }
            }
        }catch (Exception ex){
            Toast.makeText(this, "saveData() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String returnTransactionNumber(String columnName){
        String result = "";
        String type2 = getSupportActionBar().getTitle().toString();
        String type;
        String template = "";
        int prodCount = 0;
        String totalZero = "";
        String branchCode = "";

        if(type2 == "Received from Adjustment"){
            type = "Adjustment Item";
        }else if(type2 == "Transfer Out") {
            type = "Transfer Item";
        }else{
            type = "Received Item";
        }

        if(columnName == "productionin") {
            template = "RECPROD - ";
        }else if(columnName == "itemin"){
            template = "RECBRA - ";
        }else if(columnName == "supin"){
            template = "RECSUPP - ";
        }else if(columnName == "adjustmentin"){
            template = "ADJIN - ";
        }else if(columnName == "pullout") {
            template = "TRA - ";
        }
        String queryProd = "Select ISNULL(MAX(transaction_id),0) +1 [counter] from tblproduction WHERE area='Sales' AND type='" + type + "' AND type2='" + type2 + "';";
        try{
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                Statement statement = con.createStatement();
                ResultSet resultSet = statement.executeQuery(queryProd);
                if(resultSet.next()){
                    prodCount = resultSet.getInt("counter");
                }


                if(prodCount < 1000000){
                    String cselectcount_result = Integer.toString(prodCount);
                    Integer cselectcount_resultLength = 6 - cselectcount_result.length();
                    while (0 < cselectcount_resultLength){
                        totalZero += "0";
                        cselectcount_resultLength-= 1;
                    }
                }
                result = template + branchCode + totalZero + prodCount;
            }
        }catch (Exception ex){
            Toast.makeText(this, "returnTransactionNumber() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        System.out.println(result);
        return result;
    }

    public String returnBranchCode() {
        String result = "";
        try {
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String queryBranch = "SELECT branchcode FROM tblbranch WHERE main='1';";
                Statement statement2 = con.createStatement();
                ResultSet resultSet2 = statement2.executeQuery(queryBranch);
                if (resultSet2.next()) {
                    result = resultSet2.getString("branchcode") + " ";
                }
                con.close();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "returnBranchCode() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadItems(String datecreated, final String itemName){
        try{
            LinearLayout layoutItems = findViewById(R.id.linearList);
            layoutItems.removeAllViews();
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query = "SELECT * FROM funcLoadInventoryItems('" + datecreated + "','" + itemName + "','All')";
                Statement statement = con.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                int totalItems = 0;
                while (resultSet.next()){
                    totalItems += 1;
                    LinearLayout layout = new LinearLayout(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setLayoutParams(layoutParams);
                    layoutItems.addView(layout);

                    TextView txtItemname = new TextView(this);
                    LinearLayout.LayoutParams txtItemNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    txtItemNameParams.setMargins(40,10,10,0);
                    txtItemname.setTextSize(15);
                    txtItemname.setText(resultSet.getString("itemname"));
                    txtItemname.setLayoutParams(txtItemNameParams);
                    layout.addView(txtItemname);

                    final String selectedItemName = resultSet.getString("itemname");
                    Button btnAdd = new Button(this);
                    LinearLayout.LayoutParams btnCancelParams = new LinearLayout.LayoutParams(200,80);
                    btnCancelParams.setMargins(40,0,10,10);
                    int colorPrimary = getResources().getColor(R.color.colorPrimary);
                    btnAdd.setBackgroundColor(colorPrimary);
                    btnAdd.setTextColor(Color.WHITE);
                    btnAdd.setText("Add");
                    final int generatedCancelID = View.generateViewId();
                    btnAdd.setId(generatedCancelID);
                    btnAdd.setLayoutParams(btnCancelParams);

                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();

                            AlertDialog.Builder dialogAddQuantity = new AlertDialog.Builder(Received.this);
                            dialogAddQuantity.setCancelable(false);
                            dialogAddQuantity.setTitle("Atlantic Bakery");
                            LinearLayout layout = new LinearLayout(Received.this);
                            layout.setPadding(40,0,40,40);

                            TextView lblAddQuantity = new TextView(Received.this);
                            lblAddQuantity.setText(selectedItemName);
                            lblAddQuantity.setTextSize(20);
                            lblAddQuantity.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            layout.addView(lblAddQuantity);

                            final EditText txtAddQuantity = new EditText(Received.this);
                            txtAddQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                            txtAddQuantity.setHint("Enter Quantity");
                            layout.addView(txtAddQuantity);

                            dialogAddQuantity.setView(layout);
                            dialogAddQuantity.setCancelable(false);

                            layout.setOrientation(LinearLayout.VERTICAL);
                            dialogAddQuantity.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean itemExist = myDb.checkItemName(selectedItemName);
                                    if(txtAddQuantity.getText().toString().isEmpty()){
                                        Toast.makeText(Received.this, "Please input at least 1", Toast.LENGTH_SHORT).show();
                                    }else if(Integer.parseInt(txtAddQuantity.getText().toString()) <= 0){
                                        Toast.makeText(Received.this, "Please input at least 1", Toast.LENGTH_SHORT).show();
                                    }else if(itemExist){
                                        Toast.makeText(Received.this, "Item is already in Selected Items", Toast.LENGTH_SHORT).show();
                                    }else{
                                        double quantity = Double.parseDouble(txtAddQuantity.getText().toString());
                                        boolean isSuccess = myDb.insertData(selectedItemName, quantity);
                                        String msg = (isSuccess ? "Item Added" : "Error Occurred");
                                        Toast.makeText(Received.this, msg, Toast.LENGTH_SHORT).show();
                                        loadSelectedItem();
                                    }
                                }
                            });

                            dialogAddQuantity.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialogAddQuantity.show();
                        }
                    });

                    layout.addView(btnAdd);

                    View line = new View(this);
                    LinearLayout.LayoutParams lineLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                    line.setLayoutParams(lineLayout);
                    line.setBackgroundColor(Color.BLACK);
                    layout.addView(line);
                }
                con.close();
                if(totalItems == 0){
                    TextView txtItemname = new TextView(this);
                    LinearLayout.LayoutParams txtItemNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    txtItemNameParams.setMargins(40,10,10,0);
                    txtItemname.setTextSize(15);
                    txtItemname.setText("No item found");
                    txtItemname.setLayoutParams(txtItemNameParams);
                    layoutItems.addView(txtItemname);
                }
                TextView txtHeaderItems = findViewById(R.id.txtList);
                txtHeaderItems.setText("List of Items (" + totalItems + ")");
            }

        }catch (Exception ex){
            Toast.makeText(this,"loadItems() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("SetTextI18n")
    public void loadSelectedItem() {
        LinearLayout layoutItems = findViewById(R.id.linearSelected);
        layoutItems.removeAllViews();
        con = cc.connectionClass(this);
        if (con == null) {
            Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
        } else {
            final Cursor cursor = myDb.getAllData();
            int totalItems = 0;
            while (cursor.moveToNext()) {
                totalItems += 1;
                LinearLayout layout = new LinearLayout(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(layoutParams);
                layoutItems.addView(layout);

                TextView txtItemname = new TextView(this);
                LinearLayout.LayoutParams txtItemNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                txtItemNameParams.setMargins(40, 10, 10, 0);
                txtItemname.setTextSize(15);
                txtItemname.setText(cursor.getString(1));
                txtItemname.setLayoutParams(txtItemNameParams);
                layout.addView(txtItemname);

                TextView txtQuantity = new TextView(this);
                LinearLayout.LayoutParams txtQuantityParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                txtQuantityParams.setMargins(40, 10, 10, 0);
                txtQuantity.setTextSize(15);
                txtQuantity.setText(cursor.getString(2) + " pcs.");
                txtQuantity.setLayoutParams(txtQuantityParams);
                layout.addView(txtQuantity);

                LinearLayout.LayoutParams btnCancelParams = new LinearLayout.LayoutParams(200,80);
                btnCancelParams.setMargins(40,0,10,10);

                final Button btnEdit = new Button(this);
                btnEdit.setBackgroundColor(Color.BLUE);
                btnEdit.setTextColor(Color.WHITE);
                btnEdit.setText("Edit");
                final int generatedEditID = View.generateViewId();
                btnEdit.setId(generatedEditID);
                btnEdit.setLayoutParams(btnCancelParams);
                btnEdit.setTag(cursor.getString(0));

                final String selectedItemName = cursor.getString(1);
                final String selectedQuantity = cursor.getString(2);
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        AlertDialog.Builder dialogAddQuantity = new AlertDialog.Builder(Received.this);
                        dialogAddQuantity.setCancelable(false);
                        dialogAddQuantity.setTitle("Atlantic Bakery");
                        LinearLayout layout = new LinearLayout(Received.this);
                        layout.setPadding(40,0,40,40);

                        TextView lblAddQuantity = new TextView(Received.this);
                        lblAddQuantity.setText(selectedItemName);
                        lblAddQuantity.setTextSize(20);
                        lblAddQuantity.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        layout.addView(lblAddQuantity);

                        final EditText txtAddQuantity = new EditText(Received.this);
                        txtAddQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                        txtAddQuantity.setText(selectedQuantity);
                        txtAddQuantity.setHint("Enter Quantity");
                        layout.addView(txtAddQuantity);

                        dialogAddQuantity.setView(layout);
                        dialogAddQuantity.setCancelable(false);

                        layout.setOrientation(LinearLayout.VERTICAL);
                        dialogAddQuantity.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(txtAddQuantity.getText().toString().isEmpty()){
                                    Toast.makeText(Received.this, "Please input at least 1", Toast.LENGTH_SHORT).show();
                                }else if(Integer.parseInt(txtAddQuantity.getText().toString()) <= 0){
                                    Toast.makeText(Received.this, "Please input at least 1", Toast.LENGTH_SHORT).show();
                                }else{
                                    double quantity = Double.parseDouble(txtAddQuantity.getText().toString());
                                    String id = btnEdit.getTag().toString();
                                    myDb.updateData(id, quantity);
                                    Toast.makeText(Received.this, "Quantity Updated", Toast.LENGTH_SHORT).show();
                                    loadSelectedItem();
                                }
                            }
                        });

                        dialogAddQuantity.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialogAddQuantity.show();
                    }
                });
                layout.addView(btnEdit);



                final Button btnRemove = new Button(this);
                btnRemove.setBackgroundColor(Color.RED);
                btnRemove.setTextColor(Color.WHITE);
                btnRemove.setText("Remove");
                final int generatedRemoveID = View.generateViewId();
                btnRemove.setId(generatedRemoveID);
                btnRemove.setLayoutParams(btnCancelParams);
                btnRemove.setTag(cursor.getString(0));
                btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialogConfirmation = new AlertDialog.Builder(Received.this);
                        dialogConfirmation.setCancelable(false);
                        dialogConfirmation.setTitle("Atlantic Bakery");
                        dialogConfirmation.setMessage("Are you sure want to remove " + selectedItemName + "?");
                        dialogConfirmation.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String id = btnRemove.getTag().toString();
                                Integer deletedItem = myDb.deleteData(id);
                                String msg = (deletedItem < 0 ? "Item not removed" : "Item removed");
                                Toast.makeText(Received.this, msg, Toast.LENGTH_SHORT).show();
                                loadSelectedItem();
                            }
                        });
                        dialogConfirmation.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialogConfirmation.show();
                    }
                });

                layout.addView(btnRemove);

                View line = new View(this);
                LinearLayout.LayoutParams lineLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                line.setLayoutParams(lineLayout);
                line.setBackgroundColor(Color.BLACK);
                layout.addView(line);
            }
            if (totalItems == 0) {
                TextView txtItemname = new TextView(this);
                LinearLayout.LayoutParams txtItemNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                txtItemNameParams.setMargins(40, 10, 10, 0);
                txtItemname.setTextSize(15);
                txtItemname.setText("No item found");
                txtItemname.setLayoutParams(txtItemNameParams);
                layoutItems.addView(txtItemname);
            }
            TextView txtHeaderItems = findViewById(R.id.txtSelected);
            txtHeaderItems.setText("Selected Items (" + totalItems + ")");
        }
    }
}