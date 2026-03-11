package com.hx.campus.fragment.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.Message; // 假设你的留言实体类名是 Message
import com.hx.campus.adapter.entity.MessageVO;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page(name = "我的留言")
public class MyMessagesFragment extends BaseFragment<LayoutCommonListBinding> {

    private BaseRecyclerAdapter<Message> mAdapter;
    private List<Message> mDataList = new ArrayList<>();
    private User user;

    @NonNull
    @Override
    protected LayoutCommonListBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return LayoutCommonListBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        // 动态修改复用布局中的空状态文案
        binding.tvApp.setText("暂无留言消息");

        WidgetUtils.initRecyclerView(binding.recyclerView);
        user = Utils.getBeanFromSp(getContext(), "User", "user");
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化适配器
        mAdapter = new BaseRecyclerAdapter<Message>(mDataList) {
            @Override
            protected int getItemLayoutId(int viewType) {
                // TODO: 确保你创建了这个 item 的 XML 布局文件
                return R.layout.adapter_my_comment_item;
            }

            @Override
            protected void bindData(@NonNull RecyclerViewHolder holder, int position, Message item) {
                if (user != null) {
                    holder.text(R.id.tv_nickname, user.getNickname() != null ? user.getNickname() : "校友");
                    com.bumptech.glide.Glide.with(getContext())
                            .load(user.getPhoto())
                            .placeholder(R.drawable.default_avatar)
                            .circleCrop()
                            .into((android.widget.ImageView) holder.findViewById(R.id.iv_avatar));
                }
                holder.text(R.id.tv_comment_content, item.getContent() != null ? item.getContent() : "");
                holder.text(R.id.tv_time, item.getCreateTime() != null ? Utils.formatCommentTime(String.valueOf(item.getCreateTime())) : "");
            }
        };

        binding.recyclerView.setAdapter(mAdapter);

        // 加载留言数据
        loadData();
    }

    @Override
    protected void initListeners() {
        // 点击留言（如果留言不需要跳转详情，可以只弹个 Toast 或者留空）
        mAdapter.setOnItemClickListener((itemView, item, position) -> {
            Utils.showResponse("查看留言详情");
            // 如果留言也关联了具体的失物招领帖子，可以参考评论模块实现 jumpDetail(item)
        });

        // 【长按】删除该留言
        mAdapter.setOnItemLongClickListener((itemView, item, position) -> {
            new MaterialDialog.Builder(getContext())
                    .title("提示")
                    .content("确定要删除这条留言吗？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive((dialog, which) -> {
                        // 调用删除接口
                        deleteMessageApi(item.getId(), position);
                    })
                    .show();
        });
    }

    private void loadData() {
        mDataList.clear();
        mAdapter.refresh(mDataList);
        checkEmptyState();

        if (user == null || user.getId() == null) {
            Utils.showResponse("用户数据为空");
            return;
        }

        // 调用获取留言的后端接口
        RetrofitClient.getInstance().getApi().getMessagesByUserId(user.getId()).enqueue(new Callback<Result<List<MessageVO>>>() {
            @Override
            public void onResponse(Call<Result<List<MessageVO>>> call, Response<Result<List<MessageVO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MessageVO> newData = response.body().getData();
                    if (newData != null && !newData.isEmpty()) {
                        mDataList.addAll(newData);
                        mAdapter.refresh(mDataList);
                    } else {
                        Utils.showResponse("暂无留言");
                    }
                } else {
                    Utils.showResponse("请求失败");
                }
                checkEmptyState();
            }

            @Override
            public void onFailure(Call<Result<List<MessageVO>>> call, Throwable t) {
                Utils.showResponse("网络请求失败");
                t.printStackTrace();
                checkEmptyState();
            }
        });
    }

    /**
     * 删除留言接口调用
     * @param messageId 留言ID
     * @param position 列表位置
     */
    private void deleteMessageApi(Integer messageId, int position) {
        if (messageId == null) {
            Utils.showResponse("出错了，未获取到留言ID");
            return;
        }

        RetrofitClient.getInstance().getApi().deleteMessage(messageId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    mDataList.remove(position);
                    mAdapter.refresh(mDataList);
                    Utils.showResponse("删除成功");
                } else {
                    Utils.showResponse("删除失败");
                }
                checkEmptyState();
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Utils.showResponse("网络请求失败");
            }
        });
    }

    /**
     * 根据数据量切换空状态和列表的显示
     */
    private void checkEmptyState() {
        if (mAdapter.getItemCount() == 0) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
