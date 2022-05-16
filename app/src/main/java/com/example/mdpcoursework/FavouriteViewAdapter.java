package com.example.mdpcoursework;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


//refer to:
//    RecyclerView Adapter: https://developer.android.com/guide/topics/ui/layout/recyclerview

//purpose: to display the data of user's favourite skincare ingredients in the Recycler View
public class FavouriteViewAdapter extends RecyclerView.Adapter<FavouriteViewAdapter.viewHolder>{

    private ArrayList<SkincareIngredient> fav_ingredient_list;
    private Context context;

    //take a list of SkincareIngredient objects as input
    public FavouriteViewAdapter(ArrayList<SkincareIngredient> models) {
        fav_ingredient_list = models;
    }

    public void setContext(Context context){
        this.context=context;
    }

    //create view holder to hold the view (card view) for each skincare ingredient
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //retrieve and activate ("inflate") the card view layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_ingredient_cardview, parent, false);
        //pass the card view to the view holder
        return new viewHolder(view);

    }

    //declaration of viewHolder class
    class viewHolder extends RecyclerView.ViewHolder{

        TextView ingredientname;
        Button expandbutton;

        public viewHolder(View itemView){
            super(itemView);
            ingredientname = itemView.findViewById(R.id.ingredient_name);
            expandbutton = itemView.findViewById(R.id.expand_btn);

        }
    }

    //after the view holder — which holds the card view — is created
    //populate data into view via view holder
    @Override
    public void onBindViewHolder(@NonNull FavouriteViewAdapter.viewHolder holder, int position) {

        //retrieve the items on the ArrayList<SkincareIngredient> fav_ingredient_list, by their position/index
        SkincareIngredient fav_ingredient_item = fav_ingredient_list.get(position);

        //add the item name to display
        holder.ingredientname.setText(fav_ingredient_item.getName());

        //if expand button is clicked, display the ingredient details
        holder.expandbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display_ingredient_detail(fav_ingredient_item);
            }
        });

        //if ingredient card view is clicked, display the ingredient details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display_ingredient_detail(fav_ingredient_item);
            }
        });


    }

    //method to show ingredient details
    private void display_ingredient_detail(SkincareIngredient fav_ingredient_item) {

        Intent intent = new Intent(context,IngredientDetail.class); //go to IngredientDetail page
        //bring along the ingredient item so that we know what ingredient to display the details
        intent.putExtra("ingredient model",fav_ingredient_item);
        context.startActivity(intent);

    }

    @Override
    public int getItemCount() {
        return fav_ingredient_list.size();//total number of fav ingredients
    }

}
