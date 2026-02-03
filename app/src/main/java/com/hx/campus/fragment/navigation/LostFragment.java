package com.hx.campus.fragment.navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.Lost;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.lostandfound.LostDetailAdapter;
import com.hx.campus.adapter.lostandfound.LostFoundDetailAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLostBinding;
import com.hx.campus.utils.LoadingDialog;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.internet.OkHttpCallback;
import com.hx.campus.utils.internet.OkhttpUtils;
import com.hx.campus.utils.service.JsonOperate;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;
import retrofit2.Callback;

@Page
public class LostFragment extends BaseFragment<FragmentLostBinding> {

    /**
     * 构建ViewBinding
     *
     * @param inflater  inflater
     * @param container 容器
     * @return ViewBinding
     */

    public static final String KEY_TITLE_NAME = "title_name";
    private static String tabTitle;//选项卡标题
    LoadingDialog loadingDialog;//加载动画
    /**
     * 自动注入参数，不能是private
     */
    @AutoWired(name = KEY_TITLE_NAME)
    String title;
    private String[] tabs_data = new String[]{};//选项卡组
    private int currentPosition;//当前选项卡的位置
    private LostFoundDetailAdapter lostFoundDetailAdapter;//丢失物品详情adapter

    @NonNull
    @Override
    protected FragmentLostBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentLostBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initArgs() {
        // 自动注入参数必须在initArgs里进行注入
        XRouter.getInstance().inject(this);
    }

    @Override
    protected String getPageTitle() {
        return title;
    }

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle();
        titleBar.addAction(new TitleBar.ImageAction(R.drawable.add) {
            @Override
            public void performAction(View view) {
                openPage(AddLostFragment.class);
            }
        });
        return titleBar;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        String[] types = getResources().getStringArray(R.array.type_titles);//根据app语言获取不同的数据
        operate_tabs(types);//选项卡
        tabTitle = types[0];//初始值
        lostFoundDetailAdapter = new LostFoundDetailAdapter(getContext());
        binding.listview.setAdapter(lostFoundDetailAdapter);
        getTypeDetailList();
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        //跳转丢失物品详情页面
        binding.listview.setOnItemClickListener((parent, view, position, id) -> {
            LostFound lost = lostFoundDetailAdapter.getItem(position);//获取lost实例
            openPage(LostDetailFragment.class, LostDetailFragment.KEY_LOST, lost);
        });

    }

    //对选项卡进行操作
    private void operate_tabs(String[] tabs_datas) {
        currentPosition = 0;//选项卡当前位置
        tabs_data = tabs_datas;
        //选项卡内容
        for (String tab : tabs_data) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tab));
        }
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentPosition = tab.getPosition();
                tabTitle = tabs_data[currentPosition];
                //数据更新
                lostFoundDetailAdapter = new LostFoundDetailAdapter(getContext());
                binding.listview.setAdapter(lostFoundDetailAdapter);
                getTypeDetailList();
                tab.setText(tabTitle);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    //发送请求获取分类下的所有内容
    private void getTypeDetailList() {
        showLoadingDialog();
        RetrofitClient.getInstance().getApi().DetailByTitle(tabTitle,"失物").enqueue(new Callback<Result<List<LostFound>>>() {
            @Override
            public void onResponse(retrofit2.Call<Result<List<LostFound>>> call, retrofit2.Response<Result<List<LostFound>>> response) {
                if(response.body() != null && response.body().isSuccess()){
                    List<LostFound> dataList = response.body().getData();
                    setAdapter(dataList);
                    hideLoadingDialog();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Result<List<LostFound>>> call, Throwable t) {
                hideLoadingDialog();
                Utils.showResponse("网络异常");

            }
        });
    }
    private void setAdapter(List<LostFound> list) {
        if (list == null || list.isEmpty()) {
            Utils.showResponse(Utils.getString(getContext(), R.string.no_relevant_info_found));
            return;
        }
        lostFoundDetailAdapter.setData(list, 1);
    }
    // 显示加载动画
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            Context context = getContext();
            loadingDialog = new LoadingDialog(context);
        }
        loadingDialog.show();
    }

    // 隐藏加载动画
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}