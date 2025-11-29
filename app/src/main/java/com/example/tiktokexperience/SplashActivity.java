package com.example.tiktokexperience;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tiktokexperience.Optimize.PreloadManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        PreloadManager preloadManager = PreloadManager.getInstance(this);
        preloadManager.preloadData(15, data -> {
            // 预加载完成后启动主界面
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 1500); // 给用户一点视觉反馈时间
        });
    }


}