package com.hx.campus.adapter.comment;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> mList = new ArrayList<>();
    private Context mContext;
    private OnCommentClickListener listener;

    private SparseBooleanArray expandStateArray = new SparseBooleanArray();
    private static final int MAX_SHOW_REPLIES = 2; // 默认最多显示几条回复

    public interface OnCommentClickListener {
        void onReplyClick(int parentId, int targetUserId, String targetNickname);
    }

    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.listener = listener;
    }

    public void setNewData(List<Comment> list) {
        this.mList = list == null ? new ArrayList<>() : list;
        expandStateArray.clear(); // 刷新数据时重置展开状态
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = mList.get(position);

        holder.tvName.setText(TextUtils.isEmpty(comment.getNickname()) ? "匿名用户" : comment.getNickname());
        holder.tvContent.setText(TextUtils.isEmpty(comment.getContent()) ? "" : comment.getContent());

        if (comment.getCreate_time() != null) {
            holder.tvTime.setText(Utils.formatCommentTime(String.valueOf(comment.getCreate_time())));
        }
        if (!TextUtils.isEmpty(comment.getPhoto())) {
            Glide.with(mContext).load(comment.getPhoto()).apply(RequestOptions.circleCropTransform()).into(holder.ivAvatar);
        } else {
            Glide.with(mContext).load(R.color.design_default_color_background).apply(RequestOptions.circleCropTransform()).into(holder.ivAvatar);
        }

        holder.llReplyContainer.removeAllViews();
        List<Comment> replies = comment.getReplies();

        if (replies != null && !replies.isEmpty()) {
            holder.llReplyContainer.setVisibility(View.VISIBLE);

            // 判断当前一楼是否处于“展开”状态
            boolean isExpanded = expandStateArray.get(comment.getId(), false);
            // 计算需要显示的回复数量
            int showCount = isExpanded ? replies.size() : Math.min(replies.size(), MAX_SHOW_REPLIES);

            for (int i = 0; i < showCount; i++) {
                Comment reply = replies.get(i);
                LinearLayout replyRow = new LinearLayout(mContext);
                replyRow.setOrientation(LinearLayout.HORIZONTAL);
                replyRow.setGravity(Gravity.TOP);
                replyRow.setPadding(0, dp2px(4), 0, dp2px(4));
                ImageView replyAvatar = new ImageView(mContext);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp2px(20), dp2px(20));
                avatarParams.setMargins(0, dp2px(1), dp2px(8), 0);
                replyAvatar.setLayoutParams(avatarParams);

                if (!TextUtils.isEmpty(reply.getPhoto())) {
                    Glide.with(mContext).load(reply.getPhoto()).apply(RequestOptions.circleCropTransform()).into(replyAvatar);
                } else {
                    Glide.with(mContext).load(R.color.design_default_color_background).apply(RequestOptions.circleCropTransform()).into(replyAvatar);
                }
                LinearLayout textContainer = new LinearLayout(mContext);
                textContainer.setOrientation(LinearLayout.VERTICAL);
                textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

                TextView replyTv = new TextView(mContext);
                replyTv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                replyTv.setTextSize(14);
                replyTv.setLineSpacing(0, 1.2f);
                String namePart;
                if (TextUtils.isEmpty(reply.getReplyNickname())) {
                    namePart = reply.getNickname();
                } else {
                    namePart = reply.getNickname() + " ▸ " + reply.getReplyNickname();
                }

                String htmlText = "<font color='#888888'>" + namePart + "：</font> <font color='#222222'>" + reply.getContent() + "</font>";
                replyTv.setText(Html.fromHtml(htmlText));

                // --- 新增：底部操作栏容器（包含时间和回复按钮） ---
                LinearLayout actionContainer = new LinearLayout(mContext);
                actionContainer.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams actionContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                actionContainerParams.topMargin = dp2px(2);
                actionContainer.setLayoutParams(actionContainerParams);
                actionContainer.setGravity(Gravity.CENTER_VERTICAL); // 垂直居中对齐

                // 1. 添加时间 TextView
                TextView replyTimeTv = new TextView(mContext);
                // 权重设为1，将其撑开，这样能把右侧的“回复”按钮挤到最右边
                LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                replyTimeTv.setLayoutParams(timeParams);
                replyTimeTv.setTextSize(12);
                replyTimeTv.setTextColor(android.graphics.Color.parseColor("#999999")); // 使用浅灰色显示时间
                if (reply.getCreate_time() != null) {
                    replyTimeTv.setText(Utils.formatCommentTime(String.valueOf(reply.getCreate_time())));
                }

                // 2. 原有的回复按钮
                TextView replyActionBtn = new TextView(mContext);
                LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                replyActionBtn.setLayoutParams(actionParams);
                replyActionBtn.setText("回复");
                replyActionBtn.setTextSize(12);
                replyActionBtn.setTextColor(android.graphics.Color.parseColor("#555555"));
                replyActionBtn.setPadding(dp2px(8), dp2px(2), 0, dp2px(2));

                // 将时间和按钮加入水平容器
                actionContainer.addView(replyTimeTv);
                actionContainer.addView(replyActionBtn);
                // --------------------------------------------------

                // 将内容和底部操作栏加入垂直容器
                textContainer.addView(replyTv);
                textContainer.addView(actionContainer);

                // 将头像和右侧垂直容器加入整行
                replyRow.addView(replyAvatar);
                replyRow.addView(textContainer);

                replyActionBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onReplyClick(comment.getId(), reply.getUser_id(), reply.getNickname());
                });

                holder.llReplyContainer.addView(replyRow);
            }

            if (replies.size() > MAX_SHOW_REPLIES) {
                holder.tvExpandReplies.setVisibility(View.VISIBLE);
                if (isExpanded) {
                    holder.tvExpandReplies.setText("— 收起 ∧");
                    holder.tvExpandReplies.setOnClickListener(v -> {
                        expandStateArray.put(comment.getId(), false);
                        notifyItemChanged(position); // 刷新当前条目
                    });
                } else {
                    int leftCount = replies.size() - MAX_SHOW_REPLIES;
                    holder.tvExpandReplies.setText("— 展开" + leftCount + "条回复 ∨");
                    holder.tvExpandReplies.setOnClickListener(v -> {
                        expandStateArray.put(comment.getId(), true);
                        notifyItemChanged(position); // 刷新当前条目
                    });
                }
            } else {
                holder.tvExpandReplies.setVisibility(View.GONE);
            }

        } else {
            holder.llReplyContainer.setVisibility(View.GONE);
            holder.tvExpandReplies.setVisibility(View.GONE);
        }

        holder.tvReplyBtn.setOnClickListener(v -> {
            if (listener != null) listener.onReplyClick(comment.getId(), comment.getUser_id(), comment.getNickname());
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReplyClick(comment.getId(), comment.getUser_id(), comment.getNickname());
        });
    }
    @Override
    public int getItemCount() { return mList.size(); }

    private int dp2px(float dpValue) {
        return (int) (dpValue * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvContent, tvTime, tvReplyBtn, tvExpandReplies;
        LinearLayout llReplyContainer;

        ViewHolder(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.iv_comment_avatar);
            tvName = view.findViewById(R.id.tv_comment_author);
            tvContent = view.findViewById(R.id.tv_comment_content);
            tvTime = view.findViewById(R.id.tv_comment_time);
            tvReplyBtn = view.findViewById(R.id.tv_reply_btn);
            llReplyContainer = view.findViewById(R.id.ll_reply_container);
            tvExpandReplies = view.findViewById(R.id.tv_expand_replies); // 绑定展开按钮
        }
    }
}