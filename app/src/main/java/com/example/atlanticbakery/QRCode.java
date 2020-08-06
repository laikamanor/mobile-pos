package com.example.atlanticbakery;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class QRCode extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static TextView resultText;
    ProgressBar progressBar;

    DatabaseHelper myDb = new DatabaseHelper(this);

    long mLastClickTime = 0;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    //   Classes
    item_class itemc = new item_class();
    ui_class uic = new ui_class();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_q_r_code);

        NavigationView navigationView = findViewById(R.id.nav);
        drawerLayout = findViewById(R.id.navDrawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Scan QR Code</font>"));

        resultText =  findViewById(R.id.lblResult);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnCart = findViewById(R.id.btnAddCart);
        progressBar = findViewById(R.id.progWait);
        progressBar.setVisibility(View.GONE);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                boolean result = false;
                switch (menuItem.getItemId()){
                    case R.id.nav_scanItem :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        break;
                    case R.id.nav_exploreItems :
                        Toast.makeText(QRCode.this, "Event is clicked", Toast.LENGTH_SHORT).show();
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        break;
                    case R.id.nav_shoppingCart :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(QRCode.this, ShoppingCart.class));
                        break;
                }
                return result;
            }
        });

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
                boolean hasStock = itemc.checkItemNameStock(QRCode.this, itemName);
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (itemName.equals("Result: N/A")) {
                    Toast.makeText(QRCode.this, "Scan Item first", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isItemNameExist = itemc.checkItemName(QRCode.this, itemName);
                    if (!isItemNameExist) {
                        Toast.makeText(QRCode.this, "item not found", Toast.LENGTH_SHORT).show();
                    } else if (hasStock) {
                        saveData();
                    }else if(!hasStock) {
                        final AlertDialog.Builder myDialog = new AlertDialog.Builder(QRCode.this);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    public void saveData() {
        checkItem checkItem = new checkItem();
        checkItem.execute("");
    }

    @SuppressLint("StaticFieldLeak")
    public class checkItem extends AsyncTask<String, String, String> {
        String z = "";

        final LoadingDialog loadingDialog = new LoadingDialog(QRCode.this);

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            String itemName = resultText.getText().toString();
            double price = itemc.returnItemNamePrice(QRCode.this, itemName);
            boolean isInserted = myDb.insertData(1, 0.00, price, 0, price, itemName);
            if (isInserted) {
                z = "Item Added";
            } else {
                z = "Item Not Added";
            }
            resultText.setText("Result: N/A");
            return z;
        }

        @Override
        protected void onPostExecute(final String s) {
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(QRCode.this, s, Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                }
            };
            handler.postDelayed(r, 1000);
        }
    }
}