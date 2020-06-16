package com.example.atlanticbakery;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import  android.view.View;
import android.widget.Button;
import  android.widget.EditText;
import  android.widget.ProgressBar;
import  android.widget.Toast;
import  java.sql.Connection;
import  java.sql.DriverManager;
import  java.sql.ResultSet;
import  java.sql.SQLException;
import  java.sql.Statement;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    //Declaring layout button,editTexts and progress bar
    Button login;
    EditText username, password;
    ProgressBar progressBar;
    //End Declaring layout button,editTexts and progress bar

    //Declaring connection variables
    Connection con;
    //End Declaring connection variables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //Getting values from button,editTexts and progress bar
        login = findViewById(R.id.button);
        username = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        progressBar = findViewById(R.id.progressBar);
        //End Getting values from button,editTexts and progress bar

        progressBar.setVisibility(View.GONE);

        checkCurrentLogin();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckLogin checkLogin = new CheckLogin();
                checkLogin.execute("");

            }
        });

    }

    public void checkCurrentLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
        String username = sharedPreferences.getString("username","");
        String password =sharedPreferences.getString("password","");
        assert password != null;
        assert username != null;
        if(!username.isEmpty() && !password.isEmpty()){
            openMainMenu();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class CheckLogin extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String us = username.getText().toString();
            String ps = password.getText().toString();

            if (us.trim().equals("") || ps.trim().equals("")) {
                z = "Please enter Username and Password";
            } else {
                try {
                    con = connectionClass();
                    if (con == null) {
                        z = "Check Your Internet Access";

                    } else {
//                        String encoded = Base64.encodeToString("Hello".getBytes());
//                        println(encoded);   // Outputs "SGVsbG8="
//
//                        String decoded = new String(Base64.decode(encoded));
//                        println(decoded) ;   // Outputs "Hello"

                        String query = "select systemid from tblusers WHERE username='" + us + "'AND password='" + ps + "'";
                        Statement stmt = con.createStatement();

                        ResultSet rs = stmt.executeQuery(query);
                        if (rs.next()) {
                            z = "Login Success";
                            isSuccess = true;
                            con.close();
                        } else {
                            z = "Invalid Credentials";
                            isSuccess = false;
                        }
                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    z = ex.toString();
                }
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();

            if (isSuccess) {
                saveLoggedIn();
                openMainMenu();
            }
        }
    }

    public  void saveLoggedIn(){
        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username",username.getText().toString()).apply();
        editor.putString("password",password.getText().toString()).apply();
    }

    public  void openMainMenu(){
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("NewAPI")
    public Connection connectionClass() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://192.168.137.1/AKPOS;user=admin;password=admin;";
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
}