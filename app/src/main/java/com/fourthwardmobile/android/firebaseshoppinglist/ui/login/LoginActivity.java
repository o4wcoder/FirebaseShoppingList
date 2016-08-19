package com.fourthwardmobile.android.firebaseshoppinglist.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.fourthwardmobile.android.firebaseshoppinglist.R;
import com.fourthwardmobile.android.firebaseshoppinglist.model.User;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.BaseActivity;
import com.fourthwardmobile.android.firebaseshoppinglist.ui.MainActivity;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Constants;
import com.fourthwardmobile.android.firebaseshoppinglist.utils.Utils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Chris Hare on 8/1/2016.
 */
public class LoginActivity extends BaseActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    /* A dialog that is presented until the Firebase authentication finished. */
    //private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //private GoogleApiClient mGoogleApiClient;

    /**
     * Variables related to Google Login
     */
    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;
    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_GOOGLE_LOGIN = 1;
    /* A Google account object that is populated if the user signs in with Google */
    GoogleSignInAccount mGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get Firebase instance to Authentication login
        mAuth = FirebaseAuth.getInstance();
        /**
         * Link layout elements from XML and setup progress dialog
         */
        initializeScreen();

        /**
         * Call signInPassword() when user taps "Done" keyboard action
         */
        mEditTextPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    signInPassword();
                }
                return true;
            }
        });



    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.e(LOG_TAG,"onResume()");
        /**
         * This is the authentication listener that maintain the current user session
         * and signs in automatically on application launch
         */

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                hideProgressDialog();

                Log.e(LOG_TAG,"onResume():onAuthStateChanged Already logged in, to go MainActivity()");
                /**
                 * If there is a valid session to be restored, start MainActivity.
                 * No need to pass data via SharedPreferences because app
                 * already holds userName/provider data from the latest session
                 */
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        //Check to see whether SharePreferences has an email set for someone trying to sign up
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spe = sp.edit();

        //Get the newly registered user email if present, use null as default value
        String signupEmail = sp.getString(Constants.KEY_SIGNUP_EMAIL,null);

        //Fill in the email editText and remove value from SharePreference if email is present
        if(signupEmail != null) {
            mEditTextEmailInput.setText(signupEmail);

            //Clear signupEmail sharePreferences to make sure that they are used just once
            spe.putString(Constants.KEY_SIGNUP_EMAIL,null).apply();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mAuth.removeAuthStateListener(mAuthListener);
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
     * Sign in with Password provider when user clicks sign in button
     */
    public void onSignInPressed(View view) {
        signInPassword();
    }

    /**
     * Open CreateAccountActivity when user taps on "Sign up" TextView
     */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = (EditText) findViewById(R.id.edit_text_password);
        LinearLayout linearLayoutLoginActivity = (LinearLayout) findViewById(R.id.linear_layout_login_activity);
        initializeBackground(linearLayoutLoginActivity);
        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
//        mAuthProgressDialog = new ProgressDialog(this);
//        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
//        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
//        mAuthProgressDialog.setCancelable(false);
        /* Setup Google Sign In */
        setupGoogleSignIn();
    }

    /**
     * Sign in with Password provider (used when user taps "Done" action on keyboard)
     */
    public void signInPassword() {

        Log.e(LOG_TAG,"signInPassword()");
        final String email = mEditTextEmailInput.getText().toString();
        String password = mEditTextPasswordInput.getText().toString();

        if(email.equals("")) {
            mEditTextEmailInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }

        if(password.equals("")) {
            mEditTextPasswordInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }

       // mAuthProgressDialog.show();
         showProgressDialog();
        //Callback for Email/Password login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.e(LOG_TAG,"signInWithEmail:onComplete(): " + task.isSuccessful());

                        //mAuthProgressDialog.dismiss();
                        hideProgressDialog();
                        //If sign in fails, display a message to the user. If sign in succeeds
                        //the auth sate listener will be notified and handled
                        if(!task.isSuccessful()) {
                            Log.e(LOG_TAG, "signInWithEmail:failed with exception = " + task.getException().toString());
                            if((task.getException() instanceof FirebaseAuthInvalidCredentialsException) ||
                                    (task.getException() instanceof FirebaseAuthInvalidUserException)) {
                                //Problem with the email or password
                                mEditTextEmailInput.setError(task.getException().getMessage());
                            } else {

                                showErrorToast(task.getException().getMessage());
                            }
                        } else {


                            setAuthenticatedUserPasswordProvider(task.getResult().getUser());

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor spe = sp.edit();

                            spe.putString(Constants.KEY_ENCODED_EMAIL,mEncodedEmail).apply();
                            spe.putString(Constants.KEY_PROVIDER,Constants.PASSWORD_PROVIDER).apply();

                            //Got to main activity when done loging in
                            goToMainActivity();
                        }
                    }
                });


    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's email/password provider.
     */
    private void setAuthenticatedUserPasswordProvider(FirebaseUser user) {

        final String unprocessedEmail = user.getEmail();
        mEncodedEmail = Utils.encodeEmail(unprocessedEmail);

        final Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);

        //Check if current user has logged in at least once
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user != null) {

                    /**
                     * If recently registered user has hasLoggedInWithPassword = "false
                     * (never logged in using password provider)
                     */
                    if (!user.isHasLoggedInWithPassword()) {

                        /**
                         * Change password if user that just signed in signed up recently to make
                         * sure that user will be able to use temporary passwrd from the email more
                         * than 24 hours
                         */

                         mAuth.getCurrentUser().updatePassword(mEditTextPasswordInput.getText().toString())
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if (task.isSuccessful()) {
                                             Log.e(LOG_TAG,"updatePassword():onComplete successful");
                                             userRef.child(Constants.FIREBASE_PROPERTY_USER_HAS_LOGGED_IN_WITH_PASSWORD)
                                                 .setValue(true);
                                         Log.e(LOG_TAG,getString(R.string.log_message_password_changed_successfully) +
                                         mEditTextPasswordInput.getText().toString());
                                         } else {
                                             Log.e(LOG_TAG,getString(R.string.log_error_failed_to_change_password) + task.getException().getMessage());
                                         }

                                     }
                                 });


//                                 addOnSuccessListener(new Firebase.ResultHandler() {
//                                                          @Override
//                                                          public void onSuccess() {
//                                                              userRef.child(Constants.FIREBASE_PROPERTY_USER_HAS_LOGGED_IN_WITH_PASSWORD)
//                                                 .setValue(true);
//                                         Log.e(LOG_TAG,getString(R.string.log_message_password_changed_successfully) +
//                                         mEditTextPasswordInput.getText().toString());
//                                                          }
//
//                                                          @Override
//                                                          public void onError(FirebaseError firebaseError) {
//                                                              Log.e(LOG_TAG,getString(R.string.log_error_failed_to_change_password) + firebaseError);
//                                                          }
//                                                      });


                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

                Log.e(LOG_TAG,getString(R.string.log_error_the_read_failed) + firebaseError.getMessage());
            }
        });

    }

//    /**
//     * Helper method that makes sure a user is created if the user
//     * logs in with Firebase's Google login provider.
//     * @param authData AuthData object returned from onAuthenticated
//     */
//    private void setAuthenticatedUserGoogle(AuthData authData){
//
//    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Signs you into ShoppingList++ using the Google Login Provider
     * @param token A Google OAuth access token returned from Google
     */
//    private void loginWithGoogle(String token) {
//
//       Log.e(LOG_TAG,"Login with Google sucessful! ");
//        Log.e(LOG_TAG,"Got token = " + token.toString());
//
//
//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//        //Clear back stack so clicking the back button does not bring user back
//        //to the login screen
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        //Close login screen
//        finish();
//    }


    /**
     * GOOGLE SIGN IN CODE
     *
     * This code is mostly boiler plate from
     * https://developers.google.com/identity/sign-in/android/start-integrating
     * and
     * https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java
     *
     * The big picture steps are:
     * 1. User clicks the sign in with Google button
     * 2. An intent is started for sign in.
     *      - If the connection fails it is caught in the onConnectionFailed callback
     *      - If it finishes, onActivityResult is called with the correct request code.
     * 3. If the sign in was successful, set the mGoogleAccount to the current account and
     * then call get GoogleOAuthTokenAndLogin
     * 4. getGoogleOAuthTokenAndLogin launches an AsyncTask to get an OAuth2 token from Google.
     * 5. Once this token is retrieved it is available to you in the onPostExecute method of
     * the AsyncTask. **This is the token required by Firebase**
     */


    /* Sets up the Google Sign In Button : https://developers.google.com/android/reference/com/google/android/gms/common/SignInButton */
    private void setupGoogleSignIn() {
        SignInButton signInButton = (SignInButton)findViewById(R.id.login_with_google);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInGooglePressed(v);
            }
        });
    }

    /**
     * Sign in with Google plus when user clicks "Sign in with Google" textView (button)
     */
    public void onSignInGooglePressed(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
        Log.e(LOG_TAG,"Show progress dialog");
       // mAuthProgressDialog.show();
        showProgressDialog();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        /**
         * An unresolvable error has occurred and Google APIs (including Sign-In) will not
         * be available.
         */
       // mAuthProgressDialog.dismiss();
        hideProgressDialog();
        showErrorToast(result.toString());
    }


    /**
     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.e(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            /* Signed in successfully, get the OAuth token */
            mGoogleAccount = result.getSignInAccount();
            //getGoogleOAuthTokenAndLogin();
            Log.e(LOG_TAG,"Signed in user = " + mGoogleAccount.getDisplayName());
            firebaseAuthWithGoogle();

        } else {
            if (result.getStatus().getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                showErrorToast("The sign in was cancelled. Make sure you're connected to the internet and try again.");
            } else {
                showErrorToast("Error handling the sign in: " + result.getStatus().getStatusMessage());
            }
           // mAuthProgressDialog.dismiss();
            hideProgressDialog();
        }
    }

    private void firebaseAuthWithGoogle() {

        AuthCredential credential = GoogleAuthProvider.getCredential(mGoogleAccount.getIdToken(),null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.e(LOG_TAG,"signInWithCredential:onComplete: " + task.isSuccessful());

                        if(!task.isSuccessful()) {
                            showErrorToast(task.getException().getMessage());
                        } else {

                            String unprocessedEmail;
                            FirebaseUser user = task.getResult().getUser();
                            Log.e(LOG_TAG,"signInWithCredential:onComplete Google Sign-in with user = " + user.getEmail());

                           // Log.e(LOG_TAG,"Got provider = " + task.getResult().getUser().getProviders().get(0));
                            //Get lowercase email and replace "." with "," to be able to use ask Firebase Key

                            //Save Google email in shared preferences
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor spe = sp.edit();

                            if (mGoogleApiClient.isConnected()) {
                                unprocessedEmail = user.getEmail().toLowerCase();
                                spe.putString(Constants.KEY_GOOGLE_EMAIL, unprocessedEmail).apply();
                            } else {
                                /**
                                 * Otherwise get email from sharedPreferences, uas null as default value
                                 * This mean the user has resumed his session
                                 */
                                unprocessedEmail = sp.getString(Constants.KEY_GOOGLE_EMAIL, null);

                            }
                            mEncodedEmail = Utils.encodeEmail(unprocessedEmail);
                            //Get user name
                            final String userName = user.getDisplayName();

                            Log.e(LOG_TAG,"Storing to shared pref, email = " + mEncodedEmail);
                            spe.putString(Constants.KEY_ENCODED_EMAIL,mEncodedEmail).apply();

                            //Store Google Provider
                            spe.putString(Constants.KEY_PROVIDER,Constants.GOOGLE_PROVIDER).apply();

                            //Create user in Firebase if it does not exist
                            final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);
                            userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    //If there is not user, create one
                                    if(dataSnapshot.getValue() == null) {
                                        Log.e(LOG_TAG,"signInWithCredential:onDataChange Create new user " + mEncodedEmail);
                                        //Set raw version of data to the ServerValue.TIMESTAMP and save into dateCreatedMap
                                        HashMap<String, Object> timestampJoined = new HashMap<>();
                                        timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                                        User newUser = new User(userName,mEncodedEmail,timestampJoined);
                                        userLocation.setValue(newUser);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.e(LOG_TAG, getString(R.string.log_error_occurred) + firebaseError.getMessage());
                                }
                            });
                        }

                       // mAuthProgressDialog.dismiss();
                        hideProgressDialog();

                        //Got to main activity when done loging in
                        goToMainActivity();

                    }

                });


    }

    private void goToMainActivity() {
                                  /* Go to main activity */
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
