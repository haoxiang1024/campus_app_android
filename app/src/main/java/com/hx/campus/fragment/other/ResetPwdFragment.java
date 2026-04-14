package com.hx.campus.fragment.other;

import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentResetPwdBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.CountDownButtonHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class ResetPwdFragment extends BaseFragment<FragmentResetPwdBinding> implements View.OnClickListener {

    private CountDownButtonHelper mCountDownHelper;

    private boolean isPasswordVisible = false;
    private boolean isRePasswordVisible = false;

    @NonNull
    @Override
    protected FragmentResetPwdBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentResetPwdBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        mCountDownHelper = new CountDownButtonHelper(binding.btnGetVerifyCode, 60);

        // 绑定新密码眼睛图标点击事件
        binding.ivPwdToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivPwdToggle.setImageResource(R.drawable.ic_eye_open);
            } else {
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivPwdToggle.setImageResource(R.drawable.ic_eye_closed);
            }
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });

        // 绑定确认新密码眼睛图标点击事件
        binding.ivRepwdToggle.setOnClickListener(v -> {
            isRePasswordVisible = !isRePasswordVisible;
            if (isRePasswordVisible) {
                binding.rePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivRepwdToggle.setImageResource(R.drawable.ic_eye_open);
            } else {
                binding.rePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivRepwdToggle.setImageResource(R.drawable.ic_eye_closed);
            }
            binding.rePassword.setSelection(binding.rePassword.getText().length());
        });
    }

    @Override
    protected String getPageTitle() {
        return "重置密码";
    }

    @Override
    protected void initListeners() {
        binding.btnReset.setOnClickListener(this);
        binding.btnGetVerifyCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_reset) {
            reset();
        } else if (id == R.id.btn_get_verify_code) {
            send();
        }
    }

    private void reset() {
        String number = binding.etPhoneNumber.getEditValue();
        String password = binding.etPassword.getEditValue();
        String repassword = binding.rePassword.getEditValue();
        String code = binding.inputCode.getEditValue();
        String email = binding.inputEmail.getEditValue();

        // 校验输入非空
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword) || TextUtils.isEmpty(code) || TextUtils.isEmpty(email)) {
            Utils.showResponse("请填写完整信息");
            return;
        }

        // 校验密码一致性
        if (!password.equals(repassword)) {
            Utils.showResponse("两次密码不一致");
            return;
        }

        // 校验提交的邮箱是否为当前用户绑定的邮箱
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            Utils.showResponse("未获取到用户信息，请重新登录");
            return;
        }
        if (!TextUtils.equals(user.getEmail(), email)) {
            Utils.showResponse("提交的邮箱与绑定邮箱不一致");
            return;
        }

        // 校验验证码
        RetrofitClient.getInstance().getApi().verifyCode(email, code).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        resetPassword(number, password, email, code);
                    } else {
                        Utils.showResponse(response.body().getMsg());
                    }
                } else {
                    Utils.showResponse("验证异常：" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                Utils.showResponse("网络异常: " + t.getMessage());
            }
        });
    }

    private void resetPassword(String number, String password, String email, String code) {
        // 执行重置
        RetrofitClient.getInstance().getApi().resetPwd(number, password, email, code).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utils.showResponse(response.body().getMsg());
                    if (response.body().isSuccess()) {
                        openPage(LoginFragment.class);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                Utils.showResponse("请求失败: " + t.getMessage());
            }
        });
    }

    private void send() {
        String email = binding.inputEmail.getEditValue();

        // 校验输入非空
        if (TextUtils.isEmpty(email)) {
            Utils.showResponse("请输入邮箱");
            return;
        }

        // 校验发送验证码的邮箱是否为当前登录用户绑定的邮箱
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            Utils.showResponse("未获取到用户信息，请重新登录");
            return;
        }
        if (!TextUtils.equals(user.getEmail(), email)) {
            Utils.showResponse("只能向您绑定的邮箱发送验证码");
            return;
        }

        mCountDownHelper.start();

        // 发送验证码
        RetrofitClient.getInstance().getApi().sendCode(email).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utils.showResponse(response.body().getMsg());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                Utils.showResponse("发送失败");
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (mCountDownHelper != null) {
            mCountDownHelper.recycle();
        }
        super.onDestroyView();
    }
}