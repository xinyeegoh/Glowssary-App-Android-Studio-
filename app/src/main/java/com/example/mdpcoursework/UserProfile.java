package com.example.mdpcoursework;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

//refer to:
//    access Firebase: https://firebase.google.com/docs/database/android/read-and-write

//purpose: for users to view and update his/her profile
public class UserProfile extends AppCompatActivity implements HideSystemBars, HideSoftKeyboard{

    FirebaseAuth mAuth;
    FirebaseUser user;
    User current_user;
    String user_id;
    View screen;

    TextView username_view, email_view, change_pw;
    EditText username_edit, age_edit;
    RadioGroup gender_radio_group;
    RadioButton male,female,others;
    ImageButton save_btn;
    Button back_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        //retrieve the User object passed from previous Activity (MainActivity)
        //the User object is the current signed-in user
        current_user = getIntent().getParcelableExtra("user class");

        //connect to Firebase Auth
        //this is so that we can update user's info to database later
        mAuth = FirebaseAuth.getInstance();

        //get current user and user id
        user = mAuth.getCurrentUser();
        assert user != null;
        user_id = user.getUid();

        setContentView(R.layout.user_profile);

        save_btn = findViewById(R.id.save_btn);
        back_btn = findViewById(R.id.back_btn);
        username_view = findViewById(R.id.username);
        email_view = findViewById(R.id.email);
        change_pw = findViewById(R.id.change_password);
        username_edit = findViewById(R.id.username_edit);
        age_edit = findViewById(R.id.age_edit);
        gender_radio_group = findViewById(R.id.gender_radio_group);
            male = findViewById(R.id.male);
            female = findViewById(R.id.female);
            others = findViewById(R.id.others);

        //display user's username, age, email, and gender
        username_view.setText(current_user.getUsername());
        username_edit.setText(current_user.getUsername());
        email_view.setText(current_user.getEmail());
        age_edit.setText(current_user.getAge());
        String gender = current_user.getGender();
        switch (gender) {
            case "male":                            //radio button will automatically unselect other radio buttons
                male.setChecked(true);              //in the same radio group
                break;
            case "female":
                female.setChecked(true);
                break;
            case "others":
                others.setChecked(true);
                break;
        }

        //if save button is clicked, update user's profile, then back to Main Page
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_profile();
                Toast.makeText(UserProfile.this,"Profile updated",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //if back button is clicked, go back to previous Activity
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //if change password is clicked, show pop-up for user to enter new password
        change_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change_pw_popup();
            }
        });

        //dismiss keyboard
        username_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                //if user no longer typing in the edit text area, dismiss soft keyboard
                if (!typing){
                    dismissKeyboard(UserProfile.this,view);
                }
            }
        });

        //dismiss keyboard
        age_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                //if user no longer typing in the edit text area, dismiss soft keyboard
                if (!typing){
                    dismissKeyboard(UserProfile.this,view);
                }
            }
        });

    }


    //method to update user's info on database and on app (via User object)
    //user can change username, age, and gender
    //email cannot be changed
    private void update_profile() {

        //get user's latest inputs on username and age
        String latest_username = username_edit.getText().toString();
        String latest_age = age_edit.getText().toString();

        //update username and age of User object
        current_user.setUsername(latest_username);
        current_user.setAge(latest_age);

        //get user's latest gender input and update User object
        String latest_gender;
        int checked_rb_id = gender_radio_group.getCheckedRadioButtonId();
        switch (checked_rb_id){
            case R.id.male:
                current_user.setGender("male");
                latest_gender="male";
                break;
            case R.id.female:
                current_user.setGender("female");
                latest_gender="female";
                break;
            case R.id.others:
                current_user.setGender("others");
                latest_gender="others";
                break;
            default: //if none selected, by default gender will be "undefined"
                current_user.setGender("undefined");
                latest_gender="undefined";
                break;
        }

        //connect to database USER section and go to user position using user id
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = db.getReference("USER").child(user_id);//point to user

        //record user latest info to database
        reference.child("username").setValue(latest_username);
        reference.child("age").setValue(latest_age);
        reference.child("gender").setValue(latest_gender);

    }

    //method to display the pop up for changing password
    private void change_pw_popup() {

        DialogPlus change_pw_viewholder = DialogPlus.newDialog(UserProfile.this)
                .setContentHolder(new ViewHolder(R.layout.change_password_popup))
                .setContentHeight(530)
                .setContentWidth(900)
                .setGravity(Gravity.CENTER)
                .create();

        View change_pw_view = change_pw_viewholder.getHolderView();

        EditText change_pw_edit = change_pw_view.findViewById(R.id.change_pw_edit);
        Button submit_pw_btn = change_pw_view.findViewById(R.id.submit_pw_btn);

        change_pw_viewholder.show();

        //dismiss keyboard
        change_pw_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                //if user no longer typing in the edit text area, dismiss soft keyboard
                if (!typing){
                    dismissKeyboard(UserProfile.this,view);
                }
            }
        });

        //if submit button is click, update user's account password
        submit_pw_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if a password is entered by user
                if(!TextUtils.isEmpty(change_pw_edit.getText().toString())){
                    //retrieve the new password
                    String new_pw = change_pw_edit.getText().toString();
                    //update user's password on Firebase auth
                    user.updatePassword(new_pw).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //password is updated successfully
                            if(task.isSuccessful()){
                                Toast.makeText(UserProfile.this,"Password changed",Toast.LENGTH_SHORT).show();
                                change_pw_viewholder.dismiss();
                            }
                        }
                    });

                }
                //if a password is not entered but user clicks "submit", display error message
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
                    builder.setMessage("Please enter a password");
                    builder.setCancelable(true);
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }
}
