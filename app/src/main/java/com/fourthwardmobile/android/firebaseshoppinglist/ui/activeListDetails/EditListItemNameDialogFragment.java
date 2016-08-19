package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingListItem;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;

import java.util.HashMap;
import java.util.Map;


/**
 * Lets user edit list item name for all copies of the current list
 */
public class EditListItemNameDialogFragment extends EditListDialogFragment {

    /******************************************************************************************/
    /*                                   Constants                                            */
    /******************************************************************************************/
    private static final String TAG = EditListItemNameDialogFragment.class.getSimpleName();

    /******************************************************************************************/
    /*                                   Local Data                                           */
    /******************************************************************************************/
    String mItemName;
    String mItemId;


    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListItemNameDialogFragment newInstance(ShoppingList shoppingList, String itemName,
                                                             String itemId, String listId, String encodedEmail) {
        EditListItemNameDialogFragment editListItemNameDialogFragment = new EditListItemNameDialogFragment();

        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_item,
                listId, encodedEmail);
        bundle.putString(Constants.KEY_LIST_ITEM_NAME, itemName);
        bundle.putString(Constants.KEY_LIST_ITEM_ID, itemId);
        editListItemNameDialogFragment.setArguments(bundle);

        return editListItemNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemName = getArguments().getString(Constants.KEY_LIST_ITEM_NAME);
        mItemId = getArguments().getString(Constants.KEY_LIST_ITEM_ID);

    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         */
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        helpSetDefaultValueEditText(mItemName);
        return dialog;
    }

    /**
     * Change selected list item name to the editText input if it is not empty
     */
    protected void doListEdit() {

        Log.e(TAG,"doListEdit()");

        String nameInput = mEditTextForList.getText().toString();

        if(!nameInput.equals("") && !mItemName.equals(nameInput)) {

            Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

            HashMap<String, Object> updatedItemToEditMap = new HashMap<>();



            //Add the new name and update the map
            updatedItemToEditMap.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/"
                    + mListId + "/" + mItemId +"/" + Constants.FIREBASE_PROPERTY_ITEM_NAME,nameInput);

            //Make the timestamp for the last changed
            HashMap<String, Object> changedTimestampMap = new HashMap<>();
            changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            //Add the updated timestamp
            updatedItemToEditMap.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS +
                    "/" + mListId + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED,changedTimestampMap);


            firebaseRef.updateChildren(updatedItemToEditMap);
        }
    }
}
