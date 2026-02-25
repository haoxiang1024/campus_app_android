package com.hx.campus.fragment.navigation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.lostfound.LostFoundRecyclerAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoundListSubFragment extends BaseFragment<LayoutCommonListBinding> {
    private String mTabTitle;
    private LostFoundRecyclerAdapter mAdapter;

    public static FoundListSubFragment newInstance(String title) {
        FoundListSubFragment fragment = new FoundListSubFragment();
        Bundle args = new Bundle();
        args.putString("tab_title", title);
        fragment.setArguments(args);
        return fragment;
    }
    @NonNull
    @Override
    protected LayoutCommonListBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return LayoutCommonListBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        mTabTitle = getArguments() != null ? getArguments().getString("tab_title") : "";
        mAdapter = new LostFoundRecyclerAdapter(getContext(), found -> {
            // 跳转详情页
            openPage(FoundDetailFragment.class, FoundDetailFragment.KEY_FOUND, found);
        });
        binding.recyclerView.setAdapter(mAdapter);
        binding.recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        loadData();

    }
    private void loadData() {
        RetrofitClient.getInstance().getApi().DetailByTitle(mTabTitle, "招领")
                .enqueue(new Callback<Result<List<LostFound>>>() {
                    @Override
                    public void onResponse(Call<Result<List<LostFound>>> call, Response<Result<List<LostFound>>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            hideEmptyView();
                            mAdapter.setData(response.body().getData());
                        }
                    }
                    @Override public void onFailure(Call<Result<List<LostFound>>> call, Throwable t) {}
                });
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
