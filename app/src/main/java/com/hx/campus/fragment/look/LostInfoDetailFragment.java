package com.hx.campus.fragment.look;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLostInfoDetailBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class LostInfoDetailFragment extends BaseFragment<FragmentLostInfoDetailBinding> {

    public static final String KEY_LOST = "lost";

    LostFound lost; // 实体类改为 LostFound

    @Override
    protected void initArgs() {
        super.initArgs();
       // XRouter.getInstance().inject(this);
        if (getArguments() != null) {
            lost = (LostFound) getArguments().getSerializable(KEY_LOST);
        }
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.lost_info_detail);
    }

    @NonNull
    @Override
    protected FragmentLostInfoDetailBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentLostInfoDetailBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        if (lost != null) {
            setViews();
            if (binding.sumbitBtn != null) {
                binding.sumbitBtn.setOnClickListener(v -> {
                    String selected = binding.state.getSelectedItem().toString();
                    submitState(selected);
                });
            }
        }else {
            Log.e("Check", "错误：lost 数据为空");
        }
    }

    private void setViews() {
        binding.tvLostTitle.setText(lost.getTitle());
        binding.tvLostContent.setText(lost.getContent());
        if (TextUtils.isEmpty(lost.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(lost.getImg()).into(binding.imgLost);
        }
        binding.tvAuthor.setText(lost.getNickname());
        binding.tvPhonenum.setText(lost.getPhone());
        binding.location.setText(lost.getPlace());

        // 设置状态
        String[] statuses = {"已找到", "寻找中"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.state.setAdapter(adapter);

        int position = Arrays.asList(statuses).indexOf(lost.getState());
        if (position >= 0) binding.state.setSelection(position);
        binding.tvDate.setText(Utils.dateFormat(lost.getPubDate()));
    }

    private void submitState(String selectedState) {
        // 使用 Retrofit 统一更新状态接口
        RetrofitClient.getInstance().getApi()
                .updateState(lost.getId(), selectedState, lost.getUserId())
                .enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            lost.setState(selectedState);
                            Utils.showResponse("状态已更改");
                        } else {
                            Utils.showResponse("操作失败");
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        Utils.showResponse("网络异常");
                    }
                });
    }
}