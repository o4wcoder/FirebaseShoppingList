package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeListDetails;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.ui.FirebaseListAdapter;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingListItem;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;

import java.util.HashMap;

/**
 * Created by Chris Hare on 7/28/2016.
 */
public class ActiveListItemAdapter extends FirebaseListAdapter<ShoppingListItem> {

    private ShoppingList mShoppingList;
    private String mListId;


    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ActiveListItemAdapter(Activity activity, Class<ShoppingListItem> modelClass, int modelLayout,
                                 Query ref, String listId) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mListId = listId;
    }

    /**
     * Public method that is used to pass shoppingList object when it is loaded in ValueEventListener
     */
    public void setmShoppingList(ShoppingList shoppingList) {
        this.mShoppingList = shoppingList;
        this.notifyDataSetChanged();
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_active_list_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, final ShoppingListItem item,int position) {


        ImageButton buttonRemoveItem = (ImageButton) view.findViewById(R.id.button_remove_item);
        TextView textViewMealItemName = (TextView) view.findViewById(R.id.text_view_active_list_item_name);

        textViewMealItemName.setText(item.getItemName());

        //Get the id of the item to remove
        final String itemToRemoveId = this.getRef(position).getKey();

        /**
         * Set the on click listener for "Remove list item" button
         */
        buttonRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                        .setTitle(mActivity.getString(R.string.remove_item_option))
                        .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeItem(itemToRemoveId);

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                /* Dismiss the dialog */
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

    }

    /**
     * Public method that is used to pass shoppingList object when it is loaded in ValueEventListener
     */
    public void setShoppingList(ShoppingList shoppingList) {
        this.mShoppingList = shoppingList;
        this.notifyDataSetChanged();
    }

    private void removeItem(String itemId) {

        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        //Make a map for removal
        HashMap<String,Object> updatedRemoveItemMap = new HashMap<>();

        //Remove the item by passing in null
        updatedRemoveItemMap.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/"
        + mListId + "/" + itemId, null);

        //Make the timestamp for the last changed
        HashMap<String,Object> changedTimestampMap = new HashMap<>();
        changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        //Add the updated timestamp
        updatedRemoveItemMap.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS +
        "/" + mListId + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED,changedTimestampMap);

        //Do update
        firebaseRef.updateChildren(updatedRemoveItemMap);

    }
}
