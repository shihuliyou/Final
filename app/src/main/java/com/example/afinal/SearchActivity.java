package com.example.afinal;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// SearchActivity.java
public class SearchActivity extends AppCompatActivity {
    private EditText editSearch;
    private TextView textResult;
    private Button btnAdd;
    private AppDatabase db;
    private int userId;
    private Stock foundStock;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        editSearch   = findViewById(R.id.editSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        textResult   = findViewById(R.id.textResult);
        btnAdd       = findViewById(R.id.btnAdd);
        db           = AppDatabase.getDatabase(this);
        userId       = getIntent().getIntExtra("userId", -1);

        textResult.setVisibility(View.GONE);
        btnAdd.setVisibility(View.GONE);

        btnSearch.setOnClickListener(v -> {
            String code = editSearch.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "请输入股票代码", Toast.LENGTH_SHORT).show();
                return;
            }
            searchStockByEastMoney(code);
        });

        btnAdd.setOnClickListener(v -> {
            if (foundStock != null) {
                db.stockDao().insertStock(foundStock);
                WatchlistEntry entry = new WatchlistEntry();
                entry.userId      = userId;
                entry.stockSymbol = foundStock.getSymbol();
                db.watchlistDao().insertEntry(entry);
                Toast.makeText(this, "已添加自选: " + foundStock.getSymbol(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void searchStockByEastMoney(String symbolQuery) {
        // 市场前缀：上海 1.xxx，深圳 0.xxx
        String market = symbolQuery.startsWith("6") ? "1" : "0";
        String secid  = market + "." + symbolQuery;
        // 请求 f43=最新价, f58=名称
        String url    = "https://push2.eastmoney.com/api/qt/stock/get?fields=f43,f58&secid=" + secid;

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                String json = response.body().string().trim();

                JSONObject root = new JSONObject(json);
                JSONObject data = root.optJSONObject("data");
                if (data != null) {
                    // f43 返回值需除以100
                    double rawPrice = data.optDouble("f43", 0.0);
                    double price    = rawPrice / 100.0;
                    String name     = data.optString("f58", "");

                    foundStock = new Stock();
                    foundStock.setSymbol(symbolQuery);
                    foundStock.setName(name);
                    foundStock.setPrice(price);

                    runOnUiThread(() -> {
                        textResult.setText(
                                String.format("%s (%s) 当前价格: %.2f", name, symbolQuery, price)
                        );
                        textResult.setVisibility(View.VISIBLE);
                        btnAdd.setVisibility(View.VISIBLE);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                "未找到股票: " + symbolQuery,
                                Toast.LENGTH_SHORT
                        ).show();
                        textResult.setVisibility(View.GONE);
                        btnAdd.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "搜索失败: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        }).start();
    }
}
