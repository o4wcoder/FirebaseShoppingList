package com.fourthwardmobile.android.firebaseshoppinglist.utils;

/**
 * Constants class store most important strings and paths of the app
 */
public final class Constants {

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where active lists are stored (ie "activeLists")
     */
    public static final String FIREBASE_LOCATION_ACTIVE_LISTS = "activeLists";
    public static final String FIREBASE_LOCATION_SHOPPING_LIST_ITEMS = "shoppingListItems";
    public static final String FIREBASE_LOCATION_USERS = "users";

    /**
     * Constants for Firebase object properties
     */
    public static final String FIREBASE_PROPERTY_BOUGHT = "bought";
    public static final String FIREBASE_PROPERTY_BOUGHT_BY = "boughtBy";
    public static final String FIREBASE_PROPERTY_LIST_NAME = "listName";
    public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED = "timestampLastChanged";
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String FIREBASE_PROPERTY_ITEM_NAME = "itemName";
    public static final String FIREBASE_PROPERTY_EMAIL = "email";
    public static final String FIREBASE_PROPERTY_USERS_SHOPPING = "usersShopping";
    public static final String FIREBASE_PROPERTY_USER_HAS_LOGGED_IN_WITH_PASSWORD = "hasLoggedInWithPassword";


    /**
     * Constants for Firebase URL
     */
    //NOTE!! This should be put into a gradle.properties file so it can be hidden
    //but keeping it here so this project can be shared, compiled and run by others
    public static final String FIREBASE_URL = "https://fir-shoppinglist.firebaseio.com/";
    public static final String FIREBASE_URL_ACTIVE_LISTS = FIREBASE_URL + "/" + FIREBASE_LOCATION_ACTIVE_LISTS;
    public static final String FIREBASE_URL_SHOPPING_LIST_ITEMS = FIREBASE_URL + "/" + FIREBASE_LOCATION_SHOPPING_LIST_ITEMS;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;


    /**
     * Constants for bundles, extras and shared preferences keys
     */
    public static final String KEY_LIST_NAME = "LIST_NAME";
    public static final String KEY_LAYOUT_RESOURCE = "LAYOUT_RESOURCE";
    public static final String KEY_LIST_ITEM_NAME = "LIST_ITEM_NAME";
    public static final String KEY_GOOGLE_EMAIL = "GOOGLE_EMAIL";
    public static final String KEY_LIST_ID = "LIST_ID";
    public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";
    public static final String KEY_LIST_ITEM_ID = "ITEM_ID";
    public static final String KEY_LIST_OWNER = "LIST_OWNER";
    public static final String KEY_ENCODED_EMAIL = "USER_EMAIL";
    public static final String KEY_PROVIDER = "PROVIDER";

    /**
     * Constants for Firebase login
     */
    public static final String PASSWORD_PROVIDER = "password";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";


}
