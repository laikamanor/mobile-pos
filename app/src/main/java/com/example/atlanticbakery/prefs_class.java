package com.example.atlanticbakery;

import android.app.Activity;
import android.content.SharedPreferences;
import static android.content.Context.MODE_PRIVATE;
public class prefs_class {
    public void loggedOut(Activity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("LOGIN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username").apply();
        editor.remove("userid").apply();
        editor.remove("password").apply();
    }
}
