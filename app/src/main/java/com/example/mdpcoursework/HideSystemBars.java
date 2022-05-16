package com.example.mdpcoursework;

import android.view.View;

//refer to: https://developer.android.com/training/system-ui/navigation

public interface HideSystemBars {

    default void hidesystembars_oncreate(View view){

        //check whether the system bars are visible
        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if (i==0){ //system bars are visible
                    hidesystembars(view);
                }
            }
        });

    }

    default void hidesystembars(View view){
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
