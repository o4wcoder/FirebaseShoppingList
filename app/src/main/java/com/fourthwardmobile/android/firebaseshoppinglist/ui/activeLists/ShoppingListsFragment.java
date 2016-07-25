package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeLists;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
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
    private TextView mListNameTextView;
    private TextView mOwnerTextView;
    private TextView mTimestampTextView;

    public ShoppingListsFragment() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static ShoppingListsFragment newInstance() {
        ShoppingListsFragment fragment = new ShoppingListsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
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
        Firebase listNameRef = new Firebase(Constants.FIREBASE_URL).child(Constants.FIREBASE_LOCATION_ACTIVE_LISTS);
        listNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG,"The data changed");
                //Get the name of the list
                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);
                if(shoppingList != null) {
                    mListNameTextView.setText(shoppingList.getListName());
                    mOwnerTextView.setText(shoppingList.getOwner());
                    if(shoppingList.getTimestampLastChanged() != null) {
                        String strDate = Utils.SIMPLE_DATE_FORMAT.format(new Date(shoppingList.getTimestampLastChangedLong()));
                        mTimestampTextView.setText(String.valueOf(strDate));
                    }
                }
                else {
                    mTimestampTextView.setText("");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        /**
         * Set interactive bits, such as click events and adapters
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }
        });

        mListNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"onItemClick()");
                Intent intent = new Intent(getActivity(),ActiveListDetailsActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list_view_active_lists);
        mListNameTextView = (TextView) rootView.findViewById(R.id.text_view_list_name);
        mOwnerTextView = (TextView) rootView.findViewById(R.id.text_view_created_by_user);
        mTimestampTextView = (TextView) rootView.findViewById(R.id.text_view_edit_time);
    }
}
