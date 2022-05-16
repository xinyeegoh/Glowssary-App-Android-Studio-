package com.example.mdpcoursework;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

//refer to:
//    access Firebase: https://firebase.google.com/docs/database/android/read-and-write

//purpose: Main page where users can access different app features, and also view their profile
public class MainActivity extends AppCompatActivity implements HideSystemBars {

    FirebaseDatabase database;
    DatabaseReference user_reference, fav_reference;
    FirebaseAuth mAuth;
    FirebaseUser current_user;
    String user_id;
    Query user_info_query, user_fav_query;
    ArrayList<String> fav_name_list;
    User user;
    View screen;

    Button search_btn, camera_btn, mic_btn, fav_btn, help_btn;
    ImageButton signout_btn, edit_profile_btn;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        setContentView(R.layout.main_page);

        edit_profile_btn = findViewById(R.id.editprofile_btn); //update profile
        username = findViewById(R.id.username_view); //username display
        signout_btn = findViewById(R.id.signout_btn); //logout

        search_btn = findViewById(R.id.search_btn); //search by text
        mic_btn = findViewById(R.id.audio_btn); //search by voice
        camera_btn = findViewById(R.id.camera_btn);//search by image
        fav_btn = findViewById(R.id.fav_btn);//favourite section
        help_btn = findViewById(R.id.help_btn);//help centre


        //if edit profile button is clicked, go to UserProfile page
        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,UserProfile.class);
                intent.putExtra("user class",user); //bring along the User object
                startActivity(intent);
            }
        });

        //if sign out button is clicked, sign user out and back to FirstPage
        signout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut(); //sign out
                Toast.makeText(MainActivity.this,"Sign out",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,FirstPage.class));
            }
        });

        //if search button is clicked, display ingredient categories to users
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,IngredientCategory.class));
            }
        });

        //if camera button is clicked, direct users to CameraSearchIngredient page
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,CameraSearchIngredient.class));
            }
        });

        //if mic button is clicked, direct users to AudioSearchIngredient page
        mic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AudioSearchIngredient.class));
            }
        });

        //if favourite button is clicked, display to users their bookmarked ingredients
        fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,FavouriteIngredients.class);
                //bring along a list of skincare ingredient NAMES bookmarked by users
                intent.putExtra("fav name list",fav_name_list);
                startActivity(intent);
            }
        });

        //if help button is clicked, direct user to Help Desk page
        help_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,HelpDesk.class));
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        //user panel -> retrieve username, email, gender, age, etc
        setup_user_panel();

        //retrieve user's favourite skincare ingredients
        retrieve_user_fav();

    }

    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }


    //method to set up user's user panel
    //      we retrieve current signed-in user's information from the database
    //      then use the information to set up user's user panel on the app
    private void setup_user_panel() {

        //connect to Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //get current user and id
        current_user = mAuth.getCurrentUser();
        assert current_user != null;
        user_id = current_user.getUid();

        //connect to Firebase database
        database = FirebaseDatabase.getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
        //point to USER section on database
        user_reference = database.getReference().child("USER");

        //retrieve user's info from database using user's id
        user_info_query = user_reference.child(user_id);
        user_info_query.keepSynced(true); //keep user's data synced
        user_info_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //retrieve user's information
                String user_name = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                String age = Objects.requireNonNull(snapshot.child("age").getValue()).toString();
                String gender = Objects.requireNonNull(snapshot.child("gender").getValue()).toString();
                String user_id = Objects.requireNonNull(snapshot.child("user_id").getValue()).toString();
                String email = Objects.requireNonNull(snapshot.child("email").getValue()).toString();

                //create a new USER object based on the info retrieved
                user = new User(user_id,user_name,age,gender,email);

                //display username on screen
                username.setText(user_name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Failed to retrieve user info: "+ error.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });


    }


    //method to retrieve user's favourite skincare ingredients
    private void retrieve_user_fav() {

        //point to FAVOURITE section on database
        fav_reference = database.getReference().child("FAVOURITE");

        //the FAVOURITE section on database is structured as below:

        //FAVOURITE:                                        //each user will have its own list of favourites
        //      user_id1:                                   //the skincare ingredient name will be the KEY
        //          skincare_ingredient_1 : true            //if the ingredient is the user's favourite, its value will be TRUE
        //          skincare_ingredient_2 : true            //else, it wont be nested inside the user_id.
        //          skincare_ingredient_3 : true
        //      user_id2:
        //          skincare_ingredient_1 : true
        //          skincare_ingredient_2 : true
        //          skincare_ingredient_3 : true


        //create an empty arraylist<String> to store a list of skincare ingredient names
        fav_name_list = new ArrayList<>();
        //point to user's position in FAVOURITE section, using user id
        user_fav_query = fav_reference.child(user_id);
        user_fav_query.keepSynced(true);//sync user's favourite ingredient list
        user_fav_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                    //get the keys of user's children, which will be the bookmarked ingredient names
                    String fav_name = childSnapshot.getKey();
                    //add each name to the arraylist created
                    fav_name_list.add(fav_name);        //this fav_name_list will be passed to FavouriteIngredients activity
                }                                       //when the fav button on this Activity is clicked

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Failed to retrieve user favourites: "+ error.toString(),
                        Toast.LENGTH_SHORT).show();

            }
        });

    }


}