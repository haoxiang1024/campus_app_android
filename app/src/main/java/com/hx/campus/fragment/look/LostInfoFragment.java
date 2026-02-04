package com.hx.campus.fragment.look;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.LostFoundDetailAdapter;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLostInfoBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 显示失物信息页
@Page()
public class LostInfoFragment extends BaseFragment<FragmentLostInfoBinding> {
    private LostFoundDetailAdapter adapter;

    @NonNull
    @Override
    protected FragmentLostInfoBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentLostInfoBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        startAnim(); // 显示加载动画
        initData();  // 初始化列表数据
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.lost_info);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        // 跳转丢失物品详情页面
        binding.listview.setOnItemClickListener((parent, view, position, id) -> {
            LostFound lost = adapter.getItem(position);
            openPage(LostInfoDetailFragment.class, LostInfoDetailFragment.KEY_LOST, lost);
        });
    }

    private void initData() {
        adapter = new LostFoundDetailAdapter(getContext());
        binding.listview.setAdapter(adapter);
        getData(); // 请求数据
    }

    private void getData() {
        // 获取用户信息
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) {
            stopAnim();
            return;
        }

        // 使用 Retrofit 请求失物数据 (type="失物")
        RetrofitClient.getInstance().getApi()
                .getLostFoundListByUserId( user.getId())
                .enqueue(new Callback<Result<List<LostFound>>>() {
                    @Override
                    public void onResponse(Call<Result<List<LostFound>>> call, Response<Result<List<LostFound>>> response) {
                        stopAnim();
                        if (response.isSuccessful() && response.body() != null) {
                            Result<List<LostFound>> result = response.body();
                            if (result.isSuccess()) {
                                List<LostFound> allData = result.getData();
                                // 手动过滤 type 为 失物的数据
                                List<LostFound> lostList = new ArrayList<>();
                                if (allData != null) {
                                    for (LostFound item : allData) {
                                        if ("失物".equals(item.getType())) {
                                            lostList.add(item);
                                        }
                                    }
                                }
                                setAdapter(lostList);
                            } else {
                                Utils.showResponse(result.getMsg());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<List<LostFound>>> call, Throwable t) {
                        stopAnim();
                        Utils.showResponse(t.getMessage());
                    }
                });
    }

    private void setAdapter(List<LostFound> list) {
        if (list == null || list.isEmpty()) {
            Utils.showResponse(Utils.getString(getContext(), R.string.no_info_posted_yet));
            return;
        }
        adapter.setData(list, 1);
    }

    private void startAnim() {
        binding.avLoad.show();
    }

    private void stopAnim() {
        binding.avLoad.hide();
    }
}