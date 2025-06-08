package com.example.afinal; // 根据项目实际包名修改

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText editUsername;
    private EditText editPassword;
    private EditText editConfirm;
    private ImageButton btnTogglePwdReg;
    private ImageButton btnToggleConfirm;
    private TextView linkPrivacy;
    private TextView linkAgreement;
    private boolean pwdVisible = false;
    private boolean confirmVisible = false;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getDatabase(this);

        editUsername     = findViewById(R.id.editRegUsername);
        editPassword     = findViewById(R.id.editRegPassword);
        editConfirm      = findViewById(R.id.editRegConfirm);
        btnTogglePwdReg  = findViewById(R.id.btnTogglePwdReg);
        btnToggleConfirm = findViewById(R.id.btnToggleConfirm);
        Button btnRegister = findViewById(R.id.btnRegister);
        linkPrivacy      = findViewById(R.id.linkPrivacy);
        linkAgreement    = findViewById(R.id.linkAgreement);

        // 切换密码可见性
        btnTogglePwdReg.setOnClickListener(v -> {
            pwdVisible = !pwdVisible;
            if (pwdVisible) {
                editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePwdReg.setImageResource(R.drawable.ic_eye_open);
            } else {
                editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePwdReg.setImageResource(R.drawable.ic_eye_closed);
            }
            editPassword.setSelection(editPassword.length());
        });

        // 切换确认密码可见性
        btnToggleConfirm.setOnClickListener(v -> {
            confirmVisible = !confirmVisible;
            if (confirmVisible) {
                editConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnToggleConfirm.setImageResource(R.drawable.ic_eye_open);
            } else {
                editConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnToggleConfirm.setImageResource(R.drawable.ic_eye_closed);
            }
            editConfirm.setSelection(editConfirm.length());
        });

        // 隐私条款跳转
        linkPrivacy.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/privacy")));
        });

        // 用户协议跳转
        linkAgreement.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/terms")));
        });

        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirm  = editConfirm.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            User existing = db.userDao().getUserByUsername(username);
            if (existing != null) {
                Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User();
            newUser.username = username;
            newUser.password = password;
            db.userDao().insertUser(newUser);

            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
