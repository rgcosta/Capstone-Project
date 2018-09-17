package br.com.sociallinks.sociallinks.utils;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class FirebasePersistance extends Application {

    private static final String LOG_TAG = FirebasePersistance.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.e(LOG_TAG, "PERSISTANCE WAS ENABLED");
    }


}
