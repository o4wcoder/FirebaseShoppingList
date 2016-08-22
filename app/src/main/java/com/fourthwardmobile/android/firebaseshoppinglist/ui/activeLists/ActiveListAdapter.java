package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeLists;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.model.User;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;


/**
 * Populates the list_view_active_lists inside ShoppingListsFragment
 */
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {

    private static final String TAG = ActiveListAdapter.class.getSimpleName();

    private String mEncodedEmail;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout,
                             Query ref, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mEncodedEmail = encodedEmail;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_active_lists)
     * with items inflated from single_active_list.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, ShoppingList list) {
        // TODO This is where you need to populate the single_active_list layout with
        // the data in the current shopping list. It should be similar to what you
        // were displaying in ShoppingListsFragment
        TextView textViewListName = (TextView) view.findViewById(R.id.text_view_list_name);
        final TextView textViewCreatedByUser = (TextView) view.findViewById(R.id.text_view_created_by_user);

        final TextView textViewUsersShopping = (TextView) view.findViewById(R.id.text_view_people_shopping_count);

        String ownerEmail = list.getOwner();

        /* Set the list name and owner */
        textViewListName.setText(list.getListName());

        /**
         * Show "1 person is shopping" if one person is shopping
         * Show "N people shopping" if two or more users are shopping
         * Show nothing if nobody is shopping
         */
        if (list.getUsersShopping() != null) {
            int usersShopping = list.getUsersShopping().size();
            if (usersShopping == 1) {
                textViewUsersShopping.setText(String.format(
                        mActivity.getResources().getString(R.string.person_shopping),
                        usersShopping));
            } else {
                textViewUsersShopping.setText(String.format(
                        mActivity.getResources().getString(R.string.people_shopping),
                        usersShopping));
            }
        } else {
            /* otherwise show nothing */
            textViewUsersShopping.setText("");
        }

        /**
         * Set "Created by" text to "You" if current user is owner of the list
         * Set "Created by" text to userName if current user is NOT owner of the list
         */
        if (ownerEmail != null) {
            if (ownerEmail.equals(mEncodedEmail)) {
                textViewCreatedByUser.setText(mActivity.getResources().getString(R.string.text_you));
            } else {
                Log.e(TAG,"populateView() get Firebase user Reference");
                Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS).child(ownerEmail);
                Log.e(TAG,"poulateView() Got user ref");
                /* Get the user's name */
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e(TAG,"DataChange listener for user Ref");
                        User user = dataSnapshot.getValue(User.class);
                        Log.e(TAG,"Got user");
                        if (user != null) {
                            textViewCreatedByUser.setText(user.getName());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(mActivity.getClass().getSimpleName(),
                                mActivity.getString(R.string.log_error_the_read_failed) +
                                        firebaseError.getMessage());
                    }
                });
            }
        }
    }
}
