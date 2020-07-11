package com.example.atlanticbakery;

import android.util.Base64;

public class security_class {

    public String Encode(String value){
        String result = Base64.encodeToString(value.getBytes(),0);
        return  result;
    }

    public String Decode(String value){
        String result;
        byte[] actualbyte = Base64.decode(value, 0);
        result = new String(actualbyte);
        return  result;
    }
}
