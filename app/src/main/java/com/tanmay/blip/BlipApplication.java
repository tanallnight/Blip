package com.tanmay.blip;

import android.app.Application;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.tanmay.blip.database.SharedPrefs;

public class BlipApplication extends Application {

    private static BlipApplication mInstance;

    public OkHttpClient client = new OkHttpClient();

    public static synchronized BlipApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        int cacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(getCacheDir(), cacheSize);
        client.setCache(cache);

        SharedPrefs.create(this);
    }
}
