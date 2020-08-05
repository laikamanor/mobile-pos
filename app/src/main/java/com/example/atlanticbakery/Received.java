package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
        String title = Objects.requireNonNull(getSupportActionBar().getTitle()).toString().trim();
        String rectrans = (title.equals("Transfer Out") || title.equals("Adjustment Out") ? "trans" : "rec");
        loadItems(rectrans, latestInventoryDate,"");
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

                String title = Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar()).getTitle()).toString().trim();
                String rectrans = (title.equals("Transfer Out") || title.equals("Adjustment Out") ? "trans" : "rec");
                loadItems(rectrans, latestInventoryDate, txtSearch.getText().toString());
            }
        });

        Button btnProceed = findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                final String title = Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar()).getTitle()).toString().trim();
                if(title.equals("Transfer Out") || title.equals("Received from Direct Supplier")){
                    final AlertDialog.Builder dialogConfirmation = new AlertDialog.Builder(Received.this);
                    dialogConfirmation.setCancelable(false);
                    LinearLayout layout = new LinearLayout(Received.this);
                    layout.setPadding(40,40,40,40);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final AutoCompleteTextView txtSpinner = new AutoCompleteTextView(Received.this);
                    txtSpinner.setHint((title.equals("Received from Direct Supplier") ? "Select Supplier" : "Select Branch"));
                    txtSpinner.setAdapter(fillAdapter(fillSpinner(title)));
                    layout.addView(txtSpinner);
                    dialogConfirmation.setView(layout);

                    dialogConfirmation.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean isExist = brasupExist(title, txtSpinner.getText().toString().trim());
                            if(txtSpinner.getText().toString().isEmpty()){
                                Toast.makeText(Received.this, "Text is empty", Toast.LENGTH_SHORT).show();
                            }else if(!isExist){
                                Toast.makeText(Received.this, "'" + txtSpinner.getText().toString().trim() + "' is not exist", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                selectedBranch = txtSpinner.getText().toString();
                                clickk(title);
                            }
                        }
                    });

                    dialogConfirmation.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialogConfirmation.show();
                }else{
                    clickk(title);
                }
            }
        });
    }

    public boolean brasupExist(String title, String value){
        boolean result = false;
        try {
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String transbraQuery = "SELECT branchcode AS result FROM vLoadBranches WHERE status=1 AND main !=1 AND branch='" + value + "';";
                String supQuery = "SELECT name  AS result FROM tblcustomers WHERE type='Supplier' AND status=1 AND name='" + value + "';";
                String query = "";
                switch (title) {
                    case "Received from Other Branch":
                    case "Transfer Out":
                        query = transbraQuery;
                        break;
                    case "Received from Direct Supplier":
                        query = supQuery;
                        break;
                }
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                result = (rs.next());
            }
            con.close();
        }catch (Exception ex){
            Toast.makeText(Received.this,"brasupExist() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void clickk(String title){
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(Received.this);
        LinearLayout layout = new LinearLayout(Received.this);
        layout.setPadding(40,40,40,40);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView lblSAPNumber = new TextView(Received.this);
        lblSAPNumber.setText("SAP #:");
        lblSAPNumber.setTextSize(15);
        lblSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(lblSAPNumber);

        final EditText txtSAPNumber = new EditText(Received.this);
        final CheckBox toFollow = new CheckBox(Received.this);
        final CheckBox chckAddSAP = new CheckBox(Received.this);
        toFollow.setText("To Follow");


        if(title.equals("Received from Adjustment") || title.equals("Adjustment Out")){
            chckAddSAP.setText("Add SAP");
            chckAddSAP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        toFollow.setEnabled(true);
                        txtSAPNumber.setEnabled(true);
                    }else{
                        toFollow.setEnabled(false);
                        txtSAPNumber.setEnabled(false);
                    }
                    toFollow.setChecked(false);
                    txtSAPNumber.setText("");
                }
            });
            layout.addView(chckAddSAP);
            toFollow.setEnabled(false);
            txtSAPNumber.setEnabled(false);
        }else{
            toFollow.setEnabled(true);
            txtSAPNumber.setEnabled(true);
        }

        toFollow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                txtSAPNumber.setEnabled((!isChecked));
                txtSAPNumber.setText("");
            }
        });
        layout.addView(toFollow);

        txtSAPNumber.setTextSize(15);
        txtSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
        txtSAPNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(6);
        txtSAPNumber.setFilters(fArray);
        layout.addView(txtSAPNumber);

        TextView lblRemarks = new TextView(Received.this);
        lblRemarks.setText("Remarks:");
        lblRemarks.setTextSize(15);
        lblRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(lblRemarks);

        final EditText txtRemarks = new EditText(Received.this);
        txtRemarks.setTextSize(15);
        txtRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(txtRemarks);

        myDialog.setView(layout);

        myDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!toFollow.isChecked() && txtSAPNumber.getText().toString().equals("") && chckAddSAP.isChecked()){
                    Toast.makeText(Received.this, "SAP # field is empty", Toast.LENGTH_SHORT).show();
                }else if(!toFollow.isChecked() && txtSAPNumber.getText().toString().length() < 6 && chckAddSAP.isChecked()){
                    Toast.makeText(Received.this, "SAP # should 6 numbers", Toast.LENGTH_SHORT).show();
                }
                else if(txtRemarks.getText().toString().equals("")){
                    Toast.makeText(Received.this, "Remarks field is empty", Toast.LENGTH_SHORT).show();
                }else{
                    String sapNumber;
                    sapNumber = (toFollow.isChecked() ? "To Follow" : txtSAPNumber.getText().toString());
                    String columnName = "";
                    String operator = "";
                    String title = Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar()).getTitle()).toString().trim();
                    switch (title) {
                        case "Received from Production":
                            columnName = "productionin";
                            operator = "+";
                            break;
                        case "Received from Other Branch":
                            columnName = "itemin";
                            operator = "+";
                            break;
                        case "Received from Direct Supplier":
                            columnName = "supin";
                            operator = "+";
                            break;
                        case "Received from Adjustment":
                            columnName = "adjustmentin";
                            operator = "+";
                            break;
                        case "Adjustment Out":
                            columnName = "pullout";
                            operator = "-";
                            break;
                        case "Transfer Out":
                            columnName = "transfer";
                            operator = "-";
                            break;
                    }
                    saveData( operator, columnName,sapNumber, txtRemarks.getText().toString());
                }

            }
        });

        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        myDialog.show();
    }

    public ArrayAdapter<String> fillAdapter(List<String> names){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, names);
    }

    public List<String> fillSpinner(String rectrans) {
        List<String> result = new ArrayList<>();
        try {
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String transbraQuery = "SELECT branchcode AS result FROM vLoadBranches WHERE status=1 AND main !=1";
                String supQuery = "SELECT name  AS result FROM tblcustomers WHERE type='Supplier' AND status=1";
                String query = "";
                switch (rectrans) {
                    case "Transfer Out":
                    case "Received from Other Branch":
                        query = transbraQuery;
                        break;
                    case "Received from Direct Supplier":
                        query = supQuery;
                        break;
                }
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    result.add(rs.getString("result"));
                }
                con.close();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "returnDiscounts() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveData(String operator, String columnName, String sapNumber, String remarks) {
        try {
            String transactionNumber = returnTransactionNumber(columnName);
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                Cursor cursor = myDb.getAllData();
                while (cursor.moveToNext()) {
                    double quantity = cursor.getDouble(2);
                    String itemName = cursor.getString(1);
                    String query1 = "UPDATE tblinvitems SET " + columnName + "+=" + quantity + (operator.equals("+") ? ",totalav+=" + quantity : "") + ",endbal" + operator + "=" + quantity + ",variance" + (operator.equals("+") ? "-" : "+") + "=" + quantity + " WHERE itemname='" + itemName + "' AND invnum=(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC) AND area='Sales';";
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(query1);

                    SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                    String procby = Objects.requireNonNull(sharedPreferences.getString("username", ""));

                    String title = Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar()).getTitle()).toString().trim();
                    String type;
                    switch (title) {
                        case "Received from Adjustment":
                            type = "Adjustment Item";
                            break;
                        case "Transfer Out":
                            type = "Transfer Item";
                            break;
                        case ("Adjustment Out"):
                            type = "Adjustment Out Item";
                            break;
                        default:
                            type = "Received Item";
                            break;
                    }
                    String fromBranch = (!selectedBranch.equals("") ? "'" + selectedBranch + "'" : "");
                    String sapdoc = (title.equals("Received from Direct Supplier") ? "GRPO" : "IT");

                    String query2 = "INSERT INTO tblproduction (transaction_number,inv_id,item_code,item_name,category,quantity,reject,charge,sap_number,remarks,date,processed_by,type,area,status,transfer_from,transfer_to,typenum,type2) VALUES ('" + transactionNumber + "',(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC),(SELECT itemcode FROM tblitems WHERE itemname='" + itemName + "'),'" + itemName + "',(SELECT category FROM tblitems WHERE itemname='" + itemName + "')," + quantity + ",0,0,'" + sapNumber + "','" + remarks + "',(SELECT GETDATE()),'" + procby + "','" + type + "','Sales','Completed'," + (fromBranch.equals("") ? "(SELECT branchcode + ' (SLS)' FROM tblbranch WHERE main='1')" : fromBranch ) + ",(SELECT branchcode + '" + (columnName.equals("pullout") ? " (PRD)" : " (SLS)") + "' FROM tblbranch WHERE main='1'),'" + sapdoc + "','" + title + "');";
                    Statement stmt2 = con.createStatement();
                    stmt2.executeUpdate(query2);
                    con.close();

                    Toast.makeText(Received.this, "Transaction Completed", Toast.LENGTH_SHORT).show();
                    myDb.truncateTable();
                    loadSelectedItem();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this, "saveData() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String returnTransactionNumber(String columnName){
        String result = "";
        String type2 = Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar()).getTitle()).toString().trim();
        String type;
        String template = "";
        int prodCount = 0;
        StringBuilder totalZero = new StringBuilder();
        String branchCode = "";

        switch (type2) {
            case "Received from Adjustment":
                type = "Adjustment Item";
                break;
            case "Transfer Out":
                type = "Transfer Item";
                break;
            case ("Adjustment Out"):
                type = "Adjustment Out Item";
                break;
            default:
                type = "Received Item";
                break;
        }

        switch (columnName) {
            case "productionin":
                template = "RECPROD - ";
                break;
            case "itemin":
                template = "RECBRA - ";
                break;
            case "supin":
                template = "RECSUPP - ";
                break;
            case "adjustmentin":
                template = "ADJIN - ";
                break;
            case "pullout":
                template = "ADJOUT - ";
                break;
            case "transfer":
                template = "TRA - ";
                break;
        }
        String queryProd = "Select ISNULL(MAX(transaction_id),0) +1 [counter] from tblproduction WHERE area='Sales' AND type='" + type + "' AND type2='" + type2 + "';";
        String queryBra = "SELECT branchcode FROM tblbranch WHERE main='1'";
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

                Statement statement2 = con.createStatement();
                ResultSet resultSet2 = statement2.executeQuery(queryBra);
                if(resultSet2.next()){
                    branchCode = resultSet2.getString("branchcode") + " - ";
                }
                con.close();


                if(prodCount < 1000000){
                    String cselectcount_result = Integer.toString(prodCount);
                    int cselectcount_resultLength = 7 - cselectcount_result.length();
                    while (0 < cselectcount_resultLength){
                        totalZero.append("0");
                        cselectcount_resultLength-= 1;
                    }
                }
                result = template + branchCode + totalZero + prodCount;
            }
        }catch (Exception ex){
            Toast.makeText(this, "returnTransactionNumber() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadItems(String rectrans, String datecreated, final String itemName){
        try{
            LinearLayout layoutItems = findViewById(R.id.linearList);
            layoutItems.removeAllViews();
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String transQuery = "SELECT * FROM funcLoadStockItems('" + datecreated + "','" + itemName + "','All')";
                String recQuery = "SELECT * FROM funcLoadInventoryItems('" + datecreated + "','" + itemName + "','All')";
                Statement statement = con.createStatement();
                ResultSet resultSet = statement.executeQuery((rectrans.equals("rec") ? recQuery : transQuery ));
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