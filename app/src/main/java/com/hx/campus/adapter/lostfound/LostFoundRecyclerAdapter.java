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

/**
 * 失物招领RecyclerView适配器
 * 使用RecyclerView实现失物和招领信息的列表展示
 * 支持点击事件回调和ViewBinding绑定
 */
public class LostFoundRecyclerAdapter extends RecyclerView.Adapter<LostFoundRecyclerAdapter.ViewHolder> {
    /** 上下文环境 */
    private final Context context;
    /** 数据源列表 */
    private final List<LostFound> dataList = new ArrayList<>();
    /** 列表项点击监听器 */
    private OnItemClickListener listener;

    /** 列表项点击事件监听接口 */
    public interface OnItemClickListener {
        /**
         * 列表项点击回调
         * 
         * @param lost 被点击的失物招领信息
         */
        void onItemClick(LostFound lost);
    }

    /**
     * 构造函数
     * 
     * @param context 上下文环境
     * @param listener 点击事件监听器
     */
    public LostFoundRecyclerAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * 设置数据源
     * 
     * @param data 新的数据列表
     */
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

    /**
     * 加载图片到ImageView
     * 
     * @param url 图片URL地址
     * @param iv 目标ImageView控件
     */
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

    /** ViewHolder内部类 */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** ViewBinding实例 */
        ViewBinding binding;
        
        /**
         * 构造函数
         * 
         * @param binding ViewBinding实例
         */
        public ViewHolder(@NonNull ViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
