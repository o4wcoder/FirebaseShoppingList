package com.fourthwardmobile.android.firebaseshoppinglist.model;

/**
 * Created by Chris Hare on 7/27/2016.
 */
public class ShoppingListItem {

    private String itemName;
    private String itemOwner;

    public ShoppingListItem() {
    }

    public ShoppingListItem(String itemName, String owner) {
        this.itemName = itemName;
        this.itemOwner = owner;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemOwner() {
        return itemOwner;
    }
}
