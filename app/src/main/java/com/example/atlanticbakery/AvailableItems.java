package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class AvailableItems extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    ui_class uic = new ui_class();
    connection_class cc = new connection_class();
    Connection con;
    DecimalFormat df = new DecimalFormat("#,###");

    AutoCompleteTextView txtSearch;
    SwipeRefreshLayout swipeRefreshLayout;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_items);

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Available Items</font>"));
        NavigationView navigationView = findViewById(R.id.nav);
        drawerLayout = findViewById(R.id.navDrawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Button btnSearch = findViewById(R.id.btnSearch);
        txtSearch = findViewById(R.id.txtSearch);
        txtSearch.setAdapter(fillItemNames(returnItemNames()));

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadItems(txtSearch.getText().toString());
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadItems(txtSearch.getText().toString());
            }
        });

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
                        startActivity(uic.goTo(AvailableItems.this, QRCode.class));
                        finish();
                        break;
                    case R.id.nav_shoppingCart :
                        result = true;
                        drawerLayout.closeDrawer(Gravity.START, false);
                        startActivity(uic.goTo(AvailableItems.this, ShoppingCart.class));
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
        loadItems("");
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayAdapter<String> fillItemNames(List<String> names){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, names);
    }

    public List<String> returnItemNames(){
        final List<String>  result = new ArrayList<>();
        try {
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            } else {
                String query2 = "SELECT a.itemname FROM tblinvitems a INNER JOIN tblitems b ON a.itemname = b.itemname INNER JOIN tblcat c ON b.category = c.category WHERE invnum=(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC) AND b.discontinued=0  AND c.status=1 ORDER BY a.endbal DESC,a.itemname ASC";
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

    public boolean checkEndbal(String itemName){
        boolean result = false;
        try {
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
            }else {
                String query = "SELECT a.itemname, SUM(a.endbal) FROM tblinvitems a INNER JOIN tblitems b ON a.itemname = b.itemname INNER JOIN tblcat c ON b.category = c.category WHERE invnum=(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC) AND b.discontinued=0  AND c.status=1 AND a.itemname='" + itemName + "'  GROUP BY a.endbal,a.itemname HAVING SUM(a.endbal) > 0 ORDER BY a.endbal DESC,a.itemname ASC;";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                result = (rs.next());
            }
        }catch (Exception ex){
            Toast.makeText(this,  ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  result;
    }

    @SuppressLint("SetTextI18n")
    public void loadItems(String value) {
        GridLayout gridLayout = findViewById(R.id.grid);
        gridLayout.removeAllViews();

        try {
            con = cc.connectionClass(this);
            if (con == null) {
                Toast.makeText(this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();

            } else {
                String query = "SELECT a.itemname,a.endbal,b.price,b.itemid FROM tblinvitems a INNER JOIN tblitems b ON a.itemname = b.itemname INNER JOIN tblcat c ON b.category = c.category WHERE invnum=(SELECT TOP 1 invnum FROM tblinvsum ORDER BY invsumid DESC) AND b.discontinued=0  AND c.status=1 AND a.itemname LIKE '%" + value + "%' ORDER BY a.endbal DESC,a.itemname ASC";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()){

                    final String itemName = rs.getString("itemname");
                    double endBal = rs.getDouble("endbal");
                    String price = "₱ " + df.format(rs.getDouble("price"));
                    int itemId = rs.getInt("itemid");

                    CardView cardView = new CardView(this);
                    LinearLayout.LayoutParams layoutParamsCv = new LinearLayout.LayoutParams(300, 300);
                    layoutParamsCv.setMargins(20, 10, 10, 10);
                    cardView.setLayoutParams(layoutParamsCv);
                    cardView.setRadius(12);
                    cardView.setCardElevation(5);
                    gridLayout.addView(cardView);
                    LinearLayout linearLayout = new LinearLayout(this);
                    LinearLayout.LayoutParams layoutParamsLinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 5f);
                    linearLayout.setLayoutParams(layoutParamsLinear);
                    linearLayout.setTag(itemId);

                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    cardView.addView(linearLayout);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(20, 50, 20, 0);
                    LinearLayout.LayoutParams layoutParamsItemLeft = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParamsItemLeft.setMargins(20, -50, 10, 10);

                    TextView txtItemName = new TextView(this);
                    txtItemName.setText(itemName + "\n" + price);
                    txtItemName.setLayoutParams(layoutParams);
                    txtItemName.setTextSize(20);
                    linearLayout.addView(txtItemName);

                    TextView txtItemLeft = new TextView(this);
                    txtItemLeft.setLayoutParams(layoutParamsItemLeft);
                    txtItemLeft.setTextSize(15);
                    txtItemLeft.setTextColor(Color.RED);
                    if(endBal == 0.0){
                        txtItemLeft.setText("Not Available");
                        linearLayout.addView(txtItemLeft);
                    }else if(endBal <= 10){
                        txtItemLeft.setText(df.format(endBal) + " Item Left!");
                        linearLayout.addView(txtItemLeft);
                    }else{
                        txtItemLeft.setText("In Stock");
                        txtItemLeft.setTextColor(Color.rgb(30,203,6));
                        linearLayout.addView(txtItemLeft);
                    }

                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!checkEndbal(itemName)){
                                Toast.makeText(AvailableItems.this, "'" + itemName + "' is not available", Toast.LENGTH_SHORT).show();

                            }else{
                                Intent intent;
                                intent = new Intent(getBaseContext(), ItemInfo.class);
                                intent.putExtra("itemname", itemName);
                                startActivity(intent);
                            }
                        }
                    });

                }
                con.close();
            }
        }catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
