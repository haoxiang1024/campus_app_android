package com.hx.campus.fragment.other;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.adapter.comment.CommentAdapter;
import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.adapter.entity.SearchInfo;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentSearchInfoBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.launcher.XRouter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page(name = "详情")
public class SearchInfoFragment extends BaseFragment<FragmentSearchInfoBinding> {

    public static final String KEY_INFO = "info";

    @AutoWired(name = KEY_INFO)
    SearchInfo searchInfo;//实体类不能序列化，否则无法注入

    private CommentAdapter commentAdapter;
    // 评论回复相关参数：0 代表直接评论帖子，非0代表回复对应ID的评论/用户
    private int currentParentId = 0;
    private int currentReplyUserId = 0;
    private PopupWindow emojiPopup; // Emoji面板弹窗

    /**
     * 初始化参数
     */
    @Override
    protected void initArgs() {
        super.initArgs();
        XRouter.getInstance().inject(this);
    }

    /**
     * 构建ViewBinding
     *
     * @param inflater  inflater
     * @param container 容器
     * @return ViewBinding
     */
    @NonNull
    @Override
    protected FragmentSearchInfoBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentSearchInfoBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        setData();
        initCommentList();  // 初始化评论列表
        initCommentEvent(); // 初始化评论发送事件
        initEmojiPanel();   // 初始化Emoji面板
    }

    /**
     * 初始化Emoji面板
     */
    private void initEmojiPanel() {
        // 常用Emoji列表
        String[] emojis = {
                "😀","😂","🤣","😅","😊","😍","😘","😜",
                "😝","🤩","😔","😢","😭","😡","🤯","👍",
                "👎","🙏","🤝","👏","🔥","💯","❤️","💔"
        };

        // 构建Emoji网格布局
        GridLayout gridLayout = new GridLayout(getContext());
        gridLayout.setColumnCount(8); // 每行8个表情
        gridLayout.setBackgroundColor(Color.parseColor("#F5F6F9"));
        gridLayout.setPadding(16, 16, 16, 16);

        for (String emoji : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(emoji);
            tv.setTextSize(26);
            tv.setPadding(12, 12, 12, 12);
            tv.setOnClickListener(v -> {
                // 插入Emoji到输入框光标位置
                int cursor = binding.etCommentInput.getSelectionStart();
                binding.etCommentInput.getText().insert(cursor, emoji);
            });
            gridLayout.addView(tv);
        }

        // 包装成PopupWindow
        emojiPopup = new PopupWindow(gridLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        emojiPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emojiPopup.setOutsideTouchable(true);

        // Emoji按钮点击事件
        binding.btnEmoji.setOnClickListener(v -> {
            // 隐藏软键盘
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);

            // 弹出Emoji面板
            emojiPopup.showAsDropDown(binding.btnEmoji, 0, - (binding.btnEmoji.getHeight() + 400));
        });

        // 输入框点击关闭Emoji面板
        binding.etCommentInput.setOnClickListener(v -> {
            if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
    }

    /**
     * 初始化评论列表
     */
    private void initCommentList() {
        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvComments.setAdapter(commentAdapter);

        // 评论回复点击事件
        commentAdapter.setOnCommentClickListener((parentId, targetUserId, targetNickname) -> {
            currentParentId = parentId;
            currentReplyUserId = targetUserId;
            binding.etCommentInput.setHint("回复 " + targetNickname + "...");
            binding.etCommentInput.requestFocus();
            // 弹出软键盘
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.etCommentInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // 加载评论数据
        loadComments();
    }

    /**
     * 加载评论数据
     */
    private void loadComments() {
        if (searchInfo == null || searchInfo.getId() == 0) return;

        RetrofitClient.getInstance().getApi().getComments(searchInfo.getId()).enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<Comment>> serverResponse = response.body();
                    if (serverResponse.getStatus() == 0) {
                        commentAdapter.setNewData(serverResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
                Utils.showResponse("网络异常，加载评论失败");
            }
        });
    }

    /**
     * 初始化评论发送事件
     */
    private void initCommentEvent() {
        binding.btnSendComment.setOnClickListener(v -> {
            String content = binding.etCommentInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Utils.showResponse("评论内容不能为空");
                return;
            }

            // 获取当前登录用户
            User user = Utils.getBeanFromSp(getContext(), "User", "user");
            if (user == null) {
                Utils.showResponse("请先登录");
                return;
            }

            // 提交评论
            submitComment(searchInfo.getId(), user.getId(), content, currentParentId, currentReplyUserId);
        });
    }

    /**
     * 提交评论到后端
     */
    private void submitComment(int searchInfoId, int userId, String content, int parentId, int replyUserId) {
        // 禁用按钮防重复点击
        binding.btnSendComment.setEnabled(false);

        RetrofitClient.getInstance().getApi()
                .addComment(searchInfoId, userId, content, parentId, replyUserId)
                .enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        binding.btnSendComment.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            Result<String> result = response.body();
                            if (result.getStatus() == 0) {
                                // 清空输入框、重置回复状态
                                binding.etCommentInput.setText("");
                                binding.etCommentInput.clearFocus();
                                binding.etCommentInput.setHint("写下你的评论...");
                                currentParentId = 0;
                                currentReplyUserId = 0;

                                // 隐藏软键盘
                                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);
                                }

                                // 刷新评论列表
                                loadComments();
                                Utils.showResponse("评论发布成功");
                            } else {
                                Utils.showResponse(result.getMsg());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        binding.btnSendComment.setEnabled(true);
                        Utils.showResponse("网络异常，发布评论失败");
                    }
                });
    }

    /**
     * 填充基础数据
     */
    private void setData() {
        if (searchInfo == null) return;

        //设置标题
        binding.tvLostTitle.setText(searchInfo.getTitle());
        //设置内容
        binding.tvLostContent.setText(searchInfo.getContent());
        //加载图片
        if (TextUtils.isEmpty(searchInfo.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(searchInfo.getImg()).into(binding.imgLost);
        }
        //设置失主名称
        binding.tvAuthor.setText(searchInfo.getNickname());
        //设置联系方式
        binding.tvPhonenum.setText(searchInfo.getPhone());
        //设置地点
        binding.location.setText(searchInfo.getPlace());
        //设置状态
        binding.state.setText(searchInfo.getState());
        //设置发布日期
        String date = Utils.dateFormat(searchInfo.getPub_date());
        binding.tvDate.setText(date);
        //私信
        binding.chatBtn.setOnClickListener(v -> {
            String targetId = String.valueOf(searchInfo.getUser_id());
            if (TextUtils.isEmpty(targetId)) {
                return;
            }
            io.rong.imkit.utils.RouteUtils.routeToConversationActivity(
                    getContext(),
                    io.rong.imlib.model.Conversation.ConversationType.PRIVATE,
                    targetId
            );
        });
    }
}