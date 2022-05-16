package com.example.mdpcoursework;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

//refer to:
//    for Firebase to work offline: https://firebase.google.com/docs/database/android/offline-capabilities
public class OfflineCapability extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // offline capability for Firebase
        //persist data to disk (local cache memory)
        //when offline, use cached data until online again
        FirebaseDatabase.getInstance("https://mdp-coursework-default-rtdb.asia-southeast1.firebasedatabase.app").setPersistenceEnabled(true);

    }
}
