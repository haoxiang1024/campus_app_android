package com.hx.campus.fragment.look;

import android.text.TextUtils;
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
import com.hx.campus.databinding.FragmentFoundInfoDetailBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class FoundInfoDetailFragment extends BaseFragment<FragmentFoundInfoDetailBinding> {

    public static final String KEY_FOUND = "found";
    LostFound found;

    @Override
    protected void initArgs() {
        super.initArgs();
        //XRouter.getInstance().inject(this);
        if (getArguments() != null) {
            found = (LostFound) getArguments().getSerializable(KEY_FOUND);
        }
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.found_info_detail);
    }

    @NonNull
    @Override
    protected FragmentFoundInfoDetailBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentFoundInfoDetailBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        if (found != null) {
            setViews();
        }
    }

    private void setViews() {
        binding.tvLostTitle.setText(found.getTitle());
        binding.tvLostContent.setText(found.getContent());
        if (TextUtils.isEmpty(found.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(found.getImg()).into(binding.imgLost);
        }
        binding.tvAuthor.setText(found.getNickname());
        binding.tvPhonenum.setText(found.getPhone());
        binding.location.setText(found.getPlace());

        String[] statuses = {"已认领", "待认领"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.state.setAdapter(adapter);

        int position = Arrays.asList(statuses).indexOf(found.getState());
        if (position >= 0) binding.state.setSelection(position);

        binding.state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                binding.sumbitBtn.setOnClickListener(v -> submitState(selected));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.tvDate.setText(Utils.dateFormat(found.getPubDate()));
    }

    private void submitState(String selectedState) {
        RetrofitClient.getInstance().getApi()
                .updateState(found.getId(), selectedState, found.getUserId())
                .enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Utils.showResponse(Utils.getString(getContext(), R.string.submit_success));
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