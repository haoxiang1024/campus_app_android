package com.hx.campus.fragment.personal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.activity.MainActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentAccountBinding;
import com.hx.campus.fragment.other.ResetPwdFragment;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class AccountFragment extends BaseFragment<FragmentAccountBinding> implements View.OnClickListener {

    @NonNull
    @Override
    protected FragmentAccountBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentAccountBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        initData(); // 初始化账户数据
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        binding.tvResetPwd.setOnClickListener(this); // 重置密码
        binding.btnSubmit.setOnClickListener(this); // 提交
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    private void initData() {
        // 获取头像控件
        ImageView imgView = binding.rivHeadPic;
        // 获取用户信息
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;

        // 设置头像
        if (TextUtils.isEmpty(user.getPhoto())) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
            Glide.with(this).load(user.getPhoto()).into(imgView);
        }

        // 设置昵称
        binding.tvNickName.setText(user.getNickname());

        // 修改昵称弹窗
        binding.tvNickName.setOnClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            View dialogView = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
            alertDialog.setView(dialogView);
            alertDialog.show();

            EditText editText = alertDialog.findViewById(R.id.tv_message);
            editText.setText(user.getNickname());

            Button btnSure = alertDialog.findViewById(R.id.btn_positive);
            Button btnCancel = alertDialog.findViewById(R.id.btn_negative);

            btnSure.setOnClickListener(v1 -> {
                String newText = editText.getText().toString().trim();
                if (TextUtils.isEmpty(newText)) {
                    Utils.showResponse(Utils.getString(getContext(), R.string.nickname_cannot_be_empty));
                    return;
                }
                binding.tvNickName.setText(newText);
                alertDialog.dismiss();
            });
            btnCancel.setOnClickListener(v1 -> alertDialog.dismiss());
        });

        // 设置性别
        if ("男".equals(user.getSex())) {
            binding.rbMan.setChecked(true);
        } else {
            binding.rbWomen.setChecked(true);
        }

        // 注册日期与手机号
        binding.regDate.setText("注册日期：" + Utils.dateFormat(user.getReg_date()));
        binding.phone.setText("手机号：" + user.getPhone());
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.ac);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_reset_pwd) {
            openNewPage(ResetPwdFragment.class);
        } else if (id == R.id.btn_submit) {
            update();
        }
    }

    private void update() {
        String nickName = binding.tvNickName.getText().toString();
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;

        int id = user.getId();
        String sex = binding.rbMan.isChecked() ? "男" : "女";

        // 发起 Retrofit 请求
        RetrofitClient.getInstance().getApi().updateAccount(nickName, sex, id).enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(@NonNull Call<Result<User>> call, @NonNull Response<Result<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<User> result = response.body();
                    if (result.isSuccess()) {
                        //  使用返回的最新的 User 数据更新本地缓存
                        Utils.doUserData(result.getData());
                        //  提示成功并跳转
                        Utils.showResponse("修改资料成功！");
                        binding.getRoot().postDelayed(() -> {
                            if (isAdded()) { // 检查 Fragment 是否还在 Activity 中
                                startActivity(new Intent(getContext(), MainActivity.class));
                            }
                        }, 800); }
                    else {
                        Utils.showResponse(result.getMsg());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<User>> call, @NonNull Throwable t) {
                Utils.showResponse("修改失败：" + t.getMessage());
            }
        });
    }
}