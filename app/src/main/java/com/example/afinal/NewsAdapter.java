package com.example.afinal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final Context context;
    private final List<NewsItem> newsList;

    public NewsAdapter(Context context, List<NewsItem> initialList) {
        this.context = context;
        // 确保内部持有的是可变列表
        this.newsList = new ArrayList<>(initialList != null ? initialList : new ArrayList<>());
    }

    /** 外部调用以更新数据 */
    public void updateData(List<NewsItem> newList) {
        newsList.clear();
        if (newList != null) {
            newsList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_news.xml 请确保有 txtNewsTitle 和 viewDivider 两个控件
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsList.get(position);
        holder.txtTitle.setText(item.title);
        // 标题最多两行，末尾加省略
        holder.txtTitle.setMaxLines(2);
        holder.txtTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);

        // 点击打开浏览器
        holder.itemView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
            context.startActivity(browserIntent);
        });

        // 最后一条不显示分割线
        if (position == newsList.size() - 1) {
            holder.viewDivider.setVisibility(View.GONE);
        } else {
            holder.viewDivider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        View viewDivider;

        NewsViewHolder(View itemView) {
            super(itemView);
            txtTitle    = itemView.findViewById(R.id.txtNewsTitle);
            viewDivider = itemView.findViewById(R.id.viewDivider);
        }
    }
}
