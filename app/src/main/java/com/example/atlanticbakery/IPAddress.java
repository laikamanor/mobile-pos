package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class IPAddress extends AppCompatActivity {

     EditText txtIPAddress;
     EditText txtUsername;
     EditText txtPassword;
     EditText txtDatabase;
     ProgressBar progressBar;
     Connection con;

    long mLastClickTime = 0;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_p_address);

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>" + getString(R.string.app_name) + "</font>"));

        txtIPAddress = findViewById(R.id.txtIPAddress);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        txtDatabase = findViewById(R.id.txtDatabase);
        progressBar = findViewById(R.id.progressBar2);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        progressBar.setVisibility(View.GONE);

        checkCurrentLogin();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                IPAddress.checkConnection checkConnection = new IPAddress.checkConnection();
                checkConnection.execute("");
            }
        });

    }

    public void checkCurrentLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("CONFIG",MODE_PRIVATE);
        String IPaddress = sharedPreferences.getString("IPAddress","");
        String username =sharedPreferences.getString("Username","");
        String password = sharedPreferences.getString("Password","");
        String database =sharedPreferences.getString("Database","");
        assert password != null;
        assert username != null;
        assert database != null;
        assert IPaddress != null;
        if(!username.isEmpty() && !password.isEmpty() && !IPaddress.isEmpty() && !database.isEmpty()){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void gotoLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("CONFIG",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("IPAddress",txtIPAddress.getText().toString().trim()).apply();
        editor.putString("Username",txtUsername.getText().toString().trim()).apply();
        editor.putString("Password",txtPassword.getText().toString().trim()).apply();
        editor.putString("Database",txtDatabase.getText().toString().trim()).apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    public class checkConnection extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            con = connectionClass();
            if(txtIPAddress.getText().toString().equals("")){
                z = "IP Address field is required";
            }else if(txtUsername.getText().toString().equals("")){
                z = "Username field is required";
            }else if(txtPassword.getText().toString().equals("")){
                z = "Password field is required";
            }else if(txtDatabase.getText().toString().equals("")){
                z = "Database field is required";
            }else if(con == null){
                z = "Check Your Internet Access";
            }else{
                gotoLogin();
                z = "Connection Success";
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(IPAddress.this, s, Toast.LENGTH_LONG).show();
            if (isSuccess) {
                gotoLogin();
            }
        }
    }

    @SuppressLint("NewAPI")
    public Connection connectionClass()  {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;
        try {
            String IPAddress = txtIPAddress.getText().toString();
            String Username = txtUsername.getText().toString();
            String Password = txtPassword.getText().toString();
            String Database = txtDatabase.getText().toString();
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + IPAddress + "/" + Database + ";user=" + Username + ";password=" + Password + ";";
//            ConnectionURL = "jdbc:jtds:sqlserver://192.168.42.1/AKPOS;user=admin;password=admin;";
            connection = DriverManager.getConnection(ConnectionURL);

        } catch (SQLException se) {
            Log.e("error here 1: ", Objects.requireNonNull(se.getMessage()));
        } catch (ClassNotFoundException e) {
            Log.e("error here 2: ", e.toString());
        } catch (Exception e) {
            Log.e("error here 3: ", Objects.requireNonNull(e.getMessage()));
        }
        return connection;
    }


//    public void toastMsg(String value, Integer duration){
//        Toast.makeText(this, value, duration).show();
//    }
}