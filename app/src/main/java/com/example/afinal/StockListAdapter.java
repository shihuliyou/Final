package com.example.afinal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.StockViewHolder> {
    private Context context;
    private List<Stock> stocks;
    private int userId;
    private int layoutResId;    // 新增：布局资源 ID

    /** 默认构造，使用 item_stock.xml */
    public StockListAdapter(Context context, List<Stock> stocks, int userId) {
        this(context, stocks, userId, R.layout.item_stock);
    }

    /** 可指定布局，用于首页推荐使用 item_stock2.xml */
    public StockListAdapter(Context context, List<Stock> stocks, int userId, int layoutResId) {
        this.context     = context;
        this.stocks      = stocks;
        this.userId      = userId;
        this.layoutResId = layoutResId;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
        notifyDataSetChanged();
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(layoutResId, parent, false);   // 根据传入的 layoutResId 来 inflate
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        Stock stock = stocks.get(position);
        holder.txtName.setText(stock.getName());
        holder.txtSymbol.setText(stock.getSymbol());
        holder.txtPrice.setText(String.format("%.2f", stock.getPrice()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StockDetailActivity.class)
                    .putExtra("symbol", stock.getSymbol());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            db.watchlistDao().deleteEntry(userId, stock.getSymbol());
            stocks.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context,
                    "已移除自选股: " + stock.getSymbol(),
                    Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return stocks != null ? stocks.size() : 0;
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtSymbol, txtPrice;
        public StockViewHolder(View itemView) {
            super(itemView);
            txtName   = itemView.findViewById(R.id.txtStockName);
            txtSymbol = itemView.findViewById(R.id.txtStockSymbol);
            txtPrice  = itemView.findViewById(R.id.txtStockPrice);
        }
    }
}
