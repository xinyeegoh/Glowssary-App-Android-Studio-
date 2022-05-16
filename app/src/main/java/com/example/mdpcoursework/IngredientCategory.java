package com.example.mdpcoursework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

//purpose: display skincare ingredient categories to users
//         to ease their searching
public class IngredientCategory extends AppCompatActivity implements HideSystemBars{

    View screen;
    Intent intent;

    ImageButton
            viewall_btn, exfoliant_btn, antiacne_btn,
            humectant_btn, emollient_btn, soothing_btn,
            antiaging_btn, brightening_btn, antioxidant_btn,
            surfactant_btn, absorbent_btn, harmful_btn;
    FloatingActionButton submit_btn;
    Button back_btn, search_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        //set layout to display
        setContentView(R.layout.ingredient_category_page);

        //get layout elements
        back_btn = findViewById(R.id.back_btn);
        search_btn = findViewById(R.id.search_btn);
        submit_btn = findViewById(R.id.submit_btn);

        //categories
        viewall_btn = findViewById(R.id.viewall_btn);
        exfoliant_btn = findViewById(R.id.exfoliant_btn);
        antiacne_btn = findViewById(R.id.antiacne_btn);
        humectant_btn = findViewById(R.id.humectant_btn);
        emollient_btn = findViewById(R.id.emollient_btn);
        soothing_btn = findViewById(R.id.soothing_btn);
        antiaging_btn = findViewById(R.id.antiaging_btn);
        brightening_btn = findViewById(R.id.brightening_btn);
        antioxidant_btn = findViewById(R.id.antioxidant_btn);
        surfactant_btn = findViewById(R.id.surfactant_btn);
        absorbent_btn = findViewById(R.id.absorbent_btn);
        harmful_btn = findViewById(R.id.harmful_btn);

        //set up an intent: once a category is selected, go to ViewIngredients page of the corresponding category
        intent = new Intent(IngredientCategory.this, ViewIngredients.class);
        //notify ViewIngredients that the intent is coming from IngredientCategory
        intent.putExtra("source","ingredient_category");
        //check what category is selected
        listen_category_selected();

        //if back button is clicked, go back
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //if the search icon on the app bar is clicked, go to View Ingredients page right away
        // by default, display all skincare ingredients to users
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","All"); //view all
                startActivity(intent);
            }
        });

        //if submit button is clicked, go to Submit Ingredient Request page
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IngredientCategory.this,SubmitIngredientRequest.class));
            }
        });

    }


    //listen to what category is selected by user
    //pass different category name via Intent to View Ingredients page
    //so that we can display the ingredients of the selected category
    private void listen_category_selected() {

        viewall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","All");
                startActivity(intent);
            }
        });
        exfoliant_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Exfoliant");
                startActivity(intent);

            }
        });
        antiacne_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Anti-Acne");
                startActivity(intent);

            }
        });
        humectant_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Humectant");
                startActivity(intent);

            }
        });
        emollient_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Emollient");
                startActivity(intent);

            }
        });
        soothing_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Soothing");
                startActivity(intent);

            }
        });
        antiaging_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Anti-Aging");
                startActivity(intent);

            }
        });
        brightening_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Skin-Brightening");
                startActivity(intent);

            }
        });
        antioxidant_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Antioxidant");
                startActivity(intent);

            }
        });
        surfactant_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Surfactant");
                startActivity(intent);

            }
        });
        absorbent_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Absorbent");
                startActivity(intent);

            }
        });
        harmful_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("category","Harmful");
                startActivity(intent);

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }
}
