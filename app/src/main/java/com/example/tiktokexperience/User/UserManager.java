package com.example.tiktokexperience.User;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tiktokexperience.Data.UserDatabaseHelper;

import java.util.ArrayList;

import java.util.List;

public class UserManager {
    private static UserManager instance;
    private User currentUser;
    private SharedPreferences sharedPreferences;
    private UserDatabaseHelper databaseHelper;
    private static final String PREF_NAME = "UserPrefs";
    private static final String LIKED_POSTS_KEY = "LikedPosts";
    private static final String IS_LOGGED_IN_KEY = "IsLoggedIn";
    private static final String USERNAME_KEY = "Username";
    private static final String EMAIL_KEY = "Email";

    private UserManager(Context context) {
        Context appContext = context.getApplicationContext();
        sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        databaseHelper = new UserDatabaseHelper(appContext);
        loadUser();
    }

    public static synchronized UserManager getInstance(Context context) {       //单例模式
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    public boolean validateUser(String username, String password) {
        return databaseHelper.validateUser(username, password);
    }

    public boolean addUser(String username, String password, String email) {
        return databaseHelper.addUser(username, password, email);
    }

    public boolean isUsernameExists(String username) {
        return databaseHelper.isUsernameExists(username);
    }

    public void login(String username, String email) {
        currentUser = new User(username, email);
        currentUser.setLoggedIn(true);
        saveUser();
    }

    public void logout() {
        if (currentUser != null) {
            currentUser.setLoggedIn(false);
        }
        // Clear login status but keep liked posts
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LOGGED_IN_KEY, false);
        editor.putString(USERNAME_KEY, "");
        editor.putString(EMAIL_KEY, "");
        editor.apply();
    }

    public boolean isLoggedIn() {
        if (currentUser != null) {
            return currentUser.isLoggedIn();
        }
        return sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getUsername() {
        if (currentUser != null && currentUser.isLoggedIn()) {
            return currentUser.getUsername();
        }
        return sharedPreferences.getString(USERNAME_KEY, "");
    }

    private void saveUser() {
        if (currentUser != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(IS_LOGGED_IN_KEY, currentUser.isLoggedIn());
            editor.putString(USERNAME_KEY, currentUser.getUsername());
            editor.putString(EMAIL_KEY, currentUser.getEmail());
            editor.apply();
        }
    }

    private void loadUser() {
        boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false);
        String username = sharedPreferences.getString(USERNAME_KEY, "");
        String email = sharedPreferences.getString(EMAIL_KEY, "");

        if (isLoggedIn && !username.isEmpty()) {
            currentUser = new User(username, email);
            currentUser.setLoggedIn(true);
        }
    }

    // Method to save liked posts to SharedPreferences
    public void saveLikedPosts(List<String> likedPostIds) {
        // 将列表转换为逗号分隔的字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < likedPostIds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(likedPostIds.get(i));
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LIKED_POSTS_KEY, sb.toString());
        editor.apply();
    }

    public List<String> getLikedPosts() {
        String likedPostsString = sharedPreferences.getString(LIKED_POSTS_KEY, "");
        if (!likedPostsString.isEmpty()) {
            // 将逗号分隔的字符串转换回列表
            String[] ids = likedPostsString.split(",");
            List<String> result = new ArrayList<>();
            for (String id : ids) {
                if (!id.trim().isEmpty()) {
                    result.add(id.trim());
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}