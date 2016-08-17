package com.fourthwardmobile.android.firebaseshoppinglist.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.login.CreateAccountActivity;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.login.LoginActivity;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * BaseActivity class is used as a base class for all activities in the app
 * It implements GoogleApiClient callbacks to enable "Logout" in all activities
 * and defines variables that are being shared across all activities
 */
public abstract class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;
    protected GoogleApiClient mGoogleApiClient;
    protected String mEncodedEmail;
    protected String mProvider;
    protected FirebaseAuth.AuthStateListener mAuthListener;
    protected Firebase mFirebaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Setup the Google API object to allow Google logins */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) //CHRIS ADD
                .requestEmail()
                .build();



        /**
         * Build a GoogleApiClient with access to the Google Sign-In API and the
         * options specified by gso.
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEncodedEmail = sp.getString(Constants.KEY_ENCODED_EMAIL, null);
        mProvider = sp.getString(Constants.KEY_PROVIDER, null);

        if (!((this instanceof LoginActivity) || (this instanceof CreateAccountActivity))) {

            //Get Firebase instance to Authentication login
            mAuth = FirebaseAuth.getInstance();
           // mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

            mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                Log.e(TAG,"onAuthStateChanged()");


                FirebaseUser user = firebaseAuth.getCurrentUser();
                //The user has been logged out
                    if (user == null) {
                        //Clear out shared preferences
                        SharedPreferences.Editor spe = sp.edit();
                        spe.putString(Constants.KEY_ENCODED_EMAIL, null);
                        spe.putString(Constants.KEY_PROVIDER, null);

                        Log.e(TAG,"onAuthStateChanged(): User has signed out");
                        takeUserToLoginScreenOnUnAuth();
                    }
                }
            };
            mAuth.addAuthStateListener(mAuthListener);
        }

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Cleanup listeners
        if(!((this instanceof LoginActivity) || (this instanceof CreateAccountActivity))) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        } else if(id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initializeBackground(LinearLayout linearLayout) {

        /**
         * Set different background image for landscape and portrait layouts
         */
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            linearLayout.setBackgroundResource(R.drawable.background_loginscreen_land);
        } else {
            linearLayout.setBackgroundResource(R.drawable.background_loginscreen);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void showProgressDialog() {
//        if (mAuthProgressDialog == null) {
//            mAuthProgressDialog = new ProgressDialog(this);
//            mAuthProgressDialog.setMessage(getString(R.string.loading));
//            mAuthProgressDialog.setIndeterminate(true);
//        }

        mAuthProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mAuthProgressDialog != null && mAuthProgressDialog.isShowing()) {
            mAuthProgressDialog.dismiss();
        }
    }

    /**
     * Logs out the user from their current session and starts LoginActivity
     * Also disconnects the mGoogleApiClient if connect and provider is Google;
     */
    protected void logout() {

        Log.e(TAG,"logout()");
        //Logout if mProvider is not null
        if(mProvider != null) {
          //  mAuth.unauth();
            mAuth.signOut();
            Log.e(TAG,"logout(): Provider is not null");
            if(mProvider.equals(Constants.GOOGLE_PROVIDER)) {
                //Logout from Google+
                Log.e(TAG,"logout(): Sign out from Google");
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                      Log.e(TAG,"logout()onResult. Sign out from Google success");
                    }
                });
            }
        }
    }

    private void takeUserToLoginScreenOnUnAuth() {

        Log.e(TAG,"Send user back to Login Activity");
        //Move user to LoginActivity and remove the backstack
        Intent intent = new Intent(BaseActivity.this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();


        mGoogleApiClient.disconnect();
    }
}
