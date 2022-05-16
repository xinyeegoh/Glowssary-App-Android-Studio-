package com.example.mdpcoursework;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

//refer to:
//    reset password via link to email: https://firebase.google.com/docs/auth/android/manage-users

//purpose: to allow users to reset their passwords
public class ForgotPassword extends AppCompatActivity implements HideSystemBars, HideSoftKeyboard{

    FirebaseAuth mAuth;
    View screen;

    Button send_link_btn,back_btn;
    EditText email_edit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        //connect to Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //set up layout
        setContentView(R.layout.reset_password_page);

        //get the layout elements
        email_edit = findViewById(R.id.email_edit);
        back_btn = findViewById(R.id.back_btn);
        send_link_btn = findViewById(R.id.send_reset_password_link);

        //if back button is clicked, go back to previous activity (UserSignIn page)
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //if send button is clicked,
        //system will send a password reset link to the email entered by users
        send_link_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset_password();
            }
        });

        //dismiss soft keyboard
        email_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                if (!typing){
                    dismissKeyboard(ForgotPassword.this,view);
                }
            }
        });
    }


    //method to reset password via email
    private void reset_password() {

        //if an email is entered by the user
        if(!TextUtils.isEmpty(email_edit.getText().toString())){

            //get the email input by user
            String email = email_edit.getText().toString();

            //send password reset link to the email
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    //if link is successfully sent, direct user back to Sign In page
                    if(task.isSuccessful()){
                        Toast.makeText(ForgotPassword.this,"Reset Password Link sent.",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ForgotPassword.this,UserSignIn.class));
                    }
                    //if error, display error message
                    else{
                        Toast.makeText(ForgotPassword.this,"Error: " + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        //email is not entered, alert user
        else{

            AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
            builder.setMessage("Error: please fill in an email");
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
