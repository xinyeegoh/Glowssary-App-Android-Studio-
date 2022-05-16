package com.example.mdpcoursework;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

//refer to: https://guides.codepath.com/android/Working-with-the-Soft-Keyboard

public interface HideSoftKeyboard {

    default void dismissKeyboard(Context context, View view){

        InputMethodManager inputMethodManager =(InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }
}
