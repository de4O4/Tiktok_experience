package com.example.tiktokexperience;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.tiktokexperience.User.UserManager;
import com.example.tiktokexperience.User.UserSessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private Button buttonSkipLogin;
    private Button buttonGoToRegister;
    private UserManager userManager;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSkipLogin = findViewById(R.id.buttonSkipLogin);
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister);

        // 初始化用户管理器和会话管理器
        userManager = UserManager.getInstance(this);
        sessionManager = new UserSessionManager(this);

        // 设置登录按钮点击事件
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // 设置跳过登录按钮点击事件
        buttonSkipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接返回到主界面，不进行登录
                Toast.makeText(LoginActivity.this, "您可以继续浏览，但点赞记录将不会保存", Toast.LENGTH_LONG).show();

                // 启动主界面Activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // 关闭登录界面，防止用户返回登录页面
                finish();
            }
        });

        // 设置注册按钮点击事件
        buttonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void performLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // 简单验证
        if (username.isEmpty()) {
            editTextUsername.setError("请输入用户名");
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("请输入密码");
            return;
        }

        // 验证用户名和密码
        if (userManager.validateUser(username, password)) {
            Toast.makeText(this, "登录成功！欢迎 " + username, Toast.LENGTH_SHORT).show();
            // 创建登录会话
            sessionManager.createLoginSession(username);

            // 同步本地点赞数据到用户账户
            //LikeManager likeManager = new LikeManager(this);
           // likeManager.syncLocalToUser();

            // 登录成功后返回主界面
            setResult(RESULT_OK);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}