package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.concurrent.Executors;

public class HoldingActivity extends AppCompatActivity {
    private AppDatabase db;
    private HoldingDao dao;
    private StockHoldingAdapter adapter;

    private EditText etCode, etQty, etPrice;
    private Button btnBuy, btnSell;
    private RecyclerView rv;
    private BottomNavigationView bottomNav;     // 新增：底部导航

    // 新增：保存当前用户 ID
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holding);

        // 1. 取 userId
        userId = getIntent().getIntExtra("userId", -1);

        // 2. 初始化 Room
        db  = AppDatabase.getDatabase(this);
        dao = db.holdingDao();

        // 3. findViewById
        etCode  = findViewById(R.id.etStockCode);
        etQty   = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        btnBuy  = findViewById(R.id.btnBuy);
        btnSell = findViewById(R.id.btnSell);
        rv      = findViewById(R.id.rvHoldings);

        // 4. RecyclerView & Adapter
        adapter = new StockHoldingAdapter(null);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 5. 交易按钮
        btnBuy.setOnClickListener(v -> trade(true));
        btnSell.setOnClickListener(v -> trade(false));

        // 6. 底部导航
        bottomNav = findViewById(R.id.bottomNav);
        // 默认选中“持仓”页（nav_portfolio）
        bottomNav.setSelectedItemId(R.id.nav_portfolio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent iHome = new Intent(this, HomeActivity.class);
                iHome.putExtra("userId", userId);
                startActivity(iHome);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_watchlist) {
                Intent iWatch = new Intent(this, MainActivity.class);
                iWatch.putExtra("userId", userId);
                startActivity(iWatch);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_portfolio) {
                // 已在持仓页，不做任何操作
                return true;
            }
            return false;
        });

        // 7. 首次加载列表
        refreshList();
    }

    private void trade(boolean isBuy) {
        String code = etCode.getText().toString().trim();
        String sQty = etQty.getText().toString().trim();
        String sPrice = etPrice.getText().toString().trim();
        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(sQty) || TextUtils.isEmpty(sPrice)) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }
        int qty = Integer.parseInt(sQty);
        double price = Double.parseDouble(sPrice);

        Executors.newSingleThreadExecutor().execute(() -> {
            Holding h = dao.findByCodeAndUser(code, userId);
            if (h == null) {
                if (!isBuy) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "无此持仓", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                h = new Holding(userId, code, qty, price);
                dao.insert(h);
            } else {
                int newQty = isBuy ? h.quantity + qty : h.quantity - qty;
                if (newQty < 0) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "卖出数量超过持有", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                double newAvg = isBuy
                        ? (h.averagePrice * h.quantity + price * qty) / newQty
                        : h.averagePrice;
                h.quantity     = newQty;
                h.averagePrice = newAvg;
                if (newQty == 0) {
                    dao.deleteById(h.id);
                } else {
                    dao.update(h);
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this,
                        isBuy ? "买入成功" : "卖出成功",
                        Toast.LENGTH_SHORT).show();
                refreshList();
            });
        });
    }

    private void refreshList() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Holding> list = dao.getHoldingsByUserId(userId);
            runOnUiThread(() -> adapter.setData(list));
        });
    }
}
