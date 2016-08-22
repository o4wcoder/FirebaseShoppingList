package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeLists;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.activeListDetails.ActiveListDetailsActivity;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Utils;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass that shows a list of all shopping lists a user can see.
 * Use the {@link ShoppingListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListsFragment extends Fragment {

    private static final String TAG = ShoppingListsFragment.class.getSimpleName();

    private ListView mListView;
    private String mEncodedEmail;
    private ActiveListAdapter mActiveListAdapter;

    public ShoppingListsFragment() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static ShoppingListsFragment newInstance(String encodedEmail) {
        ShoppingListsFragment fragment = new ShoppingListsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL,encodedEmail);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Initalize UI elements
         */
        View rootView = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
        initializeScreen(rootView);

        //Add listener to read from database when data has changed
       // Firebase listNameRef = new Firebase(Constants.FIREBASE_URL).child(Constants.FIREBASE_LOCATION_ACTIVE_LISTS);
//        Firebase activeListsRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS);


        //Set up adapter
//        mActiveListAdapter = new ActiveListAdapter(getActivity(),ShoppingList.class,
//                R.layout.single_active_list,activeListsRef,mEncodedEmail);
//        mListView.setAdapter(mActiveListAdapter);
        /**
         * Set interactive bits, such as click events and adapters
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ShoppingList selectedList = mActiveListAdapter.getItem(position);

                if(selectedList != null) {
                    Intent intent = new Intent(getActivity(), ActiveListDetailsActivity.class);

                    String key = mActiveListAdapter.getRef(position).getKey();
                    Log.e(TAG, "Got key = " + key);
                    intent.putExtra(Constants.KEY_LIST_ID, key);
                    startActivity(intent);
                }

            }
        });

//        mListNameTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e(TAG,"onItemClick()");
//                Intent intent = new Intent(getActivity(),ActiveListDetailsActivity.class);
//                startActivity(intent);
//            }
//        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPref.getString(Constants.KEY_PREF_SORT_ORDER_LISTS,Constants.ORDER_BY_KEY);

        //Recreate the adapter when the user comes back from the SettingsActivity. Because of this
        //Adapter creation was moved out of onCreateView.
        //Grab the key/value pair that was set in SettingsActivity and modify the Firebase query
        //that is passed to the adapter accordingly.
        Query orderedActiveUserListsRef;
        Firebase activeListsRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail);

        /**
         * Sort active lists by "date created" if it's been selectedc in the SettingsActivity
         */
        if (sortOrder.equals(Constants.ORDER_BY_KEY)) {
            orderedActiveUserListsRef = activeListsRef.orderByKey();
        } else {

            /**
             * Sort active by lists by name or datelastChanged. Otherwise
             * depending on what's been selected in SettingsActivity
             */

            orderedActiveUserListsRef = activeListsRef.orderByChild(sortOrder);
        }

        /**
         * Create the adapter with selected sort order
         */
        mActiveListAdapter = new ActiveListAdapter(getActivity(), ShoppingList.class,
                R.layout.single_active_list, orderedActiveUserListsRef,
                mEncodedEmail);

        /**
         * Set the adapter to the mListView
         */
        mListView.setAdapter(mActiveListAdapter);

    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mActiveListAdapter.cleanup();
//    }

    @Override
    public void onPause() {
        super.onPause();
        //Since we're recreating the adpater whenever the user comes back to the activity,
        //Need to move the cleanup of the adapter from onDestory, to onPause()
        mActiveListAdapter.cleanup();
    }

    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list_view_active_lists);
//        mListNameTextView = (TextView) rootView.findViewById(R.id.text_view_list_name);
//        mOwnerTextView = (TextView) rootView.findViewById(R.id.text_view_created_by_user);
//        mTimestampTextView = (TextView) rootView.findViewById(R.id.text_view_edit_time);
    }
}
