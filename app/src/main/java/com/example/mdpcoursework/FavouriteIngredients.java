package com.example.mdpcoursework;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


import java.util.ArrayList;
import java.util.Objects;

//refer to:
//    retrieving data from Firebase: https://firebase.google.com/docs/database/android/lists-of-data#child-events

//purpose: retrieve skincare ingredient information from database
//         based on the list of user's favourite skincare ingredient names
public class FavouriteIngredients extends AppCompatActivity implements HideSystemBars{

    FirebaseDatabase database;
    DatabaseReference skin_reference;

    Query query;
    View screen;

    FavouriteViewAdapter favouriteViewAdapter;
    ArrayList<SkincareIngredient>  fav_ingredient_list;

    RecyclerView recyclerView;
    Button back_btn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        //connect to Firebase database
        database = FirebaseDatabase.getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
        //point to "SKIN" section on the database
        skin_reference = database.getReference().child("SKIN");

        //set up layout to display
        setContentView(R.layout.ingredient_fav_page);

        back_btn = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.fav_ingredient_recview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //go back to previous Activity when back button is clicked
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //retrieve the list of user's favourite skincare ingredient NAMES passed from previous activity (MainActivity)
        ArrayList<String> fav_name_list = getIntent().getStringArrayListExtra("fav name list");

        //create an arraylist to store SkincareIngredient objects later
        fav_ingredient_list = new ArrayList<>();

        //loop through each ingredient name on the name list
        for (String fav_item : fav_name_list) {

            //point to the corresponding skincare ingredient on the database SKIN section
            query = skin_reference.orderByChild("name").equalTo(fav_item);
            //access the ingredient
            query.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    //retrieve the ingredient's information
                    String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String type = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                    String other_names = Objects.requireNonNull(snapshot.child("other_names").getValue()).toString();
                    String rating = Objects.requireNonNull(snapshot.child("rating").getValue()).toString();
                    String featured = Objects.requireNonNull(snapshot.child("featured").getValue()).toString();
                    String usage = Objects.requireNonNull(snapshot.child("usage").getValue()).toString();

                    //create a new SkincareIngredient object based on the information
                    SkincareIngredient item = new SkincareIngredient(name, other_names, type, rating, usage, featured);

                    //add the object to the arraylist created earlier
                    fav_ingredient_list.add(item);

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FavouriteIngredients.this,
                            "Error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });


        }

        //add ArrayList<SkincareIngredient> fav_ingredient_list to favouriteViewAdapter which is a Recycler View adapter
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            favouriteViewAdapter = new FavouriteViewAdapter(fav_ingredient_list);
            favouriteViewAdapter.setContext(FavouriteIngredients.this);
            //set adapter to recycler view, to add the views to the recycler view.
            recyclerView.setAdapter(favouriteViewAdapter);
        }, 200);

    }


    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }


}
