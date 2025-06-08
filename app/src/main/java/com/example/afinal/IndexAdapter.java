package com.example.afinal;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.VH> {
    private List<Index> items;

    public IndexAdapter(List<Index> items) {
        this.items = items;
    }

    /**
     * 更新列表数据
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<Index> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * 提供给外部获取当前列表（如果需要）
     */
    public List<Index> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_index, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Index idx = items.get(pos);
        h.tvName.setText(idx.name);
        h.tvPrice.setText(String.format("%.2f", idx.price));
        h.tvChange.setText(String.format("%.2f%%", idx.change));
        h.tvChange.setTextColor(
                idx.change >= 0 ? 0xFF4CAF50 : 0xFFF44336);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvChange;
        VH(@NonNull View v) {
            super(v);
            tvName   = v.findViewById(R.id.tvIndexName);
            tvPrice  = v.findViewById(R.id.tvIndexPrice);
            tvChange = v.findViewById(R.id.tvIndexChange);
        }
    }
}
