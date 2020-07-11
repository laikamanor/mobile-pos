package com.example.atlanticbakery;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import java.util.Objects;

public class MainMenu extends AppCompatActivity {
    long mLastClickTime = 0;
    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    user_class uc = new user_class();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Main Menu</font>"));

        Button btnLogOut = findViewById(R.id.btnLogout);
        Button btnScanCode = findViewById(R.id.btnQrCode);
        Button btnShoppingCart = findViewById(R.id.btnShoppingCart);
        Button btnAdmin = findViewById(R.id.admin);
        checkWorkgroup();

        btnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                gotoCreateUser();
            }
        });
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onBtnLogout();
            }
        });
        btnScanCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                gotoScanCode();
            }
        });
        btnShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                gotoShoppingCart();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkWorkgroup(){
           Button btnAdmin = findViewById(R.id.admin);
           String workgroup = uc.returnWorkgroup(MainMenu.this);
           if (!workgroup.equals("Administrator")) {
               btnAdmin.setVisibility(View.GONE);
           }else{
               btnAdmin.setVisibility(View.VISIBLE);
           }
    }
    public  void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pc.loggedOut(MainMenu.this);
                        finish();
                        gotoLogin();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public  void gotoLogin(){
        startActivity(uic.goTo(this, MainActivity.class));
    }
    public void gotoScanCode(){
        startActivity(uic.goTo(this, ScanQRCode.class));
    }

    public void gotoShoppingCart(){
        startActivity(uic.goTo(this, ShoppingCart.class));
    }

    public void gotoCreateUser(){
        startActivity(uic.goTo(this, CreateUser.class));
    }
}