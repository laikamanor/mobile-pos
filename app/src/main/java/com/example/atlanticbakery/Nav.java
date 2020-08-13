package com.example.atlanticbakery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class Nav extends AppCompatActivity  {
    ui_class uic = new ui_class();
    DatabaseHelper db = new DatabaseHelper(this);

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        NavigationView navigationView = findViewById(R.id.nav);
        drawerLayout = findViewById(R.id.navDrawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Main Menu</font>"));


        Menu menu = navigationView.getMenu();
        MenuItem nav_shoppingCart = menu.findItem(R.id.nav_shoppingCart);
        int totalItems = db.countItems();
        nav_shoppingCart.setTitle("Shopping Cart (" + totalItems + ")");

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
                        startActivity(uic.goTo(Nav.this, QRCode.class));
                        finish();
                        break;
                    case R.id.nav_exploreItems :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(Nav.this, AvailableItems.class));
                        finish();
                        break;
                    case R.id.nav_shoppingCart :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(Nav.this, ShoppingCart.class));
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
                    case R.id.nav_cancelRecTrans :
                        result = true;
                        intent = new Intent(getBaseContext(), CancelTransaction.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                return result;
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
}