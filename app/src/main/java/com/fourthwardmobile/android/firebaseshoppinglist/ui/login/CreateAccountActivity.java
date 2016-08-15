package com.fourthwardmobile.android.firebaseshoppinglist.ui.login;

/**
 * Created by Chris Hare on 8/1/2016.
 */
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.User;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.BaseActivity;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents Sign up screen and functionality of the app
 */
public class CreateAccountActivity extends BaseActivity {
    private static final String TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private String mUserName,mUserEmail,mPassword;

    //Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();

    }

    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {

        Firebase ref = new Firebase(Constants.FIREBASE_URL);

        mUserName = mEditTextUsernameCreate.getText().toString();
        mUserEmail = mEditTextEmailCreate.getText().toString();
        mPassword = mEditTextPasswordCreate.getText().toString();

        //Check if input is valid
        boolean validUser = isUserNameValid(mUserName);
        boolean validEmail = isEmailValid(mUserEmail);
        boolean validPass = isPasswordValid(mPassword);

        if(validUser && validEmail && validPass) {

            //If everything was vallid show progress dialog
            mAuthProgressDialog.show();

            mAuth.createUserWithEmailAndPassword(mUserEmail,mPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.e(TAG,"createUserWithEmail:onComplete " + task.isSuccessful());

                            if(!task.isSuccessful()) {

                                Log.e(TAG,"Create failed with exception = " + task.getException().toString());

                                if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    mEditTextEmailCreate.setError(task.getException().getMessage());
                                } else {
                                showErrorToast(task.getException().getMessage());
                               }
                            } else {
                                Log.e(TAG,"Success creating firebase user. Add to the database");
                                createUserInFirebaseHelper();
                            }


                            mAuthProgressDialog.dismiss();
                        }
                    });

        } else {
            showErrorToast("Missing input fields");
        }

    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper() {

        final String encodedEmail = Utils.encodeEmail(mUserEmail);
        final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(encodedEmail);

        /**
         * See if there is already a user (for example, if they already logged in with an associated
         * Google account
         */
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //If there is no user, create one
                if(dataSnapshot.getValue() == null) {

                    //Set raw version of data to the ServerValue.TIMESTAMP and save into dateCreatedMap
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(mUserName,encodedEmail,timestampJoined);
                    userLocation.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

                Log.e(TAG,getString(R.string.log_error_occurred) + firebaseError.getMessage());
            }
        });
    }

    private boolean isEmailValid(String email) {

        boolean isGoodEmail = (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches());

        if(!isGoodEmail) {
            mEditTextEmailCreate.setError(String.format(getString(R.string.error_invalid_email_not_valid),email));

            return false;
        }

        return isGoodEmail;
    }

    private boolean isUserNameValid(String userName) {

        if(userName.equals("")) {
            mEditTextUsernameCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password) {

        if(password.length() < 6) {
            mEditTextPasswordCreate.setError(getResources().getString(R.string.error_invalid_password_not_valid));

            return false;
        }
        return true;
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
