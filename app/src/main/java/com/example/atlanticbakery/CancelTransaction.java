package com.example.atlanticbakery;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public class CancelTransaction extends AppCompatActivity {
    long mLastClickTime = 0;
    inventory_class ic = new inventory_class();
    connection_class cc = new connection_class();
    Connection con;
    AutoCompleteTextView txtSearch;
    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_transaction);


        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Cancel Rec/Trans</font>"));
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        txtSearch = findViewById(R.id.txtSearch);
        txtSearch.setAdapter(ic.fillAdapter(this, ic.returnTransactionNumbers(this,"")));

        loadTransactions("");
        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                loadTransactions(txtSearch.getText().toString());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadTransactions(final String transactionNumber){
        final LinearLayout linearTransactions = findViewById(R.id.linearTransactions);
        final LinearLayout layoutItems = findViewById(R.id.layoutItems);
        linearTransactions.removeAllViews();
        layoutItems.removeAllViews();
        List<String> result;
        result = ic.returnTransactionNumbers(this,transactionNumber);
        int totalTransactions = 0;
        for (String temp : result) {
            totalTransactions += 1;
            final LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(layoutParams);
            layout.setOrientation(LinearLayout.VERTICAL);
            linearTransactions.addView(layout);

            TextView txtTransaction = new TextView(this);
            LinearLayout.LayoutParams txtTransactionParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            txtTransaction.setLayoutParams(txtTransactionParams);
            txtTransactionParams.setMargins(40,10,10,10);
            txtTransaction.setText(temp);
            txtTransaction.setTextSize(15);
            txtTransaction.setTextColor(Color.BLACK);
            final int generatedTxtTransactionID = View.generateViewId();
            txtTransaction.setId(generatedTxtTransactionID);
            txtTransaction.setClickable(true);

            final String currentTransactionNumber = temp;
            txtTransaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    loadItems(currentTransactionNumber);
                }
            });

            layout.addView(txtTransaction);

            Button btnCancel = new Button(this);
            LinearLayout.LayoutParams btnCancelParams = new LinearLayout.LayoutParams(200,80);
            btnCancelParams.setMargins(40,0,10,10);
            btnCancel.setBackgroundColor(Color.RED);
            btnCancel.setTextColor(Color.WHITE);
            btnCancel.setText("Cancel");
            final int generatedCancelID = View.generateViewId();
            btnCancel.setId(generatedCancelID);
            btnCancel.setLayoutParams(btnCancelParams);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    String errorMsg = "You can't cancel this transaction because the item(s) below of ending balance is less than the Received quantity: \n \n";
                    errorMsg += ic.checkStock(CancelTransaction.this, currentTransactionNumber);
                    if(!errorMsg.equals("You can't cancel this transaction because the item(s) below of ending balance is less than the Received quantity: \n \n")){

                        AlertDialog.Builder myDialogError = new AlertDialog.Builder(CancelTransaction.this);
                        myDialogError.setMessage(errorMsg);
                        myDialogError.setCancelable(false);
                        myDialogError.setTitle("Atlantic Bakery");
                        myDialogError.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        myDialogError.show();

                    }else{
                        AlertDialog.Builder myDialog = new AlertDialog.Builder(CancelTransaction.this);
                        myDialog.setMessage("Are you sure you want to cancel this transaction?");
                        myDialog.setCancelable(false);

                        myDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ic.cancelRecTrans(CancelTransaction.this, currentTransactionNumber);
                                layoutItems.removeAllViews();
                                linearTransactions.removeAllViews();
                                TextView txtHeader = findViewById(R.id.txtHeader);
                                TextView txtHeaderItems = findViewById(R.id.txtHeaderItems);
                                txtHeader.setText("TRANSACTIONS (0)");
                                txtHeaderItems.setText("ITEMS (0)");
                            }
                        });

                        myDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        myDialog.show();
                    }

                }
            });

            layout.addView(btnCancel);

            View line = new View(this);
            LinearLayout.LayoutParams lineLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            line.setLayoutParams(lineLayout);
            line.setBackgroundColor(Color.BLACK);
            layout.addView(line);
        }
        TextView txtHeader = findViewById(R.id.txtHeader);
        txtHeader.setText("TRANSACTIONS (" + totalTransactions + ")");
    }

    public void loadItems(String transactionNumber){
        try{
            LinearLayout layoutItems = findViewById(R.id.layoutItems);
            layoutItems.removeAllViews();
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query = "SELECT item_name,quantity FROM tblproduction WHERE transaction_number='" + transactionNumber + "';";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(query);
                int totalItems = 0;
                while (rs2.next()){
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
                    txtItemname.setText(rs2.getString("item_name"));
                    txtItemname.setLayoutParams(txtItemNameParams);
                    layout.addView(txtItemname);

                    TextView txtQuantity = new TextView(this);
                    txtQuantity.setTextSize(15);
                    txtQuantity.setText(rs2.getString("quantity") + " pcs.");
                    txtQuantity.setLayoutParams(txtItemNameParams);
                    layout.addView(txtQuantity);

                    View line = new View(this);
                    LinearLayout.LayoutParams lineLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                    line.setLayoutParams(lineLayout);
                    line.setBackgroundColor(Color.BLACK);
                    layout.addView(line);

                }
                con.close();
                TextView txtHeaderItems = findViewById(R.id.txtHeaderItems);
                txtHeaderItems.setText("ITEMS (" + totalItems + ")");
            }

        }catch (Exception ex){
            Toast.makeText(this,"loadItems() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}