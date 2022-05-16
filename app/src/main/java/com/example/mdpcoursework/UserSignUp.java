package com.example.mdpcoursework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


//refer to:
//    sign up using password & email: https://firebase.google.com/docs/auth/android/password-auth
//    sign up using Google account: https://firebase.google.com/docs/auth/android/google-signin

//purpose: let user sign up for a new account
public class UserSignUp extends AppCompatActivity implements HideSystemBars, HideSoftKeyboard{


    FirebaseAuth mAuth;
    View screen;

    //Google Sign Up
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> google_signupLauncher;

    Button signup_btn, back_btn;
    ImageButton google_btn;
    EditText email_edit, password_edit, username_edit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        //connect to Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        //set layout to display
        setContentView(R.layout.user_signup_page);

        //get layout elements
        back_btn = findViewById(R.id.back_btn);
        signup_btn = findViewById(R.id.signup_btn);
        google_btn = findViewById(R.id.google_signup_btn);
        email_edit = findViewById(R.id.email_edit);
        password_edit = findViewById(R.id.password_edit);
        username_edit = findViewById(R.id.username_edit);

        //set up necessary elements for signing up using Google account
        setup_google_signup();

        //if back button is clicked, back to previous activity
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //if sign up button is clicked, sign user up using the email and password entered
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_password_signup();
            }
        });

        //if google button is clicked, sign user up using his/her Google account
        google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                google_signup();
            }
        });

        //to dismiss soft keyboard after done typing
        //dismissKeyboard(): implemented from HideSoftKeyboard interface
        email_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                //if user is no longer typing and user clicks somewhere else on the screen
                //dismiss soft keyboard
                if (!typing){
                    dismissKeyboard(UserSignUp.this,view);
                }
            }
        });

        //to dismiss soft keyboard after done typing
        password_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                if (!typing){
                    dismissKeyboard(UserSignUp.this,view);
                }
            }
        });

        //to dismiss soft keyboard after done typing
        username_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                if (!typing){
                    dismissKeyboard(UserSignUp.this,view);
                }
            }
        });

    }


    //method to set up necessary materials for signing up using Google account
    private void setup_google_signup() {

        //configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //launcher for Google sign up Intent
        //it will process the Intent when user wants to sign up using Google
        google_signupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        //if intent is successfully launched,
                        if(result.getResultCode() == Activity.RESULT_OK){

                            //get the Intent data, which is the Google sign up Intent
                            Intent data = result.getData();

                            //try signing-in to user's Google account
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                            try {
                                // Google Sign In is successful
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                // Sign user up using the Google account
                                firebaseAuthWithGoogle(account.getIdToken()); //authenticate Google account with Firebase

                            } catch (ApiException e) {
                                // Google Sign In failed, toast message
                                Toast.makeText(UserSignUp.this, "Error",Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

    }


    //method to execute google signup
    private void google_signup() {

        //set up intent for signing up using Google
        Intent signupIntent = mGoogleSignInClient.getSignInIntent();
        //launch Google sign up intent
        google_signupLauncher.launch(signupIntent);

    }


    //method to authenticate user's Google account with Firebase
    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //if sign up is successful,
                        if (task.isSuccessful()) {
                            Toast.makeText(UserSignUp.this, "signUpWithGoogle:success",Toast.LENGTH_SHORT).show();

                            //add user's account info to database USER section

                            //as user can sign in to the app using his Google account without signing up first
                            //we need to check whether user has previously signed up/in using the same Google account
                            //             and whether user's account info is already in the database
                            //if yes, move on, if no, add user's account info to database

                            //connect to Firebase Auth and get user
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            //get user's id
                            String user_id = user.getUid();
                            //connect to Firebase database and point to USER section
                            FirebaseDatabase db = FirebaseDatabase
                                    .getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
                            DatabaseReference reference = db.getReference("USER");

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    //if user doesn't exist in USER section
                                    if (!snapshot.child(user_id).exists()){
                                        //add user info to USER section on the database
                                        String email = user.getEmail();
                                        String username = user.getDisplayName();
                                        reference.child(user_id).child("username").setValue(username);
                                        reference.child(user_id).child("age").setValue("0"); //default age
                                        reference.child(user_id).child("gender").setValue("undefined"); //default gender
                                        reference.child(user_id).child("user_id").setValue(user_id);
                                        reference.child(user_id).child("email").setValue(email);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(UserSignUp.this, "Error"+error.toString(),Toast.LENGTH_SHORT).show();
                                }
                            });

                            //after that, go to main page
                            startActivity(new Intent(UserSignUp.this,MainActivity.class));

                        }
                        //sign in fails, display a message to the user.
                        else {
                            Toast.makeText(UserSignUp.this, "signInWithCredential:failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //method to validate users' email and password for sign up
    private void email_password_signup() {

        //if everything is filled
        if(!TextUtils.isEmpty(email_edit.getText().toString()) &&
                !TextUtils.isEmpty(password_edit.getText().toString()) && !TextUtils.isEmpty(username_edit.getText().toString()))
        {
            //get users' inputs
            String email = email_edit.getText().toString();
            String password = password_edit.getText().toString();
            String username = username_edit.getText().toString();

            //sign up user account using email and password
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //if account is successfully created, add user's account info onto Firebase database
                    if (task.isSuccessful()) {

                        Toast.makeText(UserSignUp.this,
                                "Welcome, " + username + "!",
                                Toast.LENGTH_SHORT).show();

                        //retrieve the user who has just signed up for an account
                        FirebaseUser user = mAuth.getCurrentUser();
                        //get user's id
                        assert user != null;
                        String user_id = user.getUid();
                        //connect to database
                        FirebaseDatabase db = FirebaseDatabase
                                .getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
                        DatabaseReference reference = db.getReference("USER").child(user_id);
                        //write user's info to database, user is identified by user id
                        reference.child("username").setValue(username);
                        reference.child("age").setValue("0"); //default age
                        reference.child("gender").setValue("undefined"); //default gender
                        reference.child("user_id").setValue(user_id);
                        reference.child("email").setValue(email);

                        //go to Main Page
                        startActivity(new Intent(UserSignUp.this, MainActivity.class));
                    }

                    //account isn't successfully created, display error message
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserSignUp.this);
                        builder.setMessage("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        builder.setCancelable(true);
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });

        }

        //user is missing any of the required inputs, display error message to user
        else{

            AlertDialog.Builder builder = new AlertDialog.Builder(UserSignUp.this);
            builder.setMessage("Error: please make sure every field is filled");
            builder.setCancelable(true);
            AlertDialog alert = builder.create();
            alert.show();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }

}
