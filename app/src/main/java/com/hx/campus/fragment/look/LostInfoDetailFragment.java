package com.hx.campus.fragment.look;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
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
import com.hx.campus.databinding.FragmentLostInfoDetailBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class LostInfoDetailFragment extends BaseFragment<FragmentLostInfoDetailBinding> {

    public static final String KEY_LOST = "lost";

    LostFound lost; // 实体类改为 LostFound
    private CommentAdapter commentAdapter;
    private int currentParentId = 0;      // 0 代表直接评论失物招领帖子
    private int currentReplyUserId = 0;   // 0 代表没有回复特定的人
    @Override
    protected void initArgs() {
        super.initArgs();
       // XRouter.getInstance().inject(this);
        if (getArguments() != null) {
            lost = (LostFound) getArguments().getSerializable(KEY_LOST);
        }
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.lost_info_detail);
    }

    @NonNull
    @Override
    protected FragmentLostInfoDetailBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentLostInfoDetailBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        if (lost != null) {
            setViews();
            if (binding.sumbitBtn != null) {
                binding.sumbitBtn.setOnClickListener(v -> {
                    String selected = binding.state.getSelectedItem().toString();
                    submitState(selected);
                });
            }
        }else {
            Log.e("Check", "错误：lost 数据为空");
        }
        initCommentList();  // 初始化评论列表
        initCommentEvent(); // 初始化发送评论事件
        initEmojiPanel();
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
                    if (serverResponse.getStatus() == 0) {
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


    private void setViews() {
        binding.tvLostTitle.setText(lost.getTitle());
        binding.tvLostContent.setText(lost.getContent());
        if (TextUtils.isEmpty(lost.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(lost.getImg()).into(binding.imgLost);
        }
        binding.tvAuthor.setText(lost.getNickname());
        binding.tvPhonenum.setText(lost.getPhone());
        binding.location.setText(lost.getPlace());

        // 设置状态
        String[] statuses = {"已找到", "寻找中"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.state.setAdapter(adapter);

        int position = Arrays.asList(statuses).indexOf(lost.getState());
        if (position >= 0) binding.state.setSelection(position);
        binding.tvDate.setText(Utils.dateFormat(lost.getPubDate()));
    }

    private void submitState(String selectedState) {
        // 使用 Retrofit 统一更新状态接口
        RetrofitClient.getInstance().getApi()
                .updateState(lost.getId(), selectedState, lost.getUserId())
                .enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            lost.setState(selectedState);
                            Utils.showResponse("状态已更改");
                        } else {
                            Utils.showResponse("操作失败");
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        Utils.showResponse("网络异常");
                    }
                });
    }
}