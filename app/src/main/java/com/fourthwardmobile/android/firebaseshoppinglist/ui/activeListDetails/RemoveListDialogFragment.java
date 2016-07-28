package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeListDetails;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;

import java.util.HashMap;


/**
 * Lets the user remove active shopping list
 */
public class RemoveListDialogFragment extends DialogFragment {

    private static final String TAG = RemoveListDialogFragment.class.getSimpleName();

    private String mListId;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static RemoveListDialogFragment newInstance(ShoppingList shoppingList, String mListId) {
        RemoveListDialogFragment removeListDialogFragment = new RemoveListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_LIST_ID,mListId);
        removeListDialogFragment.setArguments(bundle);
        return removeListDialogFragment;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getActivity().getResources().getString(R.string.action_remove_list))
                .setMessage(getString(R.string.dialog_message_are_you_sure_remove_list))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeList();
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        return builder.create();
    }

    private void removeList() {

//        Firebase shoppingListRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS).child(mListId);
//        Log.e(TAG,"removeList()");
//        shoppingListRef.removeValue();

        //We want to delete the list AND any items in that list (if there are any)

        //Create map and fill it with the deep path multi write operation list
        HashMap<String,Object> removeListData = new HashMap<>();


        removeListData.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS + "/"
        + mListId,null);
        removeListData.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/"
        + mListId, null);

        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        //Do a deep-path update
        firebaseRef.updateChildren(removeListData,(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                if(firebaseError != null) {
                    Log.e(TAG,getString(R.string.log_error_updating_data) + firebaseError.getMessage());
                }
            }
        }));
    }

}
