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

   private void showReplyDialog(InteractionMsg msg) {
       new MaterialDialog.Builder(getContext())
               .title("回复 @" + msg.username)
               .inputType(InputType.TYPE_CLASS_TEXT)
               .input("请输入回复内容", "", (dialog, input) -> {
                   String content = input.toString().trim();
                   if (TextUtils.isEmpty(content)) {
                       Utils.showResponse("回复内容不能为空");
                   } else {

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

    private void sendRefreshCommand(int targetUserId, int lostfoundId) {
        String commandData = "REFRESH_COMMENT:" + lostfoundId;
        io.rong.imlib.model.Message content = io.rong.imlib.model.Message.obtain(
                String.valueOf(targetUserId),
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

        loadCommentData();
    }
    private void loadCommentData() {

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

                            List<InteractionMsg> uiList = new ArrayList<>();

                            for (Comment comment : backendComments) {

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
                                if (comment.getParent_id() == 0) {
                                    targetParentId = comment.getId();
                                } else {
                                    targetParentId = comment.getParent_id();
                                }
                                msg.commentId = targetParentId;
                                msg.userId = comment.getUser_id();
                                msg.avatarUrl = comment.getPhoto();

                                uiList.add(msg);
                            }

                            mAdapter.setData(uiList);

                        } else {

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
        mAdapter.setData(new ArrayList<>());
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