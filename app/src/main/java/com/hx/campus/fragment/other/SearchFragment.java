/**
 * 搜索功能Fragment
 * 提供失物招领信息的搜索功能，支持关键词搜索和结果展示
 * 集成搜索适配器、网络请求、加载动画等完整功能
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2024
 */
package com.hx.campus.fragment.other;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.SearchInfo;
import com.hx.campus.adapter.lostfound.SearchInfoAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentSearchBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.LoadingDialog;
import com.xuexiang.xpage.annotation.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 搜索页面Fragment
 * 继承自BaseFragment，使用ViewBinding进行视图绑定
 */
@Page()
public class SearchFragment extends BaseFragment<FragmentSearchBinding> {
    
    /** 搜索结果适配器 */
    private SearchInfoAdapter searchInfoAdapter;
    
    /** 加载对话框 */
    private LoadingDialog loadingDialog;
    
    /** 搜索结果数据列表 */
    private List<SearchInfo> detailList = new ArrayList<>();

    /**
     * 创建ViewBinding实例
     * 
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentSearchBinding实例
     */
    @NonNull
    @Override
    protected FragmentSearchBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentSearchBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化视图组件
     * 创建并配置搜索适配器
     */
    @Override
    protected void initViews() {
        searchInfoAdapter = new SearchInfoAdapter(getContext());
        binding.listview.setAdapter(searchInfoAdapter);
    }

    /**
     * 获取页面标题
     * @return 页面标题字符串
     */
    @Override
    protected String getPageTitle() {
        return Utils.getString(getContext(), R.string.search);
    }

    /**
     * 初始化事件监听器
     * 设置搜索按钮和列表项点击事件
     */
    @Override
    protected void initListeners() {
        super.initListeners();
        
        // 搜索按钮点击事件
        binding.searchButton.setOnClickListener(v -> {
            // 每次搜索前清空之前的数据
            searchInfoAdapter.setData(new ArrayList<>(), 1);
            getData();
        });

        // 列表项点击事件 - 跳转到详情页
        binding.listview.setOnItemClickListener((parent, view, position, id) -> {
            SearchInfo searchInfo = searchInfoAdapter.getItem(position);
            openPage(SearchInfoFragment.class, SearchInfoFragment.KEY_INFO, searchInfo);
        });
    }

    /**
     * 执行搜索请求
     * 通过Retrofit发起网络请求获取搜索结果
     */
    private void getData() {
        // 获取用户输入的搜索关键词
        String value = binding.searchEdittext.getEditValue();
        if (TextUtils.isEmpty(value)) {
            Utils.showResponse("请输入搜索内容");
            return;
        }

        // 显示加载动画
        showLoadingDialog();

        // 发起搜索API请求
        RetrofitClient.getInstance().getApi().searchInfo(value).enqueue(new Callback<Result<List<SearchInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<List<SearchInfo>>> call, @NonNull Response<Result<List<SearchInfo>>> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<SearchInfo>> result = response.body();

                    // 处理搜索结果
                    if (result.isSuccess()) {
                        detailList = result.getData();
                        if (detailList != null && !detailList.isEmpty()) {
                            // 有搜索结果，更新适配器
                            searchInfoAdapter.setData(detailList, 1);
                        } else {
                            // 无搜索结果
                            Utils.showResponse(Utils.getString(getContext(), R.string.no_relevant_info_found));
                        }
                    } else {
                        // 业务逻辑失败
                        Utils.showResponse(result.getMsg());
                    }
                } else {
                    // HTTP响应异常
                    Utils.showResponse("服务器响应异常");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<List<SearchInfo>>> call, @NonNull Throwable t) {
                hideLoadingDialog();
                // 网络请求失败
                Utils.showResponse("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 显示加载对话框
     * 单例模式，避免重复创建
     */
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        loadingDialog.show();
    }

    /**
     * 隐藏加载对话框
     * 确保对话框存在且正在显示时才隐藏
     */
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}