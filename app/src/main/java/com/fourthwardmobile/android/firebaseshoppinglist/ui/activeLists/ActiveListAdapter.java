package com.fourthwardmobile.android.firebaseshoppinglist.ui.activeLists;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.ShoppingList;


/**
 * Populates the list_view_active_lists inside ShoppingListsFragment
 */
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout,
                             Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
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
        TextView textViewListName = (TextView)view.findViewById(R.id.text_view_list_name);
        TextView textViewCreatedByUser = (TextView)view.findViewById(R.id.text_view_created_by_user);

        textViewListName.setText(list.getListName());
        textViewCreatedByUser.setText(list.getOwner());
    }
}
