package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class ScanQRCode extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static TextView resultText;
    ProgressBar progressBar;

    DatabaseHelper myDb = new DatabaseHelper(this);

    long mLastClickTime = 0;

//   Classes
    item_class itemc = new item_class();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Scan QR Code</font>"));
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_scan_q_r_code);
        resultText =  findViewById(R.id.lblResult);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnCart = findViewById(R.id.btnAddCart);
        progressBar = findViewById(R.id.progWait);
        progressBar.setVisibility(View.GONE);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                startActivity(new Intent(getApplicationContext(), ScanCode.class));
            }
        });
        btnCart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String itemName = resultText.getText().toString();
                boolean hasStock = itemc.checkItemNameStock(ScanQRCode.this, itemName);
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (itemName.equals("Result: N/A")) {
                    Toast.makeText(ScanQRCode.this, "Scan Item first", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isItemNameExist = itemc.checkItemName(ScanQRCode.this, itemName);
                    if (!isItemNameExist) {
                        Toast.makeText(ScanQRCode.this, "item not found", Toast.LENGTH_SHORT).show();
                    } else if (hasStock) {
                        saveData();
                    } else {
                        final AlertDialog.Builder myDialog = new AlertDialog.Builder(ScanQRCode.this);
                        myDialog.setTitle("Atlantic Bakery");
                        myDialog.setMessage("This item is out of stock! Are you sure you want to add to cart?");
                        myDialog.setCancelable(false);
                        myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveData();
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
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void saveData() {
        String itemName = resultText.getText().toString();
        double price = itemc.returnItemNamePrice(ScanQRCode.this, itemName);
        boolean isInserted = myDb.insertData(1, 0.00, price, 0, price, itemName);
        if (isInserted) {
            Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Item Not Added", Toast.LENGTH_SHORT).show();
        }
        resultText.setText("Result: N/A");
    }
}