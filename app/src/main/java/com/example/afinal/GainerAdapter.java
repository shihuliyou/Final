package com.example.afinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GainerAdapter extends RecyclerView.Adapter<GainerAdapter.VH> {
    private List<Index> items;

    public GainerAdapter(List<Index> items) {
        this.items = items;
    }

    /** 允许外部更新列表 **/
    public void setItems(List<Index> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /** 允许外部获取当前列表 **/
    public List<Index> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gainer, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Index s = items.get(pos);
        h.tvName.setText(s.name);
        h.tvPrice.setText(String.format("%.2f", s.price));
        h.tvChange.setText(String.format("%.2f%%", s.change));
        h.tvChange.setTextColor(
                s.change >= 0 ? 0xFF4CAF50 : 0xFFF44336);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvChange;
        VH(@NonNull View v) {
            super(v);
            tvName   = v.findViewById(R.id.tvGainerName);
            tvPrice  = v.findViewById(R.id.tvGainerPrice);
            tvChange = v.findViewById(R.id.tvGainerChange);
        }
    }
}
