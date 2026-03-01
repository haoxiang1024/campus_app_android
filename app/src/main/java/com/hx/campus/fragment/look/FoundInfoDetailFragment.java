package com.hx.campus.fragment.look;

import android.content.Context;
import android.content.res.ColorStateList;
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
import android.widget.AdapterView; // 新增：Spinner监听器需要的包

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.adapter.comment.CommentAdapter;
import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentFoundInfoDetailBinding;
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
public class FoundInfoDetailFragment extends BaseFragment<FragmentFoundInfoDetailBinding> {

    public static final String KEY_FOUND = "found";
    LostFound found;
    private CommentAdapter commentAdapter;
    private int currentParentId = 0;      // 0 代表直接评论失物招领帖子
    private int currentReplyUserId = 0;   // 0 代表没有回复特定的人

    @Override
    protected void initArgs() {
        super.initArgs();
        if (getArguments() != null) {
            found = (LostFound) getArguments().getSerializable(KEY_FOUND);
        }
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.found_info_detail);
    }

    @NonNull
    @Override
    protected FragmentFoundInfoDetailBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentFoundInfoDetailBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        if (found != null) {
            setViews();
            // 初始化Spinner监听器
            initSpinnerListener();
            // 初始化时同步按钮状态
            updateSubmitBtnStatus(binding.state.getSelectedItem().toString());

            if (binding.sumbitBtn != null) {
                binding.sumbitBtn.setOnClickListener(v -> {
                    String selected = binding.state.getSelectedItem().toString();
                    submitState(selected);
                });
            }
        } else {
            Log.e("Check", "错误：found 数据为空");
        }
        initCommentList();  // 初始化评论列表
        initCommentEvent(); // 初始化发送评论事件
        initEmojiPanel();
    }

    /**
     * 新增：初始化Spinner选中状态监听器
     */
    private void initSpinnerListener() {
        binding.state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 获取选中的状态文本
                String selectedState = parent.getItemAtPosition(position).toString();
                // 更新按钮状态
                updateSubmitBtnStatus(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 未选中任何项时，默认禁用按钮
                if (binding.sumbitBtn != null) {
                    binding.sumbitBtn.setEnabled(false);
                    binding.sumbitBtn.setBackgroundColor(Color.parseColor("#CCCCCC")); // 置灰背景
                }
            }
        });
    }

    /**
     * 根据选中状态更新按钮可用状态
     * @param selectedState 选中的状态文本
     */
    private void updateSubmitBtnStatus(String selectedState) {
        if (binding.sumbitBtn == null) return;

        boolean isDisabled = "待审核".equals(selectedState) || "已驳回".equals(selectedState);

        // 设置按钮可用状态
        binding.sumbitBtn.setEnabled(!isDisabled);

        // 视觉区分：设置按钮背景色
        if (isDisabled) {
            binding.sumbitBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));        } else {
            int primaryColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary);
            binding.sumbitBtn.setBackgroundTintList(ColorStateList.valueOf(primaryColor));        }
    }

    /**
     * 发起网络请求获取评论
     */
    private void loadComments() {
        if (found == null) return;

        RetrofitClient.getInstance().getApi().getComments(found.getId()).enqueue(new Callback<Result<List<Comment>>>() {
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
            submitComment(found.getId(), currentUserId, content, currentParentId, currentReplyUserId);
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

    private void setViews() {
        binding.tvLostTitle.setText(found.getTitle());
        binding.tvLostContent.setText(found.getContent());
        if (TextUtils.isEmpty(found.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(found.getImg()).into(binding.imgLost);
        }
        binding.tvAuthor.setText(found.getNickname());
        binding.tvPhonenum.setText(found.getPhone());
        binding.location.setText(found.getPlace());
        String[] statuses = {"待审核","已驳回","已认领", "待认领"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.state.setAdapter(adapter);
        int position = Arrays.asList(statuses).indexOf(found.getState());
        if (position >= 0) binding.state.setSelection(position);
        binding.tvDate.setText(Utils.dateFormat(found.getPubDate()));
    }

    private void submitState(String selectedState) {
        RetrofitClient.getInstance().getApi()
                .updateState(found.getId(), selectedState, found.getUserId())
                .enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            found.setState(selectedState);
                            Utils.showResponse("状态已更改");
                        } else {
                            String errorMsg = (response.body() != null) ? response.body().getMsg() : "返回体为空";
                            Utils.showResponse("操作失败: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        Utils.showResponse("网络异常");
                    }
                });
    }
}