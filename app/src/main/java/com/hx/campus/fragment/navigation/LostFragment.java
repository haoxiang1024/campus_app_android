package com.hx.campus.fragment.navigation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayoutMediator;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFoundType;
import com.hx.campus.adapter.lostfound.LostPagerAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLostBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.LoadingDialog;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class LostFragment extends BaseFragment<FragmentLostBinding> {

    @AutoWired(name = KEY_TITLE_NAME)
    String title;
    public static final String KEY_TITLE_NAME = "title_name";
    LoadingDialog loadingDialog;
    @Override
    protected void initArgs() {
        XRouter.getInstance().inject(this);
    }

    @NonNull
    @Override
    protected FragmentLostBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentLostBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        showLoading();
        RetrofitClient.getInstance().getApi().getAllType().enqueue(new Callback<Result<List<LostFoundType>>>() {
            @Override
            public void onResponse(Call<Result<List<LostFoundType>>> call, Response<Result<List<LostFoundType>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<LostFoundType> types = response.body().getData();
                    setupViewPager(types);
                } else {
                    Utils.showResponse("获取分类失败");
                }
            }

            @Override
            public void onFailure(Call<Result<List<LostFoundType>>> call, Throwable t) {
                Utils.showResponse("网络错误: " + t.getMessage());
            }
        });
    }

    private void setupViewPager(List<LostFoundType> types) {
        String[] titles = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            titles[i] = types.get(i).getName();
        }

        LostPagerAdapter adapter = new LostPagerAdapter(this, titles);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(titles[position]);
        }).attach();
        hideLoadingDialog();
    }

    private void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        loadingDialog.show();
    }
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle();
        titleBar.addAction(new TitleBar.ImageAction(R.drawable.add) {
            @Override
            public void performAction(android.view.View view) {
                // 跳转到发布页
                openPage(AddLostFragment.class);
            }
        });
        titleBar.setTitle("失物");
        return titleBar;
    }
}