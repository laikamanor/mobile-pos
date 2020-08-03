package com.example.atlanticbakery;
import android.content.Intent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import java.util.Objects;

public class SplashScreen extends AppCompatActivity {
    DatabaseHelper myDb = new DatabaseHelper(this);
    prefs_class pc = new prefs_class();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                goTO();
            }
        };
        handler.postDelayed(r, 3000);
    }

    public void goTO(){
        myDb.truncateTable();
        pc.loggedOut(SplashScreen.this);
        Intent intent = new Intent(this, IPAddress.class);
        startActivity(intent);
        finish();
    }

}