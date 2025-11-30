package com.example.tiktokexperience.Optimize;


import android.app.Application;
import android.content.Context;
import android.util.Log;
public class TikTokApplication extends Application {
    private static final String TAG = "TikTokApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        PreloadManager preloadManager = PreloadManager.getInstance(this);
        preloadManager.preloadData(10, data -> {
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}