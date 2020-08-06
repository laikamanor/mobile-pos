package com.example.atlanticbakery;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.Base64;
import  android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import  android.widget.EditText;
import  android.widget.Toast;

import java.util.List;
import  java.util.Objects;

public class MainActivity extends AppCompatActivity {

    int userid;
    //Declaring layout button,editTexts and progress bar
    Button login;
    EditText username, password;
    //End Declaring layout button,editTexts and progress bar

    long mLastClickTime = 0;
    user_class uc = new user_class();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>" + getString(R.string.app_name) + "</font>"));

        //Getting values from button,editTexts and progress bar
        login = findViewById(R.id.button);
        username = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                CheckLogin checkLogin = new CheckLogin();
                checkLogin.execute("");
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class CheckLogin extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            String us = username.getText().toString();
            String ps = password.getText().toString();
            Integer resultUserID = uc.checkUsernamePassword(MainActivity.this,"username", us, ps);
            if (us.trim().equals("") || ps.trim().equals("")) {
                z = "Please enter Username and Password";
            }else if(resultUserID > 0){
                isSuccess = true;
                z = "Login Success";
                userid = resultUserID;
            }else{
                z = "Invalid Credentials";
            }
            return z;

        }

        @Override
        protected void onPostExecute(final String s) {
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (isSuccess) {
                        saveLoggedIn();
                        openMainMenu();
                    }
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                }
            };
            handler.postDelayed(r, 1000);
        }
    }

    public  void saveLoggedIn(){
        String susername = username.getText().toString();
        String spassword = Base64.encodeToString(password.getText().toString() .getBytes(),0);
        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username",susername).apply();
        editor.putString("password",spassword).apply();
        editor.putString("userid",Integer.toString(userid)).apply();
        uc.insetLoginLogs(MainActivity.this, susername);
        uc.checkCutOff(MainActivity.this, susername);
    }

    public  void openMainMenu(){
        Intent intent = new Intent(this, Nav.class);
        startActivity(intent);
        finish();
    }
}