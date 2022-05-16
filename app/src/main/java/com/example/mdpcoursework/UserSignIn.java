package com.example.mdpcoursework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
//    sign in using password & email: https://firebase.google.com/docs/auth/android/password-auth
//    sign in using Google account: https://firebase.google.com/docs/auth/android/google-signin

//purpose: let users sign in to existing accounts
public class UserSignIn extends AppCompatActivity implements HideSystemBars, HideSoftKeyboard{

    FirebaseAuth mAuth;
    View screen;

    //Google sign in
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> google_signinLauncher;

    Button signin_btn, back_btn;
    ImageButton google_signin;
    TextView forgot_password;
    EditText email_edit,password_edit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide system bars
        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        //connect to Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //set up layout view
        setContentView(R.layout.user_signin_page);

        //get view elements
        back_btn = findViewById(R.id.back_btn);
        signin_btn = findViewById(R.id.signin_btn);
        google_signin = findViewById(R.id.google_signin_btn);
        forgot_password = findViewById(R.id.forgot_password);
        email_edit = findViewById(R.id.email_edit);
        password_edit = findViewById(R.id.password_edit);

        //set up Google sign in materials
        setup_google_signin();

        //if back button is clicked, go back to previous activity
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //if sign in button is clicked, verify user's email and password for sign in
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emaill_password_signin();
            }
        });

        //if google sign in button is clicked, sign user in using Google account
        google_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                google_signin();
            }
        });

        //if forgot password is clicked, go to ForgotPassword page
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserSignIn.this,ForgotPassword.class));
            }
        });


        //dismiss keyboard
        email_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) { //typing = has focus
                //if user no longer typing in the edit text area, dismiss soft keyboard
                if (!typing){ //not focussing on the edit area
                    dismissKeyboard(UserSignIn.this,view);
                }
            }
        });

        //dismiss keyboard
        password_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                //if user no longer typing in the edit text area, dismiss soft keyboard
                if (!typing){
                    dismissKeyboard(UserSignIn.this,view);
                }
            }
        });


    }


    //method to set up Google sign in materials
    private void setup_google_signin() {

        //configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //set up launcher for Google Sign In Intent
        //it will process Google sign in intent when user wants to sign in using Google
        google_signinLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        //if intent is successfully launched
                        if(result.getResultCode() == Activity.RESULT_OK){
                            //retrieve the Intent data
                            Intent data = result.getData();
                            //try signing in to user's Google account
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                //Google Sign In successful
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                //authenticate with Firebase (sign user in to the app using the Google account)
                                firebaseAuthWithGoogle(account.getIdToken());

                            } catch (ApiException e) {
                                // Google Sign In failed, toast message
                                Toast.makeText(UserSignIn.this, "Error",Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

    }


    //method to sign user in with Google
    private void google_signin() {

        //create Google sign in intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //launch the intent
        google_signinLauncher.launch(signInIntent);

    }


    //method to authenticate user's Google account with Firebase
    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Sign in authentication is successful
                        if (task.isSuccessful()) {

                            Toast.makeText(UserSignIn.this, "signInWithCredential:success",Toast.LENGTH_SHORT).show();

                            //as user can sign-in using Google account without signing up first
                            //we need to check whether user has previously signed up/in using the same Google account
                            //             and whether user's account info is already in the database
                            //if yes, move on; if no, add user's account info to database;

                            //connect to Firebase Auth and get user
                            FirebaseUser user = mAuth.getCurrentUser();
                            //get user's id
                            assert user != null;
                            String user_id = user.getUid();
                            //connect to Firebase database, point to USER section
                            FirebaseDatabase db = FirebaseDatabase
                                    .getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
                            DatabaseReference reference = db.getReference("USER");

                            //check if user's info is ald on database - USER section
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    //if user doesn't exist in USER section
                                    if (!snapshot.child(user_id).exists()){
                                        //add user to USER section on the database
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

                                }
                            });

                            //after that, go to main page
                            startActivity(new Intent(UserSignIn.this,MainActivity.class));

                        }
                        //sign in fails, display a message to the user.
                        else {
                            Toast.makeText(UserSignIn.this, "signInWithCredential:failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //method to verify user's EMAIL AND PASSWORD for signing in
    private void emaill_password_signin() {

        //if all fields are filled
        if(!TextUtils.isEmpty(email_edit.getText().toString()) && !TextUtils.isEmpty(password_edit.getText().toString())){

            //get user's entered email and password
            String email = email_edit.getText().toString();
            String password = password_edit.getText().toString();

            //sign user in
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //if user is successfully signed in, go to Main Page
                    if(task.isSuccessful()){
                        Toast.makeText(UserSignIn.this,"Welcome back!",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserSignIn.this,MainActivity.class));
                    }
                    //else, display error message
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserSignIn.this);
                        builder.setMessage("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        builder.setCancelable(true);
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                }
            });
        }

        //any of the fields are not filled, display error message to user
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(UserSignIn.this);
            builder.setMessage("Error: Please make sure every field is filled");
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
