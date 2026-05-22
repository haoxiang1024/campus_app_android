package com.hx.campus.fragment.look;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.lostfound.LostFoundDetailAdapter;
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

@Page()
public class LostInfoFragment extends BaseFragment<FragmentLostInfoBinding> {


    private LostFoundDetailAdapter adapter;

    private List<LostFound> originalLostList = new ArrayList<>();

    @NonNull
    @Override
    protected FragmentLostInfoBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentLostInfoBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        startAnim();
        initData();
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.lost_info);
    }

    @Override
    protected void initListeners() {
        super.initListeners();


        binding.listview.setOnItemClickListener((parent, view, position, id) -> {
            LostFound lost = adapter.getItem(position);
            openPage(LostInfoDetailFragment.class, LostInfoDetailFragment.KEY_LOST, lost);
        });


        binding.searchButton.setOnClickListener(v -> {
            String keyword = binding.searchEdittext.getText().toString().trim();

            if (TextUtils.isEmpty(keyword)) {
                setAdapter(originalLostList);
                return;
            }


            List<LostFound> searchResult = new ArrayList<>();
            for (LostFound item : originalLostList) {
                if ((item.getTitle() != null && item.getTitle().contains(keyword)) ||
                        (item.getContent() != null && item.getContent().contains(keyword))) {
                    searchResult.add(item);
                }
            }
            setAdapter(searchResult);
        });
    }

    private void initData() {
        adapter = new LostFoundDetailAdapter(getContext());
        binding.listview.setAdapter(adapter);
        getData();
    }

    private void getData() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) {
            stopAnim();
            return;
        }

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
                                originalLostList.clear();
                                if (allData != null) {
                                    for (LostFound item : allData) {
                                        if ("失物".equals(item.getType())) {
                                            originalLostList.add(item);
                                        }
                                    }
                                }
                                setAdapter(originalLostList);
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

        adapter = new LostFoundDetailAdapter(getContext());
        binding.listview.setAdapter(adapter);

        if (list == null || list.isEmpty()) {
            Utils.showResponse(Utils.getString(getContext(), R.string.no_info_posted_yet));
            adapter.setData(new ArrayList<>(), 1);
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