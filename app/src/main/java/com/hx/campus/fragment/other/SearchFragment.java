
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


@Page()
public class SearchFragment extends BaseFragment<FragmentSearchBinding> {
    
    
    private SearchInfoAdapter searchInfoAdapter;
    
    
    private LoadingDialog loadingDialog;
    
    
    private List<SearchInfo> detailList = new ArrayList<>();

    
    @NonNull
    @Override
    protected FragmentSearchBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentSearchBinding.inflate(inflater, container, attachToRoot);
    }

    
    @Override
    protected void initViews() {
        searchInfoAdapter = new SearchInfoAdapter(getContext());
        binding.listview.setAdapter(searchInfoAdapter);
    }

    
    @Override
    protected String getPageTitle() {
        return Utils.getString(getContext(), R.string.search);
    }

    
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

    
    private void showLoadingDialog() {
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
}