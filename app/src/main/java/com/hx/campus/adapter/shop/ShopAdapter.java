package com.hx.campus.adapter.shop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.ShopItem;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    private List<ShopItem> mData = new ArrayList<>();
    private OnExchangeClickListener mListener;

    public void setData(List<ShopItem> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setOnExchangeClickListener(OnExchangeClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_shop_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopItem item = mData.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPoints.setText(item.getRequired_points() + " 积分");

        // 如果有图片链接，用 Glide 加载
         Glide.with(holder.itemView.getContext()).load(item.getImage_url()).into(holder.ivImage);

        holder.btnExchange.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onExchangeClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvPoints;
        View btnExchange;

        ViewHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.iv_item_image);
            tvName = view.findViewById(R.id.tv_item_name);
            tvPoints = view.findViewById(R.id.tv_item_points);
            btnExchange = view.findViewById(R.id.btn_exchange);
        }
    }

    public interface OnExchangeClickListener {
        void onExchangeClick(ShopItem item);
    }
}
