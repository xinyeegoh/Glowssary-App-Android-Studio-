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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

//refer to:
//    Firebase RecyclerView Adapter: https://firebaseopensource.com/projects/firebase/firebaseui-android/database/readme/

//purpose: to use the Firebase Recycler View Adapter for adding ingredient data to recycler view
public class SearchViewAdapter extends FirebaseRecyclerAdapter<SkincareIngredient, SearchViewAdapter.viewHolder> {
    
    private Context context;

    // Adapters provide a binding from data set to views, to be displayed within a Recycler View later
    //      1. retrieve relative data from the database
    //      2. create a view holder that holds a view
    //         - the number of holders created is based on the number of options passed to the adapter, see line 37
    //      3. bind the data to the view via the view holder


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public SearchViewAdapter(@NonNull FirebaseRecyclerOptions<SkincareIngredient> options) {
        super(options);
    }

    public void setContext(Context context){
        this.context=context;
    }


    //create view holder to hold the view (card view) for each skincare ingredient
    //view holder -> holds a view that will store 1 ingredient item
    //          for the Glowssary app, 1 skincare ingredient item will be displayed in 1 card view
    //          so 1 view holder will be the container for 1 card view
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //retrieve and activate ("inflate") the card view layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_ingredient_cardview, parent, false);

        //add the card view to the view holder
        return new viewHolder(view);

    }

    //declaration of viewHolder class
    class viewHolder extends RecyclerView.ViewHolder{

        TextView ingredientname;
        Button expandbutton;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            //get the elements on the view (card view)
            ingredientname = itemView.findViewById(R.id.ingredient_name);
            expandbutton = itemView.findViewById(R.id.expand_btn);
        }

    }

    //after the view holder — which holds the card view — is created,
    //we bind the ingredient data to the view via the view holder
    //model = ingredient item
    @Override
    protected void onBindViewHolder(@NonNull viewHolder holder, int position, @NonNull SkincareIngredient model) {

        //add ingredient name to the card view for display
        holder.ingredientname.setText(model.getName());

        //if the expand button on the card view is clicked, display ingredient details
        holder.expandbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display_ingredient_detail(model,holder);
            }
        });

        //if the card view is clicked, display ingredient details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display_ingredient_detail(model,holder);
            }
        });
    }

    //method to go to ingredient details
    private void display_ingredient_detail(SkincareIngredient model, viewHolder holder) {

        //go to IngredientDetail page
        Intent intent = new Intent(context,IngredientDetail.class);
        //bring along the ingredient item so that we know what ingredient to display the details
        intent.putExtra("ingredient model",model);
        context.startActivity(intent);

    }



}




