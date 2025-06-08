package com.example.afinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HoldingAdapter extends RecyclerView.Adapter<HoldingAdapter.ViewHolder> {
    private List<Holding> items;

    public HoldingAdapter(List<Holding> items) {
        this.items = items;
    }

    public void setItems(List<Holding> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public HoldingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_holding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoldingAdapter.ViewHolder holder, int position) {
        Holding h = items.get(position);
        holder.tvStockCode.setText(h.stockCode);
        holder.tvQuantity.setText(String.valueOf(h.quantity));
        holder.tvAvgPrice.setText(String.format("¥%.2f", h.averagePrice));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStockCode;
        TextView tvQuantity;
        TextView tvAvgPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStockCode = itemView.findViewById(R.id.tvHoldingCode);
            tvQuantity  = itemView.findViewById(R.id.tvHoldingQty);
            tvAvgPrice  = itemView.findViewById(R.id.tvHoldingAvgPrice);
        }
    }
}
