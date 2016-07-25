package com.fourthwardmobile.android.firebaseshoppinglist;

import com.firebase.client.Firebase;
import com.firebase.client.Logger;

/**
 * Includes one-time initialization of Firebase related code
 */
public class ShoppingListApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize Firebase. This could be put in the onCreate of MainActivity
        //but having it here makes sure 100% that it will be initialized
        Firebase.setAndroidContext(this);

        //Set this to see Firebase log messages
        Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
    }

}