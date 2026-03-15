package com.hx.campus.adapter.shop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.PointHistory;
import java.util.ArrayList;
import java.util.List;

public class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.ViewHolder> {

    private List<PointHistory> mData = new ArrayList<>();
    private OnItemClickListener mListener;
    private List<PointHistory> mSourceData = new ArrayList<>(); // 原始数据备份
    public void refresh(List<PointHistory> data) {
        if (data != null) {
            mSourceData.clear();
            mSourceData.addAll(data);
            mData.clear();
            mData.addAll(data);
            notifyDataSetChanged();
        }
    }
    // 模糊搜索过滤逻辑
    public void filter(String query) {
        mData.clear();
        if (query.isEmpty()) {
            mData.addAll(mSourceData);
        } else {
            for (PointHistory item : mSourceData) {
                if (item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    mData.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
    public PointHistory getItem(int position) {
        return mData.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_point_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PointHistory item = mData.get(position);

        holder.tvTitle.setText(item.getDescription());
        holder.tvTime.setText(item.getFormattedTime());
        // 积分增减带符号显示
        if (item.getpoints_changed() > 0) {
            holder.tvPoints.setText("+" + item.getpoints_changed());
            holder.tvPoints.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.picture_color_bfe85d));
        } else {
            holder.tvPoints.setText(String.valueOf(item.getpoints_changed()));
            holder.tvPoints.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        }

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvTime;
        TextView tvPoints;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvPoints = itemView.findViewById(R.id.tv_points);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}