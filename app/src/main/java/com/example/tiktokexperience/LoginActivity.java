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
import com.example.tiktokexperience.Data.UserDatabaseHelper;

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


        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSkipLogin = findViewById(R.id.buttonSkipLogin);
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister);

        userManager = UserManager.getInstance(this);
        sessionManager = new UserSessionManager(this);


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });


        buttonSkipLogin.setOnClickListener(new View.OnClickListener() {          // 设置跳过登录按钮点击事件
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "您可以继续浏览，但点赞记录将不会保存", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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

        if (username.isEmpty()) {
            editTextUsername.setError("请输入用户名");
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("请输入密码");
            return;
        }

        if (userManager.validateUser(username, password)) {
            Toast.makeText(this, "登录成功！欢迎 " + username, Toast.LENGTH_SHORT).show();
            UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);
            userManager.login(username, dbHelper.getUserEmail(username)); // 获取用户邮
            setResult(RESULT_OK);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}