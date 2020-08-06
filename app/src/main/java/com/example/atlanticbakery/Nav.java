package com.example.atlanticbakery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class Nav extends AppCompatActivity  {
    ui_class uic = new ui_class();
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

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                boolean result = false;
                switch (menuItem.getItemId()){
                    case R.id.nav_scanItem :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(Nav.this, QRCode.class));
                        finish();
                        break;
                    case R.id.nav_exploreItems :
                        Toast.makeText(Nav.this, "Event is clicked", Toast.LENGTH_SHORT).show();
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        break;
                    case R.id.nav_shoppingCart :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(Nav.this, ShoppingCart.class));
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