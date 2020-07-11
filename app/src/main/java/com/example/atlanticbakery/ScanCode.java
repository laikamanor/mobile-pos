package com.example.atlanticbakery;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import com.google.zxing.Result;

import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanCode extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView ScannerView;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScannerView = new ZXingScannerView(this);
        setContentView(ScannerView);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void handleResult(Result result) {
        ScanQRCode.resultText.setText(result.getText());
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
    }
}