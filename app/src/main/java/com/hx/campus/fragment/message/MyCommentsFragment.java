package com.hx.campus.fragment.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hx.campus.R;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;
import com.xuexiang.xui.adapter.simple.XUISimpleAdapter;
import com.xuexiang.xui.adapter.simple.XUISimpleAdapter;
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.utils.WidgetUtils;

import java.util.ArrayList;
import java.util.List;

@Page(name = "我的评论")
public class MyCommentsFragment extends BaseFragment<LayoutCommonListBinding> {

    public static class CommentEntity {
        public String id;
        public String postTitle;
        public String content;
        public String time;
        public boolean isLostPost; // 标记是失物贴还是招领贴
    }

    private BaseRecyclerAdapter<CommentEntity> mAdapter;
    private List<CommentEntity> mDataList = new ArrayList<>();

    @NonNull
    @Override
    protected LayoutCommonListBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return LayoutCommonListBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        // 设置 RecyclerView 布局管理器
        WidgetUtils.initRecyclerView(binding.recyclerView);

        // 初始化适配器
        mAdapter = new BaseRecyclerAdapter<CommentEntity>(mDataList) {
            @Override
            protected int getItemLayoutId(int viewType) {
                // 这里绑定单条评论的 XML 布局（需要在 res/layout 创建，代码见第4步）
                return R.layout.adapter_my_comment_item;
            }

            @Override
            protected void bindData(@NonNull RecyclerViewHolder holder, int position, CommentEntity item) {
                holder.text(R.id.tv_post_title, "回复帖子: " + item.postTitle);
                holder.text(R.id.tv_comment_content, item.content);
                holder.text(R.id.tv_time, item.time);
            }
        };

        binding.recyclerView.setAdapter(mAdapter);

        // 加载数据
        loadData();
    }

    @Override
    protected void initListeners() {
        mAdapter.setOnItemClickListener((itemView, item, position) -> {
            // TODO: 构建传参用的实体对象，跳转到详情页
            // 示例伪代码：
            // if (item.isLostPost) {
            //     openNewPage(LostDetailFragment.class, "key", item.id);
            // } else {
            //     openNewPage(FoundDetailFragment.class, "key", item.id);
            // }
        });

        // 【长按】删除该评论
        mAdapter.setOnItemLongClickListener((itemView, item, position) -> {
            new MaterialDialog.Builder(getContext())
                    .title("提示")
                    .content("确定要删除这条评论吗？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive((dialog, which) -> {
                        // deleteCommentApi(item.id);
                        //mAdapter.remove(position);
                        checkEmptyState(); // 重新检查空状态
                    })
                    .show();
            
        });
    }

    private void loadData() {
        mDataList.clear();

        mAdapter.refresh(mDataList);
        checkEmptyState();
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
