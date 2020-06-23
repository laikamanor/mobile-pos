package com.example.atlanticbakery;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;


public class ShoppingCart_EditProduct extends AppCompatActivity {

    double price = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart__edit_product);
        final Button btnMinus = findViewById(R.id.btnMinus);
        final Button btnPlus = findViewById(R.id.btnPlus);
        final EditText txtQuantity = findViewById(R.id.quantity);
        final EditText txtDiscount = findViewById(R.id.discount);
        final EditText txtTotalPrice = findViewById(R.id.totalPrice);
        final CheckBox free = findViewById(R.id.checkFree);
        txtTotalPrice.setEnabled(false);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                if(txtDiscount.hasFocus()){
                    txtDiscount.clearFocus();
                }

                int intQty = Integer.parseInt(txtQuantity.getText().toString()) + 1;
                txtQuantity.setText(Integer.toString(intQty));

                double priceBefore = Double.parseDouble(txtQuantity.getText().toString()) * price;
                if(Double.parseDouble(txtDiscount.getText().toString()) < 0){

                    txtTotalPrice.setText(Double.toString(priceBefore));
                }else{
                    double discountedTotalPrice = (priceBefore - (Double.parseDouble(txtDiscount.getText().toString()) / 100) * priceBefore);
                    txtTotalPrice.setText(Double.toString(discountedTotalPrice));
                }
            }
        });

        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(txtDiscount.hasFocus()){
                    txtDiscount.clearFocus();
                }

                if(Integer.parseInt(txtQuantity.getText().toString()) > 1){
                    int intQty = Integer.parseInt(txtQuantity.getText().toString()) - 1;
                    txtQuantity.setText(Integer.toString(intQty));

                    double priceBefore = Double.parseDouble(txtQuantity.getText().toString()) * price;
                    if(Double.parseDouble(txtDiscount.getText().toString()) < 0){
                        txtTotalPrice.setText(Double.toString(priceBefore));
                    }else{
                        double discountedTotalPrice = (priceBefore - (Double.parseDouble(txtDiscount.getText().toString()) / 100) * priceBefore);
                        txtTotalPrice.setText(Double.toString(discountedTotalPrice));
                    }
                }
            }
        });

        txtDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    if(Integer.parseInt(txtQuantity.getText().toString()) < 0){
                        txtDiscount.setText("0");
                    }else if(Integer.parseInt(txtQuantity.getText().toString()) > 0){
                        double discountPercent = 0.00,
                                priceBefore = Double.parseDouble(txtQuantity.getText().toString()) * price;
                        if(Double.parseDouble(txtDiscount.getText().toString()) < 25){
                            discountPercent = Double.parseDouble(txtDiscount.getText().toString());
                            double discountedTotalPrice = (priceBefore - (discountPercent / 100) * priceBefore);
                            txtTotalPrice.setText(Double.toString(discountedTotalPrice));

                        }else if(Double.parseDouble(txtDiscount.getText().toString()) >= 25){
                            discountPercent = Double.parseDouble(txtDiscount.getText().toString());
                            double discountedTotalPrice = (priceBefore - (discountPercent / 100) * priceBefore);
                            txtTotalPrice.setText(Double.toString(discountedTotalPrice));
                        }
                    }
                }catch (Exception ex){
                    if(txtDiscount.getText().toString().isEmpty()){
                        double priceBefore = priceBefore = Double.parseDouble(txtQuantity.getText().toString()) * price;
                        txtTotalPrice.setText(Double.toString(priceBefore));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtDiscount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && txtDiscount.getText().toString().isEmpty()){
                    txtDiscount.setText("0");
                }
            }
        });

        txtTotalPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                try {
//                    double priceBefore  = Double.parseDouble(txtQuantity.getText().toString()) * price;
//                    if(Double.parseDouble(txtTotalPrice.getText().toString()) > priceBefore){
//                        double doubleTotalPrice = Double.parseDouble(txtTotalPrice.getText().toString());
//                        double calculatedDiscount = ((priceBefore -  doubleTotalPrice) / priceBefore) * 100;
//                        txtDiscount.setText(Double.toString(calculatedDiscount));
//                        txtTotalPrice.setText(Double.toString(priceBefore));
//                    }else{
//                        double doubleTotalPrice = Double.parseDouble(txtTotalPrice.getText().toString());
//                        double calculatedDiscount = ((priceBefore -  doubleTotalPrice) / priceBefore) * 100;
//                        txtDiscount.setText(Double.toString(calculatedDiscount));
//                    }
//                }catch (Exception ex){
//
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        free.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    txtQuantity.setText("0");
                    txtDiscount.setText("0");
                    txtTotalPrice.setText("0.00");
                    btnMinus.setEnabled(false);
                    btnPlus.setEnabled(false);
                    txtDiscount.setEnabled(false);
                }else{
                    txtQuantity.setText("1");
                    double priceBefore = Double.parseDouble(txtQuantity.getText().toString()) * price;
                    txtTotalPrice.setText(Double.toString(priceBefore));
                    btnMinus.setEnabled(true);
                    btnPlus.setEnabled(true);
                    txtDiscount.setEnabled(true);
                }
            }
        });

    }
}