package com.example.afinal;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockDetailActivity extends AppCompatActivity {
    private static final String TAG = "StockDetailActivity";

    private TextView textTitle, textPrice;
    private PriceChartView chart;
    private RecyclerView recyclerNews;
    private NewsAdapter newsAdapter;
    private AppDatabase db;
    private String symbol;
    private final OkHttpClient http = new OkHttpClient();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        // —— Toolbar 绑定与返回键处理 —— //
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        // 显示返回箭头
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // —— UI 元素绑定 —— //
        textTitle    = findViewById(R.id.textTitle);
        textPrice    = findViewById(R.id.textPrice);
        chart        = findViewById(R.id.chart);
        recyclerNews = findViewById(R.id.recyclerNews);

        // —— RecyclerView & Adapter 初始化 —— //
        recyclerNews.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, new ArrayList<>());
        recyclerNews.setAdapter(newsAdapter);

        // —— 数据库和 Intent —— //
        db     = AppDatabase.getDatabase(this);
        symbol = getIntent().getStringExtra("symbol");

        // —— 读取本地存储的股票名称和价格 —— //
        Stock stock = db.stockDao().getStock(symbol);
        if (stock != null) {
            textTitle.setText(stock.getName() + " (" + symbol + ")");
            textPrice.setText("当前价: " + String.format("%.2f", stock.getPrice()));
        } else {
            textTitle.setText(symbol);
            textPrice.setText("当前价: --");
        }

        // —— 启动数据加载 —— //
        loadHistoricalData();
        updateCurrentPrice();
        fetchNews();
    }

    /** 拉取历史收盘价并设置折线图 */
    private void loadHistoricalData() {
        new Thread(() -> {
            List<Float> points = new ArrayList<>();
            try {
                String market = symbol.startsWith("6") ? "sh" : "sz";
                String url = "https://money.finance.sina.com.cn/quotes_service/api/json_v2.php"
                        + "/CN_MarketData.getKLineData?symbol=" + market + symbol
                        + "&scale=240&ma=no&datalen=30";

                Request req  = new Request.Builder().url(url).build();
                Response resp = http.newCall(req).execute();
                String body = resp.body().string().trim();

                int idx = body.indexOf('[');
                if (idx >= 0) {
                    JSONArray arr = new JSONArray(body.substring(idx));
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        points.add((float) o.optDouble("close", 0));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "loadHistoricalData: 异常", e);
            }
            runOnUiThread(() -> chart.setData(points));
        }).start();
    }

    /** 拉取实时价并更新 UI 与本地数据库 */
    private void updateCurrentPrice() {
        new Thread(() -> {
            try {
                String market = symbol.startsWith("6") ? "1" : "0";
                String secid  = market + "." + symbol;
                String url    = "https://push2.eastmoney.com/api/qt/stock/get"
                        + "?fields=f43&secid=" + secid;

                Request req  = new Request.Builder().url(url).build();
                Response resp = http.newCall(req).execute();
                JSONObject data = new JSONObject(resp.body().string())
                        .optJSONObject("data");

                if (data != null) {
                    double raw = data.optDouble("f43", Double.NaN);
                    if (!Double.isNaN(raw)) {
                        double price = raw / 100.0;
                        runOnUiThread(() ->
                                textPrice.setText("当前价: " + String.format("%.2f", price))
                        );
                        // 更新本地缓存
                        Stock s = db.stockDao().getStock(symbol);
                        if (s != null) {
                            s.setPrice(price);
                            db.stockDao().updateStock(s);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "updateCurrentPrice: 异常", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "实时价更新失败", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    /** 拉取新闻（JSON 优先，失败后回退 HTML） */
    private void fetchNews() {
        new Thread(() -> {
            List<NewsItem> list = new ArrayList<>();
            // 1) JSON 接口
            try {
                String api = "https://feed.mix.sina.com.cn/api/roll/get?"
                        + "pageid=153&lid=2212&num=100&page=1";
                Response resp = http.newCall(
                        new Request.Builder().url(api).build()
                ).execute();
                JSONObject res = new JSONObject(resp.body().string())
                        .optJSONObject("result");
                if (res != null && res.optJSONObject("status").optInt("code") == 0) {
                    JSONArray data = res.optJSONArray("data");
                    for (int i = 0; i < data.length() && list.size() < 100; i++) {
                        JSONObject o = data.getJSONObject(i);
                        String title = o.optString("title");
                        String url   = o.optString("url");
                        if (!title.isEmpty() && url.startsWith("http")) {
                            list.add(new NewsItem(title, url));
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "fetchNews: JSON 接口失败，回退 HTML", e);
            }
            // 2) HTML 回退
            if (list.size() < 100) {
                list = fetchNewsHTML(list);
            }
            // 3) 更新 Adapter
            List<NewsItem> finalList = list;
            runOnUiThread(() -> {
                newsAdapter.updateData(finalList);
                newsAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    /** 使用 Jsoup 抓 HTML 页面中的新闻 */
    private List<NewsItem> fetchNewsHTML(List<NewsItem> list) {
        try {
            String url = "https://finance.sina.com.cn/roll/index.d.html?cid=56588&page=1";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();
            Elements items = doc.select("div.listBlk li a");
            for (Element el : items) {
                if (list.size() >= 100) break;
                String title = el.text().trim();
                String href  = el.absUrl("href");
                if (!title.isEmpty() && href.startsWith("http")) {
                    list.add(new NewsItem(title, href));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchNewsHTML: 异常", e);
        }
        return list;
    }
}
