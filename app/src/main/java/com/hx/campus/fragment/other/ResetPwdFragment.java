package com.hx.campus.fragment.other;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

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

    @NonNull
    @Override
    protected FragmentResetPwdBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentResetPwdBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        mCountDownHelper = new CountDownButtonHelper(binding.btnGetVerifyCode, 60);
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
        if (id == com.hx.campus.R.id.btn_reset) {
            reset();
        } else if (id == com.hx.campus.R.id.btn_get_verify_code) {
            send();
        }
    }

    private void reset() {
        String number = binding.etPhoneNumber.getEditValue();
        String password = binding.etPassword.getEditValue();
        String repassword = binding.rePassword.getEditValue();
        String code = binding.inputCode.getEditValue();
        String email = binding.inputEmail.getEditValue();

        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword) || TextUtils.isEmpty(code) || TextUtils.isEmpty(email)) {
            Utils.showResponse("请填写完整信息");
            return;
        }
        if (!password.equals(repassword)) {
            Utils.showResponse("两次密码不一致");
            return;
        }

        //  校验验证码
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
        if (TextUtils.isEmpty(email)) {
            Utils.showResponse("请输入邮箱");
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