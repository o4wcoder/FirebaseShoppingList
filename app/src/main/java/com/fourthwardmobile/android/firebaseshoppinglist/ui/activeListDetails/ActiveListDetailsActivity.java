package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeListDetails;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingListItem;
import com.fourthwardmobile.android.firebaseshoppinglist.model.User;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.BaseActivity;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Represents the details screen for the selected shopping list
 */
public class ActiveListDetailsActivity extends BaseActivity {

    /*************************************************************************************/
    /*                                  Constants                                        */
    /*************************************************************************************/
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();

    /*************************************************************************************/
    /*                                  Local Data                                       */
    /*************************************************************************************/
    private ListView mListView;
    private ShoppingList mShoppingList;
    private Firebase mActiveListRef, mCurrentUserRef;
    private String mListId;
    private ActiveListItemAdapter mActiveListItemAdapter;
    private ValueEventListener mAddValueEventListener, mCurrentUserRefListener;

    //Stores whether the current user is the owner
    private boolean mCurrentUserIsOwner = false;

    //Stores whether the current user is shopping
    private boolean mShopping = false;

    private Button mButtonShopping;
    private TextView mTextViewPeopleShopping;
    private User mCurrentUser;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);

        //GEt push key
        mListId = getIntent().getExtras().getString(Constants.KEY_LIST_ID);

        Log.e(LOG_TAG,"Got key in detail activity = " + mListId);
        if(mListId == null) {
            //No use in continuing if the key is null
            finish();
        }

        //Get Firebase reference to the list
        mActiveListRef = new Firebase(Constants.FIREBASE_URL).child(Constants.FIREBASE_LOCATION_ACTIVE_LISTS).child(mListId);
        mCurrentUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);

        mAddValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                /**
                 * Saving the most recent version of current shopping list into mShoppingList if present
                 * finish() the activity if the list is null (list was removed or unshared by it's owner
                 * while current user is in the list details activity)
                 */
                ShoppingList shoppingList = snapshot.getValue(ShoppingList.class);

                if (shoppingList == null) {
                    finish();
                    /**
                     * Make sure to call return, otherwise the rest of the method will execute,
                     * even after calling finish.
                     */
                    return;
                }
                mShoppingList = shoppingList;
                /**
                 * Pass the shopping list to the adapter if it is not null.
                 * We do this here because mShoppingList is null when first created.
                 */
                mActiveListItemAdapter.setShoppingList(mShoppingList);

                /* Check if the current user is owner */
                mCurrentUserIsOwner = Utils.checkIfOwner(shoppingList, mEncodedEmail);


                /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
                invalidateOptionsMenu();

                /* Set title appropriately. */
                setTitle(shoppingList.getListName());

                /**
                 * Whent the list changes, you can check to see if the current user is part of the
                 * shopping users. If they are, you should style the green shopping button
                 * appropriately and change the state of the activity to "shopping mode"
                 */
                HashMap<String, User> usersShopping = mShoppingList.getUsersShopping();
                if (usersShopping != null && usersShopping.size() != 0 &&
                        usersShopping.containsKey(mEncodedEmail)) {
                    mShopping = true;
                    mButtonShopping.setText(getString(R.string.button_stop_shopping));
                    mButtonShopping.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.dark_grey));
                } else {
                    mButtonShopping.setText(getString(R.string.button_start_shopping));
                    mButtonShopping.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));
                    mShopping = false;
                }

                setWhosShoppingText(mShoppingList.getUsersShopping());


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        //Save teh most up-to-date version of current user in mCurrentUser
        mCurrentUserRefListener = mCurrentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if(currentUser != null)
                    mCurrentUser = currentUser;
                else
                    finish();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG,getString(R.string.log_error_the_read_failed) + firebaseError.getMessage());
            }
        });

        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        mActiveListRef.addValueEventListener(mAddValueEventListener);

        Firebase listItemRef = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);

        //Create Firebase adapter for displaying list if items
        mActiveListItemAdapter = new ActiveListItemAdapter(this, ShoppingListItem.class,
                R.layout.single_active_list_item,listItemRef,mListId, mEncodedEmail);
        //Set adapter to listview
        mListView.setAdapter(mActiveListItemAdapter);

        /**
         * Set up click listeners for interaction.
         */

        /* Show edit list item name dialog on listView item long click event */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if (view.getId() != R.id.list_view_footer_empty) {
                    ShoppingListItem shoppingListItem = mActiveListItemAdapter.getItem(position);

                    if (shoppingListItem != null) {
                        String itemName = shoppingListItem.getItemName();
                        String itemId = mActiveListItemAdapter.getRef(position).getKey();

                        showEditListItemNameDialog(itemName, itemId);
                        return true;
                    }
                }
                return false;
            }
        });

 /* Perform buy/return action on listView item click event if current user is shopping. */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if (view.getId() != R.id.list_view_footer_empty) {
                    final ShoppingListItem selectedListItem = mActiveListItemAdapter.getItem(position);
                    String itemId = mActiveListItemAdapter.getRef(position).getKey();

                    if (selectedListItem != null) {
                        /* If current user is shopping */
                        if (mShopping) {

                            /* Create map and fill it in with deep path multi write operations list */
                            HashMap<String, Object> updatedItemBoughtData = new HashMap<String, Object>();

                            /* Buy selected item if it is NOT already bought */
                            if (!selectedListItem.isBought()) {
                                updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT, true);
                                updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT_BY, mEncodedEmail);
                            } else {
                                updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT, false);
                                updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT_BY, null);
                            }

                            /* Do update */
                            Firebase firebaseItemLocation = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS)
                                    .child(mListId).child(itemId);
                            firebaseItemLocation.updateChildren(updatedItemBoughtData, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError != null) {
                                        Log.d(LOG_TAG, getString(R.string.log_error_updating_data) +
                                                firebaseError.getMessage());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details, menu);

        /**
         * Get menu items
         */
        MenuItem remove = menu.findItem(R.id.action_remove_list);
        MenuItem edit = menu.findItem(R.id.action_edit_list_name);
        MenuItem share = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /* Only the edit and remove options are implemented */
        remove.setVisible(mCurrentUserIsOwner);
        edit.setVisible(mCurrentUserIsOwner);
        share.setVisible(false);
        archive.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Show edit list dialog when the edit action is selected
         */
        if (id == R.id.action_edit_list_name) {
            showEditListNameDialog();
            return true;
        }

        /**
         * removeList() when the remove action is selected
         */
        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

        /**
         * Eventually we'll add this
         */
        if (id == R.id.action_share_list) {
            return true;
        }

        /**
         * archiveList() when the archive action is selected
         */
        if (id == R.id.action_archive) {
            archiveList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        mActiveListItemAdapter.cleanup();

        //Remove listeners
        mActiveListRef.removeEventListener(mAddValueEventListener);
        mCurrentUserRef.removeEventListener(mCurrentUserRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        mTextViewPeopleShopping = (TextView)findViewById(R.id.text_view_people_shopping);
        mButtonShopping = (Button)findViewById(R.id.button_shopping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        /* Common toolbar setup */
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);
        mListView.addFooterView(footer);
    }


    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {
    }


    /**
     * Start AddItemsFromMealActivity to add meal ingredients into the shopping list
     * when the user taps on "add meal" fab
     */
    public void addMeal(View view) {
    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList,mListId);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList,mListId, mEncodedEmail);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        Log.e(LOG_TAG,"showEditListNameDialog()");
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList,mListId,mEncodedEmail);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String itemName, String itemId) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList, itemName,
                itemId, mListId, mEncodedEmail);

        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {

        /**
         * If current user is already shopping, remove current user from usersShopping map
         */
        Firebase usersShoppingRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS)
                .child(mListId).child(Constants.FIREBASE_PROPERTY_USERS_SHOPPING)
                .child(mEncodedEmail);

        //Either add or remove the current user from the usersShopping map
        if(mShopping) {
            usersShoppingRef.removeValue();
        } else {
            usersShoppingRef.setValue(mCurrentUser);
        }

    }

    /**
     * Set appropriate text for Start/Stop shopping button and Who's shopping textView
     * depending on the current user shopping status
     */
    private void setWhosShoppingText(HashMap<String, User> usersShopping) {

        if (usersShopping != null) {
            ArrayList<String> usersWhoAreNotYou = new ArrayList<>();
            /**
             * If at least one user is shopping
             * Add userName to the list of users shopping if this user is not current user
             */
            for (User user : usersShopping.values()) {
                if (user != null && !(user.getEmail().equals(mEncodedEmail))) {
                    usersWhoAreNotYou.add(user.getName());
                }
            }

            int numberOfUsersShopping = usersShopping.size();
            String usersShoppingText;

            /**
             * If current user is shopping...
             * If current user is the only person shopping, set text to "You are shopping"
             * If current user and one user are shopping, set text "You and userName are shopping"
             * Else set text "You and N others shopping"
             */
            if (mShopping) {
                switch (numberOfUsersShopping) {
                    case 1:
                        usersShoppingText = getString(R.string.text_you_are_shopping);
                        break;
                    case 2:
                        usersShoppingText = String.format(
                                getString(R.string.text_you_and_other_are_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    default:
                        usersShoppingText = String.format(
                                getString(R.string.text_you_and_number_are_shopping),
                                usersWhoAreNotYou.size());
                }
                /**
                 * If current user is not shopping..
                 * If there is only one person shopping, set text to "userName is shopping"
                 * If there are two users shopping, set text "userName1 and userName2 are shopping"
                 * Else set text "userName and N others shopping"
                 */
            } else {
                switch (numberOfUsersShopping) {
                    case 1:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_is_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    case 2:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_and_other_are_shopping),
                                usersWhoAreNotYou.get(0),
                                usersWhoAreNotYou.get(1));
                        break;
                    default:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_and_number_are_shopping),
                                usersWhoAreNotYou.get(0),
                                usersWhoAreNotYou.size() - 1);
                }
            }
            mTextViewPeopleShopping.setText(usersShoppingText);
        } else {
            mTextViewPeopleShopping.setText("");
        }
    }
}
