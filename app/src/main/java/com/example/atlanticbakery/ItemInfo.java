package com.example.atlanticbakery;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class ItemInfo extends AppCompatActivity {
    TextView txtItemName;
    EditText txtQuantity;
    Button btnMinus, btnPlus, btnAddCart;
    String itemName;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    item_class itemc = new item_class();
    ui_class uic = new ui_class();
    DatabaseHelper myDb;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);

        myDb = new DatabaseHelper(this);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Add Quantity</font>"));

        txtQuantity = findViewById(R.id.txtQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddCart = findViewById(R.id.btnAddCart);

        itemName = getIntent().getStringExtra("itemname");

        txtItemName = findViewById(R.id.itemName);
        txtItemName.setText(itemName);


        txtQuantity.setText("0");
        txtQuantity.setBackgroundResource(R.drawable.custom_edittext);

        NavigationView navigationView = findViewById(R.id.nav);
        drawerLayout = findViewById(R.id.navDrawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                boolean result = false;
                switch (menuItem.getItemId()){
                    case R.id.nav_scanItem :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(ItemInfo.this, QRCode.class));
                        finish();
                        break;
                    case R.id.nav_exploreItems :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(ItemInfo.this, AvailableItems.class));
                        finish();
                        break;
                    case R.id.nav_shoppingCart :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(ItemInfo.this, ShoppingCart.class));
                        finish();
                        break;
                    case R.id.nav_receivedProduction :
                        result = true;
                        intent = new Intent(getBaseContext(), Received.class);
//        intent.putExtra("type", "Received from Production");
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


        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtQuantity.setText(minusPlus("-"));
                txtQuantity.setSelection(txtQuantity.getText().length());
            }
        });
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtQuantity.setText(minusPlus("+"));
                txtQuantity.setSelection(txtQuantity.getText().length());
            }
        });

        btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isItemNameExist = itemc.checkItemName(ItemInfo.this, itemName);
                boolean hasStock = itemc.checkItemNameStock(ItemInfo.this, itemName);
                int quantity;
                if(txtQuantity.getText().toString().isEmpty()){
                    quantity = 0;
                    txtQuantity.setText("0");
                }else{
                    quantity = Integer.parseInt(txtQuantity.getText().toString());
                }
                if (!isItemNameExist) {
                    Toast.makeText(ItemInfo.this, "item not found", Toast.LENGTH_SHORT).show();
                }else if(quantity <=0){
                    Toast.makeText(ItemInfo.this, "Add Quantity atleast 1", Toast.LENGTH_SHORT).show();
                }else if (hasStock) {
                    saveData();
                }else if(!hasStock) {
                    final AlertDialog.Builder myDialog = new AlertDialog.Builder(ItemInfo.this);
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
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        loadShoppingCartCount();
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String minusPlus(String operator){
        int quantity;
        if(txtQuantity.getText().toString().isEmpty()){
            quantity = 0;
        }else{
            quantity = Integer.parseInt(txtQuantity.getText().toString());
        }
        if(operator.equals("+")){
            quantity += 1;
        }else {
            if(quantity > 0){
                quantity -= 1;
            }
        }
        return Integer.toString(quantity);
    }

    @SuppressLint("SetTextI18n")
    public void saveData() {
        checkItem checkItem = new checkItem();
        checkItem.execute("");
    }

    @SuppressLint("StaticFieldLeak")
    public class checkItem extends AsyncTask<String, String, String> {
        String z = "";

        final LoadingDialog loadingDialog = new LoadingDialog(ItemInfo.this);

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            double price = itemc.returnItemNamePrice(ItemInfo.this, itemName);
            double quantity = Double.parseDouble(txtQuantity.getText().toString());
            boolean isInserted = myDb.insertData(quantity, 0.00, price, 0, price, itemName);
            if (isInserted) {
                z = "Item Added";
            } else {
                z = "Item Not Added";
            }
            return z;
        }

        @Override
        protected void onPostExecute(final String s) {
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ItemInfo.this, s, Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                }
            };
            handler.postDelayed(r, 1000);
        }
    }

    public void loadShoppingCartCount(){
        NavigationView navigationView = findViewById(R.id.nav);
        Menu menu = navigationView.getMenu();
        MenuItem nav_shoppingCart = menu.findItem(R.id.nav_shoppingCart);
        int totalItems = myDb.countItems();
        nav_shoppingCart.setTitle("Shopping Cart (" + totalItems + ")");
    }
}