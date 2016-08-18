package com.fourthwardmobile.android.firebaseshoppinglist.utils;

import android.content.Context;

import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;

import java.text.SimpleDateFormat;

/**
 * Utility class
 */
public class Utils {
    /**
     * Format the date with SimpleDateFormat
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Context mContext = null;


    /**
     * Public constructor that takes mContext for later use
     */
    public Utils(Context con) {
        mContext = con;
    }

    public static String encodeEmail(String email) {

        return email.replace(".",",");
    }

    /**
     * Return true if currentUserEmail equals to shoppingList.owner()
     * Return false otherwise
     */
    public static boolean checkIfOwner(ShoppingList shoppingList, String currentUserEmail)
     {
        return (shoppingList.getOwner() != null &&
                shoppingList.getOwner().equals(currentUserEmail));
    }

}
