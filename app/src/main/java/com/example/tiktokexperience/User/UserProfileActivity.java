package com.example.tiktokexperience.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.tiktokexperience.Bean.LikeManager;
import com.example.tiktokexperience.Bean.PostItem;
import com.example.tiktokexperience.User.UserManager;

import com.example.tiktokexperience.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

    }
}