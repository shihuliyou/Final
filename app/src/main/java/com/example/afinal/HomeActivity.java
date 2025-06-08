package com.example.afinal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private RecyclerView rvIndices, rvHoldings, rvRandomStocks;
    private BottomNavigationView bottomNav;

    private ExecutorService executor;
    private OkHttpClient httpClient;
    private AppDatabase db;
    private int userId;
    private String username;

    private IndexAdapter indexAdapter;
    private StockHoldingAdapter holdingAdapter;
    private GainerAdapter randomAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        executor   = Executors.newSingleThreadExecutor();
        httpClient = new OkHttpClient();
        db         = AppDatabase.getDatabase(this);
        userId     = getIntent().getIntExtra("userId", -1);
        username   = getIntent().getStringExtra("username");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.ic_logo);
        setSupportActionBar(toolbar);

        // Views
        tvWelcome      = findViewById(R.id.tvWelcome);
        rvIndices      = findViewById(R.id.rvIndices);
        rvHoldings     = findViewById(R.id.rvHoldings);
        rvRandomStocks = findViewById(R.id.rvRandomStocks);
        bottomNav      = findViewById(R.id.bottomNav);

        tvWelcome.setText("Hello，" + (username != null ? username : ""));

        // RecyclerView 布局管理
        rvIndices.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHoldings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRandomStocks.setLayoutManager(new GridLayoutManager(this, 3));

        // Adapters
        indexAdapter   = new IndexAdapter(new ArrayList<>());
        holdingAdapter = new StockHoldingAdapter(new ArrayList<>());
        randomAdapter  = new GainerAdapter(new ArrayList<>());

        rvIndices.setAdapter(indexAdapter);
        rvHoldings.setAdapter(holdingAdapter);
        rvRandomStocks.setAdapter(randomAdapter);

        // BottomNavigation
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_watchlist) {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("userId", userId)
                        .putExtra("username", username));
                finish();
                return true;
            } else if (id == R.id.nav_portfolio) {
                startActivity(new Intent(this, HoldingActivity.class)
                        .putExtra("userId", userId)
                        .putExtra("username", username));
                finish();
                return true;
            }
            return false;
        });

        cacheAndFetchHistory();
        loadMarketIndices();
        loadUserHoldings();
        loadRandomRecommendationsFromLocal();
    }

    /** 缓存前200支股票 & 抓取最近3日收盘价 */
    private void cacheAndFetchHistory() {
        executor.execute(() -> {
            try {
                String urlPool = "https://push2.eastmoney.com/api/qt/clist/get"
                        + "?pn=1&pz=200&po=1&np=1&fltt=2&invt=2"
                        + "&fields=f12,f14,f2&market=0"
                        + "&ut=7eea3edcaed734bea9cbfc24409ed989";
                Request reqPool = new Request.Builder()
                        .url(urlPool)
                        .addHeader("Accept", "application/json, text/plain, */*")
                        .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                        .addHeader("Connection", "keep-alive")
                        .build();
                Response respPool = httpClient.newCall(reqPool).execute();
                JSONArray arr = new JSONObject(respPool.body().string())
                        .optJSONObject("data")
                        .optJSONArray("diff");

                List<Stock> pool = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    pool.add(new Stock(o.optString("f12"), o.optString("f14"), o.optDouble("f2")));
                }

                db.stockDao().clearAllStocks();
                db.stockDao().insertAll(pool);

                runOnUiThread(() ->
                        Toast.makeText(this, "市场数据已更新，共 " + pool.size() + " 支股票", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception ignored) {}
        });
    }

    /** 获取最近3个交易日（跳过周末） */
    private List<String> getLast3TradingDates() {
        List<String> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        while (dates.size() < 3) {
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) {
                dates.add(fmt.format(cal.getTime()));
            }
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        return dates;
    }

    /** 加载并展示实时指数 */
    private void loadMarketIndices() {
        List<Index> list = new ArrayList<>();
        list.add(new Index("上证综指", "1.000001"));
        list.add(new Index("深证成指", "0.399001"));
        list.add(new Index("创业板指", "0.399006"));
        list.add(new Index("北证50",   "1.000016"));
        indexAdapter.setItems(list);

        executor.execute(() -> {
            for (Index idx : list) {
                try {
                    Request req = new Request.Builder()
                            .url("https://push2.eastmoney.com/api/qt/stock/get?fields=f43,f46&secid=" + idx.secid)
                            .addHeader("Referer", "https://quote.eastmoney.com/")
                            .build();
                    JSONObject d = new JSONObject(httpClient.newCall(req).execute()
                            .body().string())
                            .optJSONObject("data");
                    if (d != null) {
                        double now  = d.optDouble("f43") / 100.0;
                        double prev = d.optDouble("f46") / 100.0;
                        idx.setPrice(now);
                        idx.setChange(prev > 0 ? (now - prev) / prev * 100.0 : 0);
                    }
                } catch (Exception ignored) {}
            }
            runOnUiThread(indexAdapter::notifyDataSetChanged);
        });
    }

    /** 从本地数据库加载当前用户的持仓并替换为名称 */
    private void loadUserHoldings() {
        executor.execute(() -> {
            List<Holding> holdings = db.holdingDao().getHoldingsByUserId(userId);
            for (Holding h : holdings) {
                Stock s = db.stockDao().getStock(h.stockCode);
                if (s != null) {
                    h.stockCode = s.getName();
                }
            }
            runOnUiThread(() -> holdingAdapter.setData(holdings));
        });
    }

    /** 随机推荐：只显示名称和现价，不展示涨跌 */
    private void loadRandomRecommendationsFromLocal() {
        executor.execute(() -> {
            List<Stock> all = db.stockDao().getAllStocks();
            Collections.shuffle(all);
            int n = Math.min(all.size(), 9);
            List<Index> picks = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Stock s = all.get(i);
                picks.add(new Index(s.getName(), s.getSymbol(), s.getPrice(), 0));
            }
            runOnUiThread(() -> randomAdapter.setItems(picks));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
