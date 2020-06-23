package com.example.atlanticbakery;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Button btnlogout = findViewById(R.id.btnLogout);
        Button btnScanCode = findViewById(R.id.btnQrCode);
        Button btnShoppingCart = findViewById(R.id.btnShoppingCart);

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnLogout();
            }
        });
        btnScanCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoScanCode();
            }
        });
        btnShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoShoppingCart();
            }
        });
    }

    public  void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loggedOut();
                        finish();
                        gotoLogin();
                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void loggedOut(){
        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username").apply();
        editor.remove("password").apply();
    }

    public  void gotoLogin(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void gotoScanCode(){
        Intent intent = new Intent(this, ScanQRCode.class);
        startActivity(intent);
    }

    public void gotoShoppingCart(){
        Intent intent = new Intent(this, ShoppingCart2.class);
        startActivity(intent);
    }
}