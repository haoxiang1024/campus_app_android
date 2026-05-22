
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
        

        binding.searchButton.setOnClickListener(v -> {

            searchInfoAdapter.setData(new ArrayList<>(), 1);
            getData();
        });


        binding.listview.setOnItemClickListener((parent, view, position, id) -> {
            SearchInfo searchInfo = searchInfoAdapter.getItem(position);
            openPage(SearchInfoFragment.class, SearchInfoFragment.KEY_INFO, searchInfo);
        });
    }

    
    private void getData() {

        String value = binding.searchEdittext.getEditValue();
        if (TextUtils.isEmpty(value)) {
            Utils.showResponse("请输入搜索内容");
            return;
        }


        showLoadingDialog();


        RetrofitClient.getInstance().getApi().searchInfo(value).enqueue(new Callback<Result<List<SearchInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<List<SearchInfo>>> call, @NonNull Response<Result<List<SearchInfo>>> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<SearchInfo>> result = response.body();


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