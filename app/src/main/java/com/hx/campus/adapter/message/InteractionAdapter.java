package com.hx.campus.adapter.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.InteractionMsg;
import com.hx.campus.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class InteractionAdapter extends RecyclerView.Adapter<InteractionAdapter.ViewHolder> {

    private List<InteractionMsg> dataList = new ArrayList<>();
    // 定义点击回调接口
    public interface OnItemClickListener {
        void onReplyClick(InteractionMsg msg);
    }
    private OnItemClickListener mListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
    // 更新数据的方法
    public void setData(List<InteractionMsg> list) {
        this.dataList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_interaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InteractionMsg msg = dataList.get(position);

        holder.tvUsername.setText(msg.username);
        holder.tvContent.setText(msg.content);
        holder.tvTime.setText(Utils.formatCommentTime(String.valueOf(msg.getTime())));

        // TODO: 使用 Glide 等图片加载库加载头像
         Glide.with(holder.itemView.getContext()).load(msg.avatarUrl).into(holder.ivAvatar);
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onReplyClick(msg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    // ViewHolder 缓存视图
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvContent;
        TextView tvTime;
         ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
             ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}