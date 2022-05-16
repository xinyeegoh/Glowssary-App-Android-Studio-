package com.example.mdpcoursework;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//purpose: for users to submit any ingredient not found on the app
public class SubmitIngredientRequest extends AppCompatActivity implements HideSystemBars, HideSoftKeyboard{

     View screen;
     EditText ingredient_request;
     Button back_btn, submit_btn;
     FloatingActionButton home_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        setContentView(R.layout.submit_ingredient_page);

        ingredient_request = findViewById(R.id.ingredient_request_edittxt);
        back_btn = findViewById(R.id.back_btn);
        submit_btn = findViewById(R.id.submit_btn);
        home_btn = findViewById(R.id.home_btn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SubmitIngredientRequest.this,MainActivity.class));
            }
        });


        //if submit button is clicked, add request to database
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if the ingredient request input is filled
                if (!TextUtils.isEmpty(ingredient_request.getText().toString())){
                    //get the ingredient request from user
                    String name = ingredient_request.getText().toString();
                    name=name.toLowerCase();
                    //connect to the database REQUEST section
                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
                    DatabaseReference req_reference = database.getReference("REQUEST");
                    //add request to the section
                    req_reference.child(name).setValue("1");
                    Toast.makeText(getApplicationContext(),"Added Successfully",Toast.LENGTH_SHORT).show();
                }
                //if no request info is entered by user but "submit" button is clicked
                else{
                    //alert user
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubmitIngredientRequest.this);
                    builder.setMessage("Error: request is empty");
                    builder.setCancelable(true);
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        });

        //dismiss keyboard
        ingredient_request.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean typing) {
                if (!typing){
                    dismissKeyboard(SubmitIngredientRequest.this,view);
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
