package com.hx.campus.fragment.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.hx.campus.fragment.navigation.FoundDetailFragment;
import com.hx.campus.fragment.navigation.LostDetailFragment;
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

@Page(name = "我的评论")
public class MyCommentsFragment extends BaseFragment<LayoutCommonListBinding> {

    private BaseRecyclerAdapter<Comment> mAdapter;
    private List<Comment> mDataList = new ArrayList<>();
    private User user;

    @NonNull
    @Override
    protected LayoutCommonListBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return LayoutCommonListBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        // 设置 RecyclerView 布局管理器
        WidgetUtils.initRecyclerView(binding.recyclerView);
        user = Utils.getBeanFromSp(getContext(), "User", "user");
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 初始化适配器
        mAdapter = new BaseRecyclerAdapter<Comment>(mDataList) {
            @Override
            protected int getItemLayoutId(int viewType) {
                return R.layout.adapter_my_comment_item;
            }

            @Override
            protected void bindData(@NonNull RecyclerViewHolder holder, int position, Comment item) {
                if (user != null) {
                    holder.text(R.id.tv_nickname, user.getNickname() != null ? user.getNickname() : "校友");
                    com.bumptech.glide.Glide.with(getContext())
                            .load(user.getPhoto())
                            .placeholder(R.drawable.default_avatar) // 默认占位图
                            .circleCrop()
                            .into((android.widget.ImageView) holder.findViewById(R.id.iv_avatar));
                }
                holder.text(R.id.tv_comment_content, item.getContent() != null ? item.getContent() : "");
                holder.text(R.id.tv_time, item.getCreate_time() != null ? Utils.formatCommentTime(String.valueOf(item.getCreate_time())) : "");
            }
        };

        binding.recyclerView.setAdapter(mAdapter);

        // 加载数据
        loadData();
    }

    @Override
    protected void initListeners() {
        mAdapter.setOnItemClickListener((itemView, item, position) -> {
            jumpDetail(item);
        });

        // 删除该评论
        mAdapter.setOnItemLongClickListener((itemView, item, position) -> {
            new MaterialDialog.Builder(getContext())
                    .title("提示")
                    .content("确定要删除这条评论吗？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive((dialog, which) -> {
                        // 调用删除接口
                        deleteCommentApi(item.getId(), position);
                        checkEmptyState(); // 重新检查空状态
                    })
                    .show();
        });
    }

    private void jumpDetail(Comment comment) {
        if (comment== null){
            return;
        }
    RetrofitClient.getInstance().getApi().getLostFoundById(comment.getLostfound_id()).enqueue(new Callback<Result<LostFound>>() {
        @Override
        public void onResponse(Call<Result<LostFound>> call, Response<Result<LostFound>> response) {
            if (response.isSuccessful() && response.body() != null){
                LostFound lostFound=response.body().getData();
                if (lostFound.getType().equals("失物")){
                    openNewPage(LostDetailFragment.class, LostDetailFragment.KEY_LOST, lostFound);
                }else {
                    openNewPage(FoundDetailFragment.class, FoundDetailFragment.KEY_FOUND, lostFound);
                }
            }
        }

        @Override
        public void onFailure(Call<Result<LostFound>> call, Throwable t) {
            Utils.showResponse("网络异常");
        }
    });

    }

    private void loadData() {
        // 清空原有数据并刷新
        mDataList.clear();
        mAdapter.refresh(mDataList);
        checkEmptyState();
        if (user == null || user.getId() == null) {
            Utils.showResponse("用户数据为空");
            return;
        }

        // 网络请求加载评论
        RetrofitClient.getInstance().getApi().getCommentsByUserId(user.getId()).enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> newData = response.body().getData();
                    if (newData != null && !newData.isEmpty()) {
                        mDataList.addAll(newData);
                        mAdapter.refresh(mDataList); // 刷新适配器
                    }
                }
                checkEmptyState();
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
                Utils.showResponse("网络请求失败");
                t.printStackTrace();
                checkEmptyState();
            }
        });
    }

    /**
     * 删除评论接口调用
     * @param commentId 评论ID
     * @param position 列表位置
     */
    private void deleteCommentApi(Integer commentId, int position) {
        if (commentId == null) {
            Utils.showResponse("出错了");
            return;
        }
        // 调用删除接口
        RetrofitClient.getInstance().getApi().deleteComment(commentId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 删除成功，更新列表
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