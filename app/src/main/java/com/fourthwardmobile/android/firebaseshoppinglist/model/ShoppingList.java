package com.fourthwardmobile.android.firebaseshoppinglist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Chris Hare on 7/24/2016.
 */
public class ShoppingList {

    String listName;
    String owner;
    private HashMap<String, User> usersShopping;

    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;

    public ShoppingList() {
    }

    public ShoppingList(String listName, String owner, HashMap<String,Object> timestampCreated) {
        this.listName = listName;
        this.owner = owner;

        //Date last changed will always be set to ServerValue.TIMESTAMP
        //Don't want to use a Calendar to get the time from the client
        //because of lag between sending the date to firebase. Need to know
        //the time the server got the data
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;

        this.usersShopping = new HashMap<>();

    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    //Get long value of the object
    @JsonIgnore
    public long getTimestampLastChangedLong() {
        return (long)timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    @JsonIgnore
    public long getTimestampCreatedLong() {
        return (long)timestampCreated.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    public HashMap<String, User> getUsersShopping() {
        return usersShopping;
    }
}
