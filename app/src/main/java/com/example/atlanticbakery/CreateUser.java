package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class CreateUser extends AppCompatActivity {
    Connection con;
    long mLastClickTime = 0;
    EditText txtName;
    EditText txtUsername;
    EditText txtPassword;
    EditText txtConfirmPassword;
    Spinner cmbWorkgroup;
    Spinner cmbBranch;
    Spinner cmbPOSType;
    @SuppressLint({"WrongConstant", "RestrictedApi"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Create User</font>"));
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        txtName = findViewById(R.id.txtName);
        txtUsername  = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        cmbWorkgroup = findViewById(R.id.cmbWorkgroup);
        cmbBranch = findViewById(R.id.cmbBranchOutlet);
        cmbPOSType = findViewById(R.id.cmbPOSType);

        String branchItems = loadBranchOutlet();
        fillCombos(branchItems, R.id.cmbBranchOutlet);

        String workgroupItems = "Select Workgroup,Sales,Cashier,Manager,Administrator,LC Accounting";
        fillCombos(workgroupItems,R.id.cmbWorkgroup);

        String POSTypeItems = "Select POS Type,Retail,Wholesale,Coffee Shop";
        fillCombos(POSTypeItems,R.id.cmbPOSType);

        Button btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                async async = new async();
                async.execute("");
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    public class async extends AsyncTask<String, String, String> {
        String z;
        Boolean isSuccess;
        LoadingDialog loadingDialog = new LoadingDialog(CreateUser.this);
        @SuppressLint("WrongConstant")
        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... strings) {
            if(txtName.getText().toString().equals("")){
                z = "Name field is required";
            }else if(txtUsername.getText().toString().equals("")){
                z = "Username field is required";
            }else if(txtPassword.getText().toString().equals("")){
                z = "Password field is required";
            }else if(txtConfirmPassword.getText().toString().equals("")){
                z = "Confirm Password field is required";
            }else if(!txtPassword.getText().toString().equals(txtConfirmPassword.getText().toString())){
                z = "Password did not match";
            }else if(cmbWorkgroup.getSelectedItemPosition() == 0){
                z = "Please Select Workgroup";
            }else if(cmbBranch.getSelectedItemPosition() == 0){
                z = "Please Select Branch/Outlet";
            }else if(cmbPOSType.getSelectedItemPosition() == 0){
                z = "Please Select POS Type";
            }
            else{
                String name = txtName.getText().toString();
                String username = txtUsername.getText().toString();
                String password = txtConfirmPassword.getText().toString();
                String workgroup = cmbWorkgroup.getSelectedItem().toString();
                String branch = cmbBranch.getSelectedItem().toString();
                String postype = cmbPOSType.getSelectedItem().toString();
                z = insertTransaction(name, username, Base64.encodeToString(password.getBytes(),0),workgroup,branch,postype);
                isSuccess = true;
            }
            return z;
        }

        @SuppressLint({"WrongConstant", "ShowToast"})
        @Override
        protected void onPostExecute(final String s) {
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CreateUser.this, s, Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                }
            };
            handler.postDelayed(r, 1000);
        }
    }

//    public void toastMsg(String msg, Integer duration){
//        Toast.makeText(this, msg, duration).show();
//    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String insertTransaction(String name, String username, String password, String workgroup, String branch, String postype)  {
        String result;
        try{
            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
            int userid = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("userid", "")));
            con = connectionClass();
            if (con == null) {
                result = "Check Your Internet Access";
            } else {
                String query = "INSERT INTO tblusers (fullname,username,password,workgroup,datecreated," +
                        "createdby,status,brid,postype) VALUES(" +
                        "'" + name + "', '" + username + "','" + password + "','"+ workgroup + "'," +
                        "(SELECT GETDATE()),(SELECT username FROM tblusers WHERE systemid=" + userid + "),1," +
                        "(SELECT brid FROM tblbranch WHERE branchcode='" + branch + "'),'" + postype + "');";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                result = "User Created";
                txtName.getText().clear();
                txtUsername.getText().clear();
                txtPassword.getText().clear();
                txtConfirmPassword.getText().clear();
            }
        }catch(SQLException ex){
            result = ex.getMessage();
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public  String loadBranchOutlet(){
        StringBuilder result = new StringBuilder();
        try{
            con = connectionClass();
            if(con == null){
                Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                result.append("Select Branch/Outlet,");
                String query = "SELECT branchcode FROM vLoadBranches WHERE status=1 ORDER BY branchcode";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()){
                    result.append(rs.getString("branchcode"));
                    result.append(",");
                }
                con.close();
            }
        }catch (Exception ignored){

        }
        return result.toString();
    }

    public  void fillCombos(String items,int id){
        final List<String>  list = new ArrayList<>();
        Spinner spinner = findViewById(id);

        StringTokenizer tokenizer = new StringTokenizer(items,",");
        while (tokenizer.hasMoreTokens()){
            list.add(tokenizer.nextToken());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Connection connectionClass() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            SharedPreferences sharedPreferences = getSharedPreferences("CONFIG",MODE_PRIVATE);
            String IPAddress = Objects.requireNonNull(sharedPreferences.getString("IPAddress", ""));
            String Username = Objects.requireNonNull(sharedPreferences.getString("Username", ""));
            String Password = Objects.requireNonNull(sharedPreferences.getString("Password", ""));
            String Database = Objects.requireNonNull(sharedPreferences.getString("Database", ""));
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
}