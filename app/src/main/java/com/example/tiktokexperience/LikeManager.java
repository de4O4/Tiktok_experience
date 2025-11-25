package com.example.tiktokexperience;



import android.content.Context;
import android.content.SharedPreferences;

public class LikeManager {
    private static final String PREF_NAME = "like_prefs";
    private SharedPreferences preferences;

    public LikeManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 获取点赞状态
    public boolean isLiked(String postId) {
        return preferences.getBoolean(postId, false);
    }

    // 设置点赞状态
    public void setLiked(String postId, boolean isLiked) {
        preferences.edit().putBoolean(postId, isLiked).apply();
    }
}