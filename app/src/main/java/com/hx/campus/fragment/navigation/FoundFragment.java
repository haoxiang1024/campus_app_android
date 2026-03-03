package com.hx.campus.fragment.navigation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayoutMediator;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFoundType;
import com.hx.campus.adapter.lostfound.FoundPagerAdapter;
import com.hx.campus.adapter.lostfound.LostPagerAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentFoundBinding;
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
public class FoundFragment extends BaseFragment<FragmentFoundBinding> {

    // 自动注入的标题参数
    @AutoWired(name = KEY_TITLE_NAME)
    String title;
    // 标题参数键名常量
    public static final String KEY_TITLE_NAME = "title_name";
    LoadingDialog loadingDialog;

    /**
     * 初始化参数
     * 通过XRouter进行依赖注入
     */
    @Override
    protected void initArgs() {
        // 使用XRouter进行字段自动注入
        XRouter.getInstance().inject(this);
    }

    /**
     * 创建视图绑定对象
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentFoundBinding 视图绑定实例
     */
    @NonNull
    @Override
    protected FragmentFoundBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        // 使用FragmentFoundBinding inflate方法创建绑定对象
        return FragmentFoundBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化视图组件
     * 设置ViewPager和TabLayout的关联
     */
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

        FoundPagerAdapter adapter = new FoundPagerAdapter(this, titles);
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
    /**
     * 初始化标题栏
     * 添加发布按钮和设置标题
     * @return TitleBar 标题栏对象
     */
    @Override
    protected TitleBar initTitle() {
        // 获取父类初始化的标题栏
        TitleBar titleBar = super.initTitle();
        // 添加右侧发布按钮
        titleBar.addAction(new TitleBar.ImageAction(R.drawable.add) {
            /**
             * 发布按钮点击事件处理
             * @param view 按钮视图
             */
            @Override
            public void performAction(android.view.View view) {
                // 跳转到招领信息发布页面
                openPage(AddFoundFragment.class);
            }
        });
        // 设置页面标题为"招领"
        titleBar.setTitle("招领");
        return titleBar;
    }

}