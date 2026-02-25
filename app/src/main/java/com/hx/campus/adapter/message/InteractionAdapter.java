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

/**
 * 互动消息适配器
 * 用于展示用户互动消息列表
 * 支持点击事件回调和图片加载功能
 */
public class InteractionAdapter extends RecyclerView.Adapter<InteractionAdapter.ViewHolder> {

    /** 互动消息数据源 */
    private List<InteractionMsg> dataList = new ArrayList<>();
    
    /** 列表项点击事件监听接口 */
    public interface OnItemClickListener {
        /**
         * 回复按钮点击回调
         * 
         * @param msg 被点击的互动消息
         */
        void onReplyClick(InteractionMsg msg);
    }
    
    /** 点击事件监听器 */
    private OnItemClickListener mListener;
    
    /**
     * 设置点击事件监听器
     * 
     * @param listener 点击事件监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
    // 更新数据的方法
    /**
     * 设置数据源
     * 
     * @param list 新的数据列表
     */
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

    /** ViewHolder缓存视图类 */
    static class ViewHolder extends RecyclerView.ViewHolder {
        /** 用户名显示文本 */
        TextView tvUsername;
        /** 内容显示文本 */
        TextView tvContent;
        /** 时间显示文本 */
        TextView tvTime;
        /** 用户头像图片 */
         ImageView ivAvatar;

        /**
         * 构造函数
         * 
         * @param itemView 列表项根视图
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
             ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}