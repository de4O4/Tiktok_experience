package com.example.tiktokexperience.Bean;


import java.util.List;
import java.util.Set;
import android.content.Context;
import android.content.SharedPreferences;
import com.example.tiktokexperience.User.UserManager;

public class LikeManager {
    private static final String PREF_NAME = "like_prefs";
    private SharedPreferences preferences;
    private UserManager userManager;


    public LikeManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        userManager = UserManager.getInstance(context);

    }

    // 获取点赞状态
    public boolean isLiked(String postId) {

        // 如果用户已登录，优先从UserManager获取点赞状态
        if (userManager.isLoggedIn()) {
            List<String> likedPosts = userManager.getLikedPosts();
            return likedPosts.contains(postId);
        } else {
            // 如果用户未登录，从本地SharedPreferences获取
            return preferences.getBoolean(postId, false);
        }
    }

    // 设置点赞状态
    public void setLiked(String postId, boolean isLiked) {
        if (userManager.isLoggedIn()) {
            // 如果用户已登录，保存到UserManager

            List<String> likedPosts = userManager.getLikedPosts();
            if (isLiked) {
                if (!likedPosts.contains(postId)) {
                    likedPosts.add(postId);
                }
            } else {
                likedPosts.remove(postId);
            }
            userManager.saveLikedPosts(likedPosts);

        } else {
            // 如果用户未登录，保存到本地SharedPreferences
            preferences.edit().putBoolean(postId, isLiked).apply();
        }
    }
    public void syncLocalToUser() {
        if (userManager.isLoggedIn()) {
            // 将本地的点赞数据合并到用户数据中
            List<String> userLikedPosts = userManager.getLikedPosts();
            Set<String> allKeys = preferences.getAll().keySet();

            for (String key : allKeys) {
                Boolean isLiked = preferences.getBoolean(key, false);
                if (isLiked && !userLikedPosts.contains(key)) {
                    userLikedPosts.add(key);
                }
            }

            // 清除本地点赞数据，因为现在由用户账户管理
            preferences.edit().clear().apply();

            // 保存合并后的点赞数据
            userManager.saveLikedPosts(userLikedPosts);
        }
    }
}