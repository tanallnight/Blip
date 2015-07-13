package com.tanmay.blip.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefs {

    private static SharedPrefs mInstance;

    private SharedPreferences sharedPreferences;

    public static void create(Context context) {
        mInstance = new SharedPrefs(context);
    }

    public static SharedPrefs getInstance() {
        return mInstance;
    }

    private SharedPrefs(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setFirstRun(boolean firstRun) {
        sharedPreferences.edit().putBoolean("FIRST_RUN", firstRun).apply();
    }

    public boolean getFirstRun() {
        return sharedPreferences.getBoolean("FIRST_RUN", true);
    }

}
