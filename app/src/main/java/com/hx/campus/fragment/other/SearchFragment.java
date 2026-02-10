package com.hx.campus.fragment.other;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.lostfound.SearchInfoAdapter;
import com.hx.campus.adapter.entity.SearchInfo;
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
    private SearchInfoAdapter searchInfoAdapter; // 搜索适配器
    private LoadingDialog loadingDialog; // 加载动画
    private List<SearchInfo> detailList = new ArrayList<>(); // 数据list

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
        // 搜索按钮点击
        binding.searchButton.setOnClickListener(v -> {
            // 每次搜索重置适配器数据
            searchInfoAdapter.setData(new ArrayList<>(), 1);
            getData();
        });

        // List 点击跳转详情页
        binding.listview.setOnItemClickListener((parent, view, position, id) -> {
            SearchInfo searchInfo = searchInfoAdapter.getItem(position);
            openPage(SearchInfoFragment.class, SearchInfoFragment.KEY_INFO, searchInfo);
        });
    }

    private void getData() {
        // 获取输入框的值
        String value = binding.searchEdittext.getEditValue();
        if (TextUtils.isEmpty(value)) {
            Utils.showResponse("请输入搜索内容");
            return;
        }

        showLoadingDialog();

        // 使用 Retrofit 进行异步请求
        RetrofitClient.getInstance().getApi().searchInfo(value).enqueue(new Callback<Result<List<SearchInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<List<SearchInfo>>> call, @NonNull Response<Result<List<SearchInfo>>> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<SearchInfo>> result = response.body();

                    // 判断业务逻辑：是否搜索到数据
                    if (result.isSuccess()) {
                        detailList = result.getData();
                        if (detailList != null && !detailList.isEmpty()) {
                            searchInfoAdapter.setData(detailList, 1);
                        } else {
                            Utils.showResponse(Utils.getString(getContext(), R.string.no_relevant_info_found));
                        }
                    } else {
                        Utils.showResponse(result.getMsg());
                    }
                } else {
                    Utils.showResponse("服务器响应异常");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<List<SearchInfo>>> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Utils.showResponse("网络错误: " + t.getMessage());
            }
        });
    }

    // 显示加载动画
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
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