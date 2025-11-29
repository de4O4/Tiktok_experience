package com.example.tiktokexperience.Optimize;


import android.app.Application;
import android.content.Context;
import android.util.Log;
public class TikTokApplication extends Application {
    private static final String TAG = "TikTokApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TikTokApplication onCreate");

        PreloadManager preloadManager = PreloadManager.getInstance(this);

        preloadManager.preloadData(12, data -> {
            Log.d(TAG, "预加载完成，共加载 " + data.size() + " 条数据");
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}