package com.example.atlanticbakery;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {
    DatabaseHelper myDb = new DatabaseHelper(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}