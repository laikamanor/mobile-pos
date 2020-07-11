package com.example.atlanticbakery;

import android.app.Activity;
import android.content.Intent;

public class ui_class {

    public Intent goTo(Activity fromActivity, Class toActivity){
        Intent intent = new Intent(fromActivity, toActivity);
        return  intent;
    }

}
