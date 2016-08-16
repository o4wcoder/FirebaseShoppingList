package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;

import java.util.HashMap;
import java.util.Objects;


/**
 * Lets user edit the list name for all copies of the current list
 */
public class EditListNameDialogFragment extends EditListDialogFragment {

    /***************************************************************************************/
    /*                                Constants                                            */
    /***************************************************************************************/
    private static final String TAG = ActiveListDetailsActivity.class.getSimpleName();

    /***************************************************************************************/
    /*                                  Local Data                                         */
    /***************************************************************************************/
    private String mListName;


    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListNameDialogFragment newInstance(ShoppingList shoppingList,
                                                         String listId,String encodedEmail) {
        EditListNameDialogFragment editListNameDialogFragment = new EditListNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_list,
                listId,encodedEmail);

        //Add list name to the bundle
        bundle.putString(Constants.KEY_LIST_NAME,shoppingList.getListName());
        editListNameDialogFragment.setArguments(bundle);
        return editListNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListName = getArguments().getString(Constants.KEY_LIST_NAME);
        Log.e(TAG,"onCreate with list name = " + mListName);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        helpSetDefaultValueEditText(mListName);

        return dialog;
    }

    /**
     * Changes the list name in all copies of the current list
     */
    protected void doListEdit() {

        Log.e(TAG,"doListEdit()");

        final String inputListName = mEditTextForList.getText().toString();

        //Make sure text is n ot empty
        if(!inputListName.equals("")) {
            Log.e(TAG,"inputListName is not null");
            //Make sure list name from db is not null
            if(mListName != null && mListId != null) {

                //Make sure text has changed
                if(!inputListName.equals(mListName)) {
                    Log.e(TAG,"Edit list at key mListId = " +mListId);
                    //Get Firebase ref
                   Firebase shoppingListRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS).child(mListId);

                    //Make hashmap for the specific properties that are changing
                    HashMap<String,Object> updateProperties = new HashMap<>();
                    updateProperties.put(Constants.FIREBASE_PROPERTY_LIST_NAME,inputListName);

                    //Add the timestamp for the last changed to updateProperties Hashmap
                    HashMap<String,Object> changedTimestampMap = new HashMap<>();
                    changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    //Add the updated timestamp
                    updateProperties.put(Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED,changedTimestampMap);

                    shoppingListRef.updateChildren(updateProperties);
                }


            } else {
                Log.e(TAG,"list name or list id is null");
            }
        }

    }
}

