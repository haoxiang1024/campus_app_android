package com.hx.campus.adapter.shop;

import android.graphics.Color;
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
import com.xuexiang.xui.widget.textview.supertextview.SuperButton;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    private List<ShopItem> mData = new ArrayList<>();
    private OnExchangeClickListener mExchangeListener;
    private OnItemClickListener mItemClickListener;
    private int mUserPoints = 0;

    public void setData(List<ShopItem> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setUserPoints(int points) {
        this.mUserPoints = points;
        notifyDataSetChanged();
    }

    public void setOnExchangeClickListener(OnExchangeClickListener listener) {
        this.mExchangeListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
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

        Glide.with(holder.itemView.getContext()).load(item.getImage_url()).into(holder.ivImage);

        if (mUserPoints >= item.getRequired_points()) {
            holder.btnExchange.setShapeSolidColor(Color.parseColor("#0099FF"));
            holder.btnExchange.setEnabled(true);
        } else {
            holder.btnExchange.setShapeSolidColor(Color.parseColor("#B0B0B0"));
            holder.btnExchange.setEnabled(false);
        }
        holder.btnExchange.setUseShape();

        // 兑换按钮点击事件
        holder.btnExchange.setOnClickListener(v -> {
            if (mExchangeListener != null) {
                mExchangeListener.onExchangeClick(item);
            }
        });

        // 整个商品卡片点击事件
        holder.itemView.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(item);
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
        SuperButton btnExchange;

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

    public interface OnItemClickListener {
        void onItemClick(ShopItem item);
    }
}