package com.example.afinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StockHoldingAdapter extends RecyclerView.Adapter<StockHoldingAdapter.VH> {
    private List<Holding> data;

    public StockHoldingAdapter(List<Holding> data) {
        this.data = data;
    }

    public void setData(List<Holding> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_holding, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Holding h = data.get(position);
        // h.stockCode 已在 loadUserHoldings() 中被替换为“名称”
        holder.code.setText(h.stockCode);
        holder.qty.setText("数量: " + h.quantity);
        holder.avgPrice.setText(String.format("成本: ¥%.2f", h.averagePrice));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView code, qty, avgPrice;

        VH(@NonNull View itemView) {
            super(itemView);
            code     = itemView.findViewById(R.id.tvHoldingCode);
            qty      = itemView.findViewById(R.id.tvHoldingQty);
            avgPrice = itemView.findViewById(R.id.tvHoldingAvgPrice);
        }
    }
}
