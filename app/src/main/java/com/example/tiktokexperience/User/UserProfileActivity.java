package com.example.tiktokexperience.User;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tiktokexperience.R;

public class UserProfileActivity extends AppCompatActivity {
    private UserManager userManager;
    private android.widget.Button buttonLogout;
    private android.widget.TextView textViewUsername, textViewUserStatus;
    private androidx.recyclerview.widget.RecyclerView recyclerViewLikedPosts;
    private android.widget.TextView textViewNoLikes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        initViews();

        // 获取UserManager实例
        userManager = UserManager.getInstance(this);

        // 更新UI显示用户信息
        updateUserInfo();

        // 设置退出登录按钮点击事件
        setupLogoutButton();
    }

    private void initViews() {
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewUserStatus = findViewById(R.id.textViewUserStatus);
        recyclerViewLikedPosts = findViewById(R.id.recyclerViewLikedPosts);
        textViewNoLikes = findViewById(R.id.textViewNoLikes);
    }

    private void updateUserInfo() {
        if (userManager.isLoggedIn()) {
            String username = userManager.getUsername();
            textViewUsername.setText(username);
            textViewUserStatus.setText("已登录");
        } else {
            textViewUsername.setText("未登录");
            textViewUserStatus.setText("游客模式");
        }
    }

    private void setupLogoutButton() {
        buttonLogout.setOnClickListener(v -> {
            // 退出登录
            userManager.logout();
            updateUserInfo();
            // 返回到主界面
            finish();
        });

    }
}