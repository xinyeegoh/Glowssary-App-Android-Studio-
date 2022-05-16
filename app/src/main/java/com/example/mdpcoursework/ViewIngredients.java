package com.example.mdpcoursework;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

//refer to:
//    search menu:https://developer.android.com/guide/topics/ui/menus#options-menu

//purpose: retrieve skincare ingredients corresponding to search (by text, voice, camera, or a category) from the database
//         then display them in a recycler view
public class ViewIngredients extends AppCompatActivity implements HideSystemBars{

    FirebaseDatabase database;
    DatabaseReference skin_reference;
    Query query;
    View screen;
    SearchViewAdapter searchViewAdapter;

    Toolbar toolbar;
    RecyclerView recyclerView;
    Button back_btn, home_btn;
    FloatingActionButton submit_btn;
    String category_selected, audio_result;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide system bars
        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        //set up layout view
        setContentView(R.layout.ingredient_list_page);

        //get layout elements
        back_btn = findViewById(R.id.back_btn);
        home_btn = findViewById(R.id.home_btn);
        submit_btn = findViewById(R.id.submit_btn);
        recyclerView = findViewById(R.id.ingredient_recview);
        toolbar = findViewById(R.id.toolbar);

        //set layout manager for recycler view (mandatory)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set the custom toolbar to behave like an action bar
        //to enable our SearchView action later
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false); //hide bar title

        //verify the intent source,
        //whether we are displaying ingredients based on
        // a) a category selected by user (IngredientCategory.java)
        // b) a voice input from user (AudioSearchIngredient.java)
        String intent_source = getIntent().getStringExtra("source");//retrieve the "source" info
        category_selected="";
        audio_result="";

        //if intent is passed from IngredientCategory
        if (intent_source.equals("ingredient_category")){
            //retrieve the skincare category users have selected to view
            category_selected = getIntent().getStringExtra("category");
        }
        //if intent is passed from AudioSearchIngredient
        else if (intent_source.equals("ingredient_audio")){
            //retrieve users' voice input
            audio_result = getIntent().getStringExtra("audio_result");
            System.out.println(audio_result);
        }


        //if back button is clicked, back to previous activity
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //if home button is clicked, go to MainActivity page
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ViewIngredients.this,MainActivity.class));
            }
        });

        //if submit button is clicked, go to SubmitIngredientRequest page
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ViewIngredients.this,SubmitIngredientRequest.class));
            }
        });

        //display ingredients in recycler view
        display_skincare_ingredients();


    }


    //method to display ingredients in recycler view
    //      1. set up query
    //      2. retrieve skincare ingredients from database based on query
    //      3. display
    private void display_skincare_ingredients() {

        //connect to the database SKIN section
        database = FirebaseDatabase.getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
        skin_reference = database.getReference().child("SKIN");
        skin_reference.keepSynced(true);//sync skincare ingredient data on database

        //create a query based on the category selected/audio result
        setupQuery();

        //retrieve skincare ingredient data from Firebase database based on the query
        //output type is FirebaseRecyclerOptions because we are displaying ingredients from Firebase in a recycler view
        FirebaseRecyclerOptions<SkincareIngredient> ingredients =
                new FirebaseRecyclerOptions.Builder<SkincareIngredient>()
                        .setQuery(query, SkincareIngredient.class)
                        .build();

        //pass ingredients to searchViewAdapter which is a Firebase Recycler View Adapter
        //the adapter will then bind each resulting ingredient to a view that's held inside a view holder
        //      1 view holder holds 1 view, then the adapter will add the data of 1 ingredient to 1 view.
        //      so, 1 view represents the data of 1 ingredient item
        searchViewAdapter = new SearchViewAdapter(ingredients);
        searchViewAdapter.setContext(ViewIngredients.this); //record the caller of the adapter

        //connect the adapter to the recycler view,
        //so that all views (bound with data just now) can be added to the recycler view
        //----Recycler View----
        //      1.ViewHolder(View(data))
        //      2.ViewHolder(View(data))
        //      3. .....
        //      4. .....
        recyclerView.setAdapter(searchViewAdapter);


    }

    //method to set up QUERY based on skincare categories selected by users / audio input
    private void setupQuery() {

        //retrieving ingredients from a category selected
        if(!category_selected.isEmpty()) {

            //user selects view all
            if(category_selected.equals("All")){
                query = skin_reference;
            }
            //user selects view harmful ingredients
            else if(category_selected.equals("Harmful")){
                query = skin_reference.orderByChild("rating").equalTo("Nasty");
            }
            //user select other categories
            else{
                query = skin_reference.orderByChild("type").equalTo(category_selected);
            }

            Toast.makeText(getApplicationContext(), category_selected, Toast.LENGTH_SHORT).show();

        }
        //retrieving ingredients from voice command
        else if(!audio_result.isEmpty()){
            query = skin_reference.orderByChild("name").equalTo(audio_result);
            Toast.makeText(getApplicationContext(), audio_result, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }

    }

    //method to retrieve and activate the search menu created in res/menu/search.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //activate ("inflate") the search.xml menu
        getMenuInflater().inflate(R.menu.search,menu);

        //connect to the search element on the search.xml menu
        MenuItem search_btn = menu.findItem(R.id.search);

        //retrieve the searching action of the search element
        SearchView searching = (SearchView)search_btn.getActionView();
        searching.setQueryHint("Enter an ingredient name...");
        searching.setPadding(0,0,140,0);

        //detect and process whatever that's searched by users
        search(searching);

        return super.onCreateOptionsMenu(menu);
    }

    //method to detect the searching from users
    private void search(SearchView search_action) {

        //when there is something typed in the search area
        search_action.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                //retrieve data from Firebase database based on text typed in search area
                processSearch(text);
                return false;
            }

            //keep track of any changes in the text typed
            @Override
            public boolean onQueryTextChange(String text) {
                processSearch(text);
                return false;
            }
        });

    }

    //method to process SEARCHED TEXT from users and return a result
    private void processSearch(String text) {

        text=text.toLowerCase();//to make searched text not case-sensitive

        //retrieve data from database based on the searched text
        //add them to Recycler View options
        FirebaseRecyclerOptions<SkincareIngredient> ingredients =
                new FirebaseRecyclerOptions.Builder<SkincareIngredient>()
                        .setQuery(skin_reference.orderByChild("name").startAt(text).endAt(text+"\uf8ff"), SkincareIngredient.class)
                        .build();


        //add them to adapter
        searchViewAdapter = new SearchViewAdapter(ingredients);
        searchViewAdapter.setContext(ViewIngredients.this);
        searchViewAdapter.startListening();//start working
        recyclerView.setAdapter(searchViewAdapter);

    }


    @Override
    protected void onStart() {
        super.onStart();
        searchViewAdapter.startListening();//start working

    }

    @Override
    protected void onStop() {
        super.onStop();
        searchViewAdapter.stopListening();//stop working
    }

    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
        searchViewAdapter.notifyDataSetChanged();
    }
}
