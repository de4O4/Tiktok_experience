package com.example.tiktokexperience.Bean;


import java.util.List;
import com.example.tiktokexperience.Data.PostDatabaseHelper;
import android.content.Context;
import android.content.SharedPreferences;
import com.example.tiktokexperience.User.UserManager;

public class LikeManager {
    private static final String PREF_NAME = "like_prefs";
    private SharedPreferences preferences;
    private UserManager userManager;
    private PostDatabaseHelper postDatabaseHelper;

    public LikeManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        userManager = UserManager.getInstance(context);
        postDatabaseHelper = new PostDatabaseHelper(context);

    }


    public boolean isLiked(String postId) {


        if (userManager.isLoggedIn()) {
            List<String> likedPosts = userManager.getLikedPosts();  // 如果用户已登录，优先从UserManager获取点赞状态
            return likedPosts.contains(postId);
        } else {

            return preferences.getBoolean(postId, false);  // 如果用户未登录，从本地SharedPreferences获取
        }
    }


    public void setLikedWithPostInfo(String postId, boolean isLiked, PostItem postItem) {
        // 保存帖子信息到数据库
        postDatabaseHelper.savePostIfNotExists(postItem);

        if (userManager.isLoggedIn()) {
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

            preferences.edit().putBoolean(postId, isLiked).apply();// 如果用户未登录，保存到本地SharedPreferences
        }
    }

}