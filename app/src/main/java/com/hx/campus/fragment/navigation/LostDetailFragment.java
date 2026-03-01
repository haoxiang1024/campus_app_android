package com.hx.campus.fragment.navigation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
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
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLostDetailBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class LostDetailFragment extends BaseFragment<FragmentLostDetailBinding> {
    public static final String KEY_LOST = "lost";
    private CommentAdapter commentAdapter;

    LostFound lost;

    private int currentParentId = 0;      // 0 代表直接评论失物帖子
    private int currentReplyUserId = 0;   // 0 代表没有回复特定的人

    /**
     * 初始化参数
     */
    @Override
    protected void initArgs() {
        super.initArgs();
        if (getArguments() != null) {
            lost = (LostFound) getArguments().getSerializable(KEY_LOST);
        }
        initIMListener();
    }

    /**
     * 获取页面标题
     */
    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.detail);
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
    protected FragmentLostDetailBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentLostDetailBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        setViews();         //设置控件
        initCommentList();  // 初始化评论列表
        initCommentEvent(); // 初始化发送评论事件
        initEmojiPanel();   // 初始化表情面板

    }

    private void initEmojiPanel() {
        // 常用的自带 Emoji 列表
        String[] emojis = {
                "😀","😂","🤣","😅","😊","😍","😘","😜",
                "😝","🤩","😔","😢","😭","😡","🤯","👍",
                "👎","🙏","🤝","👏","🔥","💯","❤️","💔"
        };

        // 动态创建一个简单的网格布局放表情
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
                // 点击表情，直接插入到输入框当前光标位置
                int cursor = binding.etCommentInput.getSelectionStart();
                binding.etCommentInput.getText().insert(cursor, emoji);
            });
            gridLayout.addView(tv);
        }

        // 用 PopupWindow 包装这个面板
        PopupWindow emojiPopup = new PopupWindow(gridLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true); // true 允许点击外部消失
        emojiPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emojiPopup.setOutsideTouchable(true);

        // 点击表情按钮弹出
        binding.btnEmoji.setOnClickListener(v -> {
            // 隐藏软键盘
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);

            // 在输入框上方或下方弹出
            emojiPopup.showAsDropDown(binding.btnEmoji, 0, - (binding.btnEmoji.getHeight() + 400));
        });

        // 当用户点击输入框时，如果表情面板开着就把它关掉
        binding.etCommentInput.setOnClickListener(v -> {
            if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
    }

    /**
     * 初始化评论列表和 RecyclerView
     */
    private void initCommentList() {
        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvComments.setAdapter(commentAdapter);

        commentAdapter.setOnCommentClickListener((parentId, targetUserId, targetNickname) -> {
            currentParentId = parentId;
            currentReplyUserId = targetUserId;
            binding.etCommentInput.setHint("回复 " + targetNickname + "...");
            binding.etCommentInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.etCommentInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // 加载评论数据
        loadComments();
    }

    /**
     * 发起网络请求获取评论
     */
    private void loadComments() {
        if (lost == null) return;
        RetrofitClient.getInstance().getApi().getComments(lost.getId()).enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<Comment>> serverResponse = response.body();
                    if (serverResponse.getStatus() == 0){
                        commentAdapter.setNewData(serverResponse.getData());
                    }

                }
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
                Utils.showResponse("网络异常");
            }

        });
    }

    /**
     * 初始化发送评论按钮的点击事件
     */
    private void initCommentEvent() {
        binding.btnSendComment.setOnClickListener(v -> {
            String content = binding.etCommentInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Utils.showResponse("评论内容不能为空");
                return;
            }

            User user = Utils.getBeanFromSp(getContext(), "User", "user");
            if (user == null) {
                Utils.showResponse("请先登录");
                return;
            }
            int currentUserId = user.getId();
            submitComment(lost.getId(), currentUserId, content, currentParentId, currentReplyUserId);
        });
    }

    /**
     * 提交评论到后端
     */
    private void submitComment(int lostfoundId, int userId, String content, int parentId, int replyUserId) {
        // 禁用按钮防连点
        binding.btnSendComment.setEnabled(false);

        RetrofitClient.getInstance().getApi().addComment(lostfoundId, userId, content, parentId, replyUserId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                binding.btnSendComment.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus() == 0) {
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

                        // 重新加载评论列表刷新 UI
                        loadComments();
                    } else {
                        Utils.showResponse(response.body().getMsg());
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                binding.btnSendComment.setEnabled(true);
                Utils.showResponse("网络异常");
            }
        });
    }

    private void setViews() {
        //设置标题
        binding.tvLostTitle.setText(lost.getTitle());
        //设置内容
        binding.tvLostContent.setText(lost.getContent());
        //加载图片
        if (TextUtils.isEmpty(lost.getImg())) {
            binding.imgLost.setVisibility(View.GONE);

        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(lost.getImg()).into(binding.imgLost);
        }
        //设置失主名称
        binding.tvAuthor.setText(lost.getNickname());
        //设置联系方式
        binding.tvPhonenum.setText(lost.getPhone());
        //设置地点
        binding.location.setText(lost.getPlace());
        //设置状态
        binding.state.setText(lost.getState());
        //设置发布日期
        String date = Utils.dateFormat(lost.getPubDate());
        binding.tvDate.setText(date);
        //私信
        binding.chatBtn.setOnClickListener(v -> {
            String targetId = String.valueOf(lost.getUserId());
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
    // IM消息监听
    private void initIMListener() {
        io.rong.imlib.RongIMClient.setOnReceiveMessageListener(new io.rong.imlib.RongIMClient.OnReceiveMessageWrapperListener() {
            @Override
            public boolean onReceived(io.rong.imlib.model.Message message, int left, boolean hasPackage, boolean offline) {
                if (message.getContent() instanceof io.rong.message.CommandMessage) {
                    io.rong.message.CommandMessage command = (io.rong.message.CommandMessage) message.getContent();

                    if ("RefreshComment".equals(command.getName())) {
                        String data = command.getData(); // 拿到 "REFRESH_COMMENT:123"
                        if (data != null && data.contains(":")) {
                            String id = data.split(":")[1];

                            getActivity().runOnUiThread(() -> {
                                if (lost != null && String.valueOf(lost.getId()).equals(id)) {
                                    loadComments();
                                    Log.d("IM", "收到远程指令，自动刷新评论区");
                                }
                            });
                        }
                    }
                }
                return false; // 返回 false 表示此消息走系统默认处理流程（命令消息默认不显示）
            }
        });
    }
}