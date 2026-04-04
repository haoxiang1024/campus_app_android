package com.hx.campus.fragment.message;


import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.adapter.entity.InteractionMsg;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.message.InteractionAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//评论页
@Page
public class InteractionFragment extends BaseFragment<LayoutCommonListBinding> {

    private InteractionAdapter mAdapter;

    @NonNull
    @Override
    protected LayoutCommonListBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return LayoutCommonListBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new InteractionAdapter();
        binding.recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(msg -> showReplyDialog(msg));
        loadCommentData();
    }
   // 弹出回复对话框
   private void showReplyDialog(InteractionMsg msg) {
       new MaterialDialog.Builder(getContext())
               .title("回复 @" + msg.username)
               .inputType(InputType.TYPE_CLASS_TEXT)
               .input("请输入回复内容", "", (dialog, input) -> {
                   String content = input.toString().trim();
                   if (TextUtils.isEmpty(content)) {
                       Utils.showResponse("回复内容不能为空");
                   } else {
                       // 触发发送请求
                       sendReplyToServer(msg, content);
                   }
               })
               .positiveText("发送")
               .negativeText("取消")
               .show();
   }

    private void sendReplyToServer(InteractionMsg msg, String replyContent) {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) {
            Utils.showResponse("请先登录");
            return;
        }

        int currentUserId = user.getId();
        RetrofitClient.getInstance().getApi().addComment(
                msg.lostfoundId,
                currentUserId,
                replyContent,
                msg.commentId,
                msg.userId
        ).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus() == 0) {
                        Utils.showResponse("回复成功");
                        sendRefreshCommand(msg.userId,msg.lostfoundId);
                        loadCommentData();
                    } else {
                        Utils.showResponse("回复失败：" + response.body().getMsg());
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Utils.showResponse("网络异常");
            }
        });
    }
    // 发送消息通知评论区更新
    private void sendRefreshCommand(int targetUserId, int lostfoundId) {
        String commandData = "REFRESH_COMMENT:" + lostfoundId;
        io.rong.imlib.model.Message content = io.rong.imlib.model.Message.obtain(
                String.valueOf(targetUserId), // 接收者的用户ID
                io.rong.imlib.model.Conversation.ConversationType.PRIVATE,
                io.rong.message.CommandMessage.obtain("RefreshComment", commandData)
        );
        io.rong.imlib.RongIMClient.getInstance().sendMessage(content, null, null,
                new IRongCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {
                        Log.e("IM", "命令消息发送成功");
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                        Log.e("IM", "命令消息发送失败: " + errorCode);
                    }
                });
    }
    @Override
    public void onResume() {
        super.onResume();
        // 每次页面可见时，重新请求一次最新数据
        loadCommentData();
    }
    private void loadCommentData() {
        //获取登录用户信息
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if(user==null)return;
        RetrofitClient.getInstance().getApi().getReceivedComments(user.getId()).enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<Comment>> serverResponse = response.body();
                    if (serverResponse.isSuccess()) {
                        List<Comment> backendComments = serverResponse.getData();
                        if (backendComments != null && !backendComments.isEmpty()) {
                            hideEmptyView();
                            // 准备一个新集合，用来装转换后的 UI 数据
                            List<InteractionMsg> uiList = new ArrayList<>();
                            // 遍历后端数据，转换为前端 Adapter 需要的数据
                            for (Comment comment : backendComments) {
                                // 格式化时间
                                String timeStr = "";
                                if (comment.getCreate_time() != null) {
                                    timeStr = Utils.formatCommentTime(String.valueOf(comment.getCreate_time()));
                                }

                                InteractionMsg msg = new InteractionMsg(
                                        comment.getNickname(),
                                        comment.getContent(),
                                        timeStr
                                );
                                msg.lostfoundId = comment.getLostfound_id();
                                int targetParentId = 0;
                                if (comment.getParentId() == 0) {
                                    targetParentId = comment.getId();
                                } else {
                                    targetParentId = comment.getParentId();
                                }
                                msg.commentId = targetParentId;
                                msg.userId = comment.getUser_id();
                                msg.avatarUrl = comment.getPhoto();

                                uiList.add(msg);
                            }
                            // 将转换好的数据传给 Adapter，刷新列表
                            mAdapter.setData(uiList);

                        } else {
                            // 数据为空时的处理
                            showEmptyView();
                        }
                    } else {
                        Utils.showResponse("查询失败");
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
                Utils.showResponse("网络异常");

            }
        });

    }

    private void showEmptyView() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        mAdapter.setData(new ArrayList<>()); // 确保列表清空
    }
    private void hideEmptyView() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    protected TitleBar initTitle() {
        return null;
    }
}