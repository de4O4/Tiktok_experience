package com.example.tiktokexperience.Bean;

public class PostItem {
    private String id;
    private String imageUrl;
    private String title;
    private String userAvatar;
    private String userName;
    private int baseLikeCount;

    public PostItem(String id, String imageUrl, String title, String userAvatar, String userName, int baseLikeCount) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.userAvatar = userAvatar;
        this.userName = userName;
        this.baseLikeCount = baseLikeCount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public String getUserAvatar() { return userAvatar; }
    public String getUserName() { return userName; }
    public int getBaseLikeCount() { return baseLikeCount; }
}