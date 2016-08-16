package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingListItem;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.activeLists.AddListDialogFragment;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


/**
 * Lets user add new list item.
 */
public class AddListItemDialogFragment extends EditListDialogFragment {

    /**************************************************************************************/
    /*                                    Constants                                       */
    /**************************************************************************************/
    private static final String TAG = AddListItemDialogFragment.class.getSimpleName();

    /**************************************************************************************/
    /*                                    Local Data                                      */
    /**************************************************************************************/
    private String mListId;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String listId,
                                                        String encodedEmail) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();

        Bundle bundle = newInstanceHelper(shoppingList, R.layout.dialog_add_item, listId,encodedEmail);
        bundle.putString(Constants.KEY_LIST_ID,listId);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListId = getArguments().getString(Constants.KEY_LIST_ID);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    /**
     * Adds new item to the current shopping list
     */
    @Override
    protected void doListEdit() {
        Log.e(TAG,"doListEdit()");

        String itemName = mEditTextForList.getText().toString();

        if(!itemName.equals("")) {

            Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
            Log.e(TAG,"get reference to shoppingListItems");
            Firebase itemsRef = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);

            //Make a map for the item you are adding
            HashMap<String, Object> updatedItemToAddMap = new HashMap<>();

            //Get push request which will produce unique Id
            Firebase newItemRef = itemsRef.push();
            String itemId = newItemRef.getKey();

            //Make a POJO for the item and immediately turn int into a HashMap
            ShoppingListItem itemToAddObject = new ShoppingListItem(itemName,mEncodedEmail);
            HashMap<String, Object> itemToAdd = (HashMap<String, Object>) new ObjectMapper().convertValue(itemToAddObject,Map.class);

            //Add item to the update map
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/"
            + mListId + "/" + itemId,itemToAdd);

            //Make the timestamp for the last changed
            HashMap<String, Object> changedTimestampMap = new HashMap<>();
            changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            //Add the updated timestamp
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS +
            "/" + mListId + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED,changedTimestampMap);

            //Do the update
            firebaseRef.updateChildren(updatedItemToAddMap);

           //Close the dialog fragment when done
            AddListItemDialogFragment.this.getDialog().cancel();

        }

    }
}
