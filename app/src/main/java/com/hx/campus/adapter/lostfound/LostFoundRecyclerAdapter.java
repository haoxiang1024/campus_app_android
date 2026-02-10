package com.hx.campus.adapter.lostfound;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.databinding.FoundItemsBinding;
import com.hx.campus.databinding.LostItemsBinding;

import java.util.ArrayList;
import java.util.List;

public class LostFoundRecyclerAdapter extends RecyclerView.Adapter<LostFoundRecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<LostFound> dataList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LostFound lost);
    }

    public LostFoundRecyclerAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<LostFound> data) {
        this.dataList.clear();
        this.dataList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return "0".equals(dataList.get(position).getType()) ? 0 : 1; // 0失物, 1招领
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ViewHolder(LostItemsBinding.inflate(LayoutInflater.from(context), parent, false));
        } else {
            return new ViewHolder(FoundItemsBinding.inflate(LayoutInflater.from(context), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFound item = dataList.get(position);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));

        if (holder.binding instanceof LostItemsBinding) {
            LostItemsBinding b = (LostItemsBinding) holder.binding;
            b.lostTitle.setText(item.getTitle());
            b.authorName.setText(item.getNickname());
            b.tvLostContent.setText(item.getContent());
            loadImage(item.getImg(), b.lostImg);
        } else {
            FoundItemsBinding b = (FoundItemsBinding) holder.binding;
            b.lostTitle.setText(item.getTitle());
            b.authorName.setText(item.getNickname());
            b.tvLostContent.setText(item.getContent());
            loadImage(item.getImg(), b.lostImg);
        }
    }

    private void loadImage(String url, android.widget.ImageView iv) {
        if (TextUtils.isEmpty(url)) {
            iv.setVisibility(android.view.View.GONE);
        } else {
            iv.setVisibility(android.view.View.VISIBLE);
            Glide.with(context).load(url).into(iv);
        }
    }

    @Override
    public int getItemCount() { return dataList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewBinding binding;
        public ViewHolder(@NonNull ViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
