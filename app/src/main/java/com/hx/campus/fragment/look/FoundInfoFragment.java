package com.hx.campus.fragment.look;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.lostfound.LostFoundDetailAdapter;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentFoundInfoBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class FoundInfoFragment extends BaseFragment<FragmentFoundInfoBinding> implements AdapterView.OnItemClickListener {

    private LostFoundDetailAdapter adapter;

    @NonNull
    @Override
    protected FragmentFoundInfoBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentFoundInfoBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        startAnim();
        initData();
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.found_info);
    }

    private void initData() {
        adapter = new LostFoundDetailAdapter(getContext());
        binding.listview.setAdapter(adapter);
        getData();
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        binding.listview.setOnItemClickListener(this);
    }

    private void getData() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;

        // 使用 Retrofit 请求招领数据 (type="招领")
        RetrofitClient.getInstance().getApi()
                .getLostFoundListByUserId(user.getId())
                .enqueue(new Callback<Result<List<LostFound>>>() {
                    @Override
                    public void onResponse(Call<Result<List<LostFound>>> call, Response<Result<List<LostFound>>> response) {
                        stopAnim();
                        if (response.isSuccessful() && response.body() != null) {
                            Result<List<LostFound>> result = response.body();
                            if (result.isSuccess()) {
                                List<LostFound> allData = result.getData();
                                // 手动过滤 type 为 招领的数据
                                List<LostFound> foundList = new ArrayList<>();
                                if (allData != null) {
                                    for (LostFound item : allData) {
                                        if ("招领".equals(item.getType())) {
                                            foundList.add(item);
                                        }
                                    }
                                }
                                setAdapter(foundList);
                            } else {
                                Utils.showResponse(result.getMsg());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<List<LostFound>>> call, Throwable t) {
                        stopAnim();
                        Utils.showResponse("网络异常");
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LostFound found = adapter.getItem(position);
        openPage(FoundInfoDetailFragment.class, FoundInfoDetailFragment.KEY_FOUND, found);
    }
}