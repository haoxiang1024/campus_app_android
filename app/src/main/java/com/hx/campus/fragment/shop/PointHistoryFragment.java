package com.hx.campus.fragment.shop;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.PointHistory;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.shop.PointHistoryAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentPointHistoryBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page(name = "积分明细")
public class PointHistoryFragment extends BaseFragment<FragmentPointHistoryBinding> {

    private PointHistoryAdapter mAdapter;

    @NonNull
    @Override
    protected FragmentPointHistoryBinding viewBindingInflate(LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentPointHistoryBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle();
        titleBar.setLeftClickListener(v -> popToBack());
        return titleBar;
    }

    @Override
    protected void initViews() {
        // 使用 binding.recyclerView 防止空指针闪退
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new PointHistoryAdapter();
        binding.recyclerView.setAdapter(mAdapter);

        // 设置列表项点击事件
        mAdapter.setOnItemClickListener((view, position) -> {
            PointHistory history = mAdapter.getItem(position);
            showHistoryDetailDialog(history);
        });


        binding.ivSearch.setOnClickListener(v -> {
            // 获取输入框中的文本并去除前后空格
            String keyword = binding.etSearch.getText().toString().trim();
            // 调用适配器的过滤方法
            if (mAdapter != null) {
                mAdapter.filter(keyword);
            }
        });

        loadData();
    }

    // 加载积分历史数据
    private void loadData() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;
        RetrofitClient.getInstance().getApi().getPointHistory(user.getId()).enqueue(new Callback<Result<List<PointHistory>>>() {
            @Override
            public void onResponse(Call<Result<List<PointHistory>>> call, Response<Result<List<PointHistory>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    mAdapter.refresh(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<Result<List<PointHistory>>> call, Throwable t) {
                Utils.showResponse("网络错误");
            }
        });
    }

    // 显示详细信息的弹窗
    private void showHistoryDetailDialog(PointHistory history) {
        if (history == null || getContext() == null) return;

        String detailMsg = "积分变更：" + history.getDisplayPoints() + "\n" +
                "变动类型：" + history.getTypeText() + "\n" + // 使用转换后的文本
                "变动时间：" + history.getFormattedTime() + "\n" + // 使用格式化时间
                "变动原因：" + history.getDescription();

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                .title("积分详情")
                .content(detailMsg)
                .positiveText("确定");

        builder.show();
    }
}