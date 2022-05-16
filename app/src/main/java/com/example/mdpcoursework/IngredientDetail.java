package com.example.mdpcoursework;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

//refer to:
//     add items to list view using array adapter: https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
//     featured product pop-up:https://github.com/orhanobut/dialogplus

//purpose: to display ingredient details when user clicks on the ingredient card
//        user can come to this page from either ViewIngredients or FavouriteIngredients
public class IngredientDetail extends AppCompatActivity implements HideSystemBars{

    FirebaseDatabase db;
    DatabaseReference fav_reference, prod_reference;
    FirebaseAuth mAuth;
    FirebaseUser user;
    View screen;

    Product product;
    SkincareIngredient model;

    ScrollView ingredient_scrollview;
    Button back_btn;
    ToggleButton fav_btn;
    FloatingActionButton home_btn;
    TextView ingredient_name, ingredient_type, ingredient_other_names, ingredient_usage, ingredient_rating;
    ListView ingredient_featured_list;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        setContentView(R.layout.ingredient_detail_page);

        ingredient_scrollview = findViewById(R.id.ingredient_detail_scrowview);
        back_btn = findViewById(R.id.back_btn);
        fav_btn = findViewById(R.id.favourite_btn);
        home_btn = findViewById(R.id.home_btn);
        ingredient_name = findViewById(R.id.ingredient_name);
        ingredient_type = findViewById(R.id.ingredient_type_detail);
        ingredient_other_names = findViewById(R.id.ingredient_other_names_detail);
        ingredient_usage = findViewById(R.id.ingredient_usage_detail);
        ingredient_rating = findViewById(R.id.ingredient_ratings_detail);
        ingredient_featured_list = findViewById(R.id.ingredient_featured_in_listview);

        //identify the ingredient clicked by user
        model = getIntent().getParcelableExtra("ingredient model");


        /////////////check whether the skincare ingredient is the user's favourite///////////

        //  1. connect to the database and go to FAVOURITE section
        db = FirebaseDatabase.getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app");
        fav_reference = db.getReference().child("FAVOURITE");

        //  2. connect to Firebase Auth to get current user and id
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        assert user != null;
        String user_id = user.getUid();

        //  3. point to user's favourite list in the FAVOURITE section,  user is identified by user id
        Query query = fav_reference.child(user_id);

        // Here is how the FAVOURITE section looks like:

        //FAVOURITE:                                        //each user will have its own list of favourites
        //      user_id1:                                   //the skincare ingredient name will be the KEY
        //          skincare_ingredient_1 : true            //if the ingredient is the user's favourite, its value will be TRUE
        //          skincare_ingredient_2 : true            //else, it wont be nested inside the user_id.
        //          skincare_ingredient_3 : true
        //      user_id2:
        //          skincare_ingredient_1 : true
        //          skincare_ingredient_2 : true
        //          skincare_ingredient_3 : true

        //  4.access the favourite list of the user
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if the currently viewing ingredient is on user's favourite list
                if(snapshot.child(model.getName()).exists()){
                    //set favourite button as checked
                    fav_btn.setChecked(true);
                }
                //if the ingredient isn't on the list
                else{
                    //set favourite button as unchecked
                    fav_btn.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(IngredientDetail.this,
                        "Error: "+ error.toString(),
                        Toast.LENGTH_SHORT).show();

            }
        });

        ////////////////////////////////////////////////////////////////////////////////


        //add other data to display in the view
        ingredient_name.setText(model.getName());
        ingredient_type.setText(model.getType());
        ingredient_other_names.setText(model.getOther_names());
        ingredient_usage.setText(model.getUsage());
        ingredient_rating.setText(model.getRating());


        /////////////////////////display featured products ///////////////////////////

        //on the database, the "featured" of the ingredient is recorded in the form of a string
            //-> "p1,p2,p3,..."

        // 1. split the products into a LIST of products by comma.
            // "p1,p2,..." -> ["p1","p2",...]
        String[] featured_stringlist = model.getFeatured().split(",");

        //2. create an ArrayList to store each product (names only)
        ArrayList<String> featured_arraylist = new ArrayList<>();
        Collections.addAll(featured_arraylist, featured_stringlist);

        //3. add featured products on the Arraylist to the array adapter
        ArrayAdapter<String> featured_adapter = new ArrayAdapter<>(IngredientDetail.this, R.layout.single_product_view, featured_arraylist);

        //4. set array adapter to list view, to display the featured products in the list view
        ingredient_featured_list.setAdapter(featured_adapter);

        //////////////////////////////////////////////////////////


        //////////display product description when product is clicked ////////////

        //1. point to PRODUCT section on database
        prod_reference = db.getReference().child("PRODUCT");
        prod_reference.keepSynced(true); //keep product info synced

        //when any featured product on the list is clicked,
        ingredient_featured_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //2. check what product is clicked by the position
                String product_name = (String) adapterView.getItemAtPosition(i);

                //3. check whether product info is available on the database PRODUCT section
                prod_reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // if yes
                        if(snapshot.child(product_name).exists()) {

                            //retrieve product info
                            String name = snapshot.child(product_name).getKey();
                            String description = Objects.requireNonNull(snapshot.child(product_name).getValue()).toString();

                            //create a Product object based on name and description
                            product = new Product(name, description);

                            //display product info
                            display_product_description();
                        }

                        //if no
                        else{
                            //inform user
                            AlertDialog.Builder builder = new AlertDialog.Builder(IngredientDetail.this);
                            builder.setMessage("Sorry product info isn't available on Glowssary yet, we will update soon!");
                            builder.setCancelable(true);
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(IngredientDetail.this,"Error"+error.toString(),Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        //////////////////////////////////////////////////////////////////////


        //as we nest a scrollable list view inside a scroll view for our ingredient detail page
        //this is to make sure that both views can be scrolled

        //when general scrollview is touched / focussed, other area is still touchable for scrolling
        ingredient_scrollview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        //when list view is touched / focussed, other parts can't be scrolled
        ingredient_featured_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        //if back button is clicked, back to previous activity (either View Ingredients OR Favourite Ingredients)
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //if home button is clicked, go to main page
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IngredientDetail.this,MainActivity.class));
            }
        });


        //if favourite button is clicked and it is a checked,
        //  save the ingredient item onto user's favourite list on the database FAVOURITE section
        //if it is an unchecked,
        //  remove the ingredient from user's favourite list
        fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fav_btn.isChecked()){
                    Toast.makeText(IngredientDetail.this,"Favourited",Toast.LENGTH_SHORT).show();
                    //update item to database
                    fav_reference.child(user_id).child(model.getName()).setValue("true");
                }
                else {
                    Toast.makeText(IngredientDetail.this,"Un-favourited",Toast.LENGTH_SHORT).show();
                    //remove item from database
                    fav_reference.child(user_id).child(model.getName()).removeValue();
                }
            }
        });

    }


    //method to display product description
    private void display_product_description() {

        //create a dialog view holder for "product detail" view
        DialogPlus product_detail_viewholder = DialogPlus.newDialog(IngredientDetail.this)
                .setContentHolder(new ViewHolder(R.layout.product_detail_page))
                .setExpanded(true,1200)
                .setGravity(Gravity.BOTTOM)
                .create();

        //retrieve the view from the view holder
        View product_detail_view = product_detail_viewholder.getHolderView();

        //get view elements
        Button dialog_back_btn = product_detail_view.findViewById(R.id.back_btn);
        TextView product_name = product_detail_view.findViewById(R.id.product_name);
        TextView product_description = product_detail_view.findViewById(R.id.product_desc_detail);

        //bind product data to view
        product_name.setText(product.getName());
        product_description.setText(product.getDescription());

        //display view
        product_detail_viewholder.show();

        //if back button is clicked, dismiss view
        dialog_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                product_detail_viewholder.dismiss();
            }
        });

    }

    @Override
    protected void onStart() { super.onStart(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }

}
