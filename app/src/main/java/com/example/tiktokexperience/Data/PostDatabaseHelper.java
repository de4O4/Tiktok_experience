package com.example.tiktokexperience.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.tiktokexperience.Bean.PostItem;

public class PostDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "post_database.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_USER_AVATAR = "user_avatar";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_LIKE_COUNT = "like_count";

    private static final String CREATE_TABLE_POSTS = "CREATE TABLE " + TABLE_POSTS + "("
            + COLUMN_POST_ID + " TEXT PRIMARY KEY,"
            + COLUMN_IMAGE_URL + " TEXT,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_USER_AVATAR + " TEXT,"
            + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_LIKE_COUNT + " INTEGER"
            + ")";

    public PostDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_POSTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }


    public boolean savePost(PostItem postItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_ID, postItem.getId());
        values.put(COLUMN_IMAGE_URL, postItem.getImageUrl());
        values.put(COLUMN_TITLE, postItem.getTitle());
        values.put(COLUMN_USER_AVATAR, postItem.getUserAvatar());
        values.put(COLUMN_USER_NAME, postItem.getUserName());
        values.put(COLUMN_LIKE_COUNT, postItem.getBaseLikeCount());

        long result = db.insert(TABLE_POSTS, null, values);
        db.close();
        return result != -1;
    }

    // 检查帖子是否已存在于数据库中
    public boolean isPostExists(String postId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_POSTS + " WHERE " + COLUMN_POST_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{postId});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // 如果帖子不存在则保存帖子信息
    public boolean savePostIfNotExists(PostItem postItem) {
        if (!isPostExists(postItem.getId())) {
            return savePost(postItem);
        }
        return true; // 帖子已存在，视为成功
    }
}