package com.example.mdpcoursework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


//purpose: First page. Let user choose to sign in or sign up
public class FirstPage extends AppCompatActivity {

    FirebaseAuth mAuth;
    View screen;
    Button signup_btn, signin_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //to view app in fullscreen, without action bar, navigation bar, etc
        //method implemented from HideSystemBars interface

        //connect to Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        //set up layout to display
        setContentView(R.layout.first_page);

        //connect to layout elements
        signup_btn=findViewById(R.id.signup_btn);
        signin_btn=findViewById(R.id.signin_btn);

        //if sign up button is clicked, go to Sign Up UI
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FirstPage.this, UserSignUp.class));
            }
        });


        //if sign in button is clicked, go to Sign In UI
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FirstPage.this, UserSignIn.class));

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        //check if the user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //if yes, go to Main Page right away
        if (currentUser!=null){
           startActivity(new Intent(FirstPage.this,MainActivity.class));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //continue to keep bars on screen hidden
    }
}
