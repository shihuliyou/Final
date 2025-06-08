package com.example.afinal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private StockListAdapter adapter;
    private AppDatabase db;
    private int userId;
    private ExecutorService executor;
    private OkHttpClient httpClient;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化线程池和 HTTP 客户端
        executor = Executors.newSingleThreadExecutor();
        httpClient = new OkHttpClient();

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 下拉刷新
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            loadAndRefreshStocks();
            swipeRefresh.setRefreshing(false);
        });

        // 获取 Intent 参数并设置标题
        userId = getIntent().getIntExtra("userId", -1);
        String username = getIntent().getStringExtra("username");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("自选股 - " + (username != null ? username : ""));
        }

        // RecyclerView & Adapter
        recyclerView = findViewById(R.id.recyclerWatchlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);  // ← 确保开启嵌套滚动
        adapter = new StockListAdapter(this, new ArrayList<>(), userId);
        recyclerView.setAdapter(adapter);

        // 添加分隔线装饰
        DividerItemDecoration decor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decor.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(decor);

        // 数据库实例化
        db = AppDatabase.getDatabase(this);

        // 悬浮添加按钮
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // 底部导航
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
// 默认选中当前页面对应的 menu id
        bottomNav.setSelectedItemId(R.id.nav_watchlist);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // 切换到首页
                startActivity(new Intent(this, HomeActivity.class)
                        .putExtra("userId", userId)
                        .putExtra("username", username));
                finish();
                return true;
            } else if (id == R.id.nav_watchlist) {
                // 已在自选股页，不做任何操作
                return true;
            } else if (id == R.id.nav_portfolio) {
                // 切换到我的持股
                startActivity(new Intent(this, HoldingActivity.class)
                        .putExtra("userId", userId)
                        .putExtra("username", username));
                finish();
                return true;
            }
            return false;
        });


        // 首次加载数据
        loadAndRefreshStocks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndRefreshStocks();
    }

    private void loadAndRefreshStocks() {
        executor.execute(() -> {
            // 1. 从数据库获取列表
            List<Stock> stocks = db.watchlistDao().getStocksForUser(userId);

            // 2. 更新 UI 列表
            runOnUiThread(() -> {
                adapter.setStocks(stocks);
                adapter.notifyDataSetChanged();
            });

            // 3. 异步更新行情
            updateStockPrices(stocks);
        });
    }

    private void updateStockPrices(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) return;

        executor.execute(() -> {
            for (Stock s : stocks) {
                try {
                    String market = s.getSymbol().startsWith("6") ? "1" : "0";
                    String secid  = market + "." + s.getSymbol();
                    String url    = "https://push2.eastmoney.com/api/qt/stock/get?fields=f43&secid=" + secid;

                    Request req = new Request.Builder().url(url).build();
                    Response resp = httpClient.newCall(req).execute();
                    String body = resp.body().string().trim();

                    JSONObject root = new JSONObject(body);
                    JSONObject data = root.optJSONObject("data");
                    if (data != null) {
                        double raw = data.optDouble("f43", s.getPrice() * 100);
                        s.setPrice(raw / 100.0);
                        db.stockDao().updateStock(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 更新完成后再次刷新列表
            runOnUiThread(adapter::notifyDataSetChanged);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
