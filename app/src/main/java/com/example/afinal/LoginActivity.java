package com.example.afinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText editUsername, editPassword;
    private ImageButton btnTogglePwd;
    private Button btnLogin;
    private TextView linkRegister, linkForgot, linkPrivacy, linkAgreement, tvTip;
    private ViewPager2 imageSlider;
    private boolean pwdVisible = false;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final String[] tips = {
            "坚持定投，时间是最好的朋友。",
            "分散投资，降低单一风险。",
            "多关注财报，洞察企业基本面。",
            "设置止盈止损，控制回撤。"
    };

    private final int[] banners = {
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 立即绑定必要控件
        editUsername  = findViewById(R.id.editUsername);
        editPassword  = findViewById(R.id.editPassword);
        btnTogglePwd  = findViewById(R.id.btnTogglePwd);
        btnLogin      = findViewById(R.id.btnLogin);
        linkRegister  = findViewById(R.id.linkRegister);
        linkForgot    = findViewById(R.id.linkForgot);
        linkPrivacy   = findViewById(R.id.linkPrivacy);
        linkAgreement = findViewById(R.id.linkAgreement);
        tvTip         = findViewById(R.id.tvTip);
        imageSlider   = findViewById(R.id.imageSlider);

        // 显示随机小贴士
        tvTip.setText("小贴士：" + tips[new Random().nextInt(tips.length)]);

        // 密码可见性切换
        btnTogglePwd.setOnClickListener(v -> {
            pwdVisible = !pwdVisible;
            if (pwdVisible) {
                editPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );
                btnTogglePwd.setImageResource(R.drawable.ic_eye_open);
            } else {
                editPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );
                btnTogglePwd.setImageResource(R.drawable.ic_eye_closed);
            }
            editPassword.setSelection(editPassword.getText().length());
        });

        // 链接跳转
        linkForgot.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.example.com/forgot")))
        );
        linkRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
        linkPrivacy.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.example.com/privacy")))
        );
        linkAgreement.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.example.com/terms")))
        );

        // 登录点击：异步验证，立即响应，无延时
        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            btnLogin.setEnabled(false);
            btnLogin.setText("登录中…");

            executor.execute(() -> {
                if (db == null) {
                    db = AppDatabase.getDatabase(LoginActivity.this);
                }
                User user = db.userDao().getUserByCredentials(username, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class)
                                .putExtra("userId", user.id)
                                .putExtra("username", user.username));
                        finish();
                    } else {
                        Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("登录");
                    }
                });
            });
        });

        // 延后绑定 BannerAdapter，优先渲染布局
        imageSlider.post(() -> {
            imageSlider.setAdapter(new BannerAdapter());
            imageSlider.setOffscreenPageLimit(1);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // 保留原有 BannerAdapter 逻辑
    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        @Override
        public BannerViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            ImageView iv = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_banner, parent, false);
            return new BannerViewHolder(iv);
        }

        @Override
        public void onBindViewHolder(BannerViewHolder holder, int position) {
            holder.imageView.setImageResource(banners[position]);
        }

        @Override
        public int getItemCount() {
            return banners.length;
        }

        class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            BannerViewHolder(ImageView view) {
                super(view);
                imageView = view;
            }
        }
    }
}
