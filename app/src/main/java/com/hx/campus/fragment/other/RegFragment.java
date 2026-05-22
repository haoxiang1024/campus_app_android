package com.hx.campus.fragment.other;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hx.campus.R;
import com.hx.campus.activity.MainActivity;
import com.hx.campus.adapter.entity.LoginResponseDTO;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentRegBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.LoadingDialog;
import com.hx.campus.utils.common.TokenUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.CountDownButtonHelper;
import com.xuexiang.xutil.app.ActivityUtils;

import io.rong.imkit.IMCenter;
import io.rong.imlib.RongIMClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class RegFragment extends BaseFragment<FragmentRegBinding> implements View.OnClickListener {

    private CountDownButtonHelper mCountDownHelper;
    LoadingDialog loadingDialog;

    private final int timeLimit = 10;

    private boolean isPasswordVisible = false;
    private boolean isRePasswordVisible = false;

    @NonNull
    @Override
    protected FragmentRegBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return FragmentRegBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected String getPageTitle() {
        return "注册";
    }

    @Override
    protected void initViews() {

        mCountDownHelper = new CountDownButtonHelper(binding.btnGetVerifyCode, 60);


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
    protected void initListeners() {

        binding.btnRegister.setOnClickListener(this);
        binding.btnGetVerifyCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_register) {
            handleRegister();
        } else if (id == R.id.btn_get_verify_code) {
            sendVerifyCode();
        }
    }

    
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getActivity());
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    
    private void handleRegister() {
        String phone = binding.etPhoneNumber.getEditValue();
        String password = binding.etPassword.getEditValue();
        String rePassword = binding.rePassword.getEditValue();
        String email = binding.inputEmail.getEditValue();
        String code = binding.inputCode.getEditValue();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(email) || TextUtils.isEmpty(code)) {
            Utils.showResponse("请填写完整注册信息");
            return;
        }

        if (!password.equals(rePassword)) {
            Utils.showResponse("两次输入的密码不一致");
            return;
        }

        verifyCodeAndRegister(phone, password, email, code);
    }

    
    private void verifyCodeAndRegister(String phone, String password, String email, String code) {
        showLoadingDialog();

        RetrofitClient.getInstance().getApi().verifyCode(email, code).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {

                        doRegisterRequest(phone, email, password);
                    } else {
                        hideLoadingDialog();
                        Utils.showResponse("验证码错误：" + response.body().getMsg());
                    }
                } else {
                    hideLoadingDialog();
                    Utils.showResponse("服务器验证异常");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Utils.showResponse("网络异常: " + t.getMessage());
            }
        });
    }

    
    private void doRegisterRequest(String phone, String email, String password) {
        RetrofitClient.getInstance().getApi().register(phone, email, password,0).enqueue(new Callback<Result<LoginResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<Result<LoginResponseDTO>> call, @NonNull Response<Result<LoginResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utils.showResponse(response.body().getMsg());
                    if (response.body().isSuccess()) {

                        LoginResponseDTO loginData = response.body().getData();
                        User user = loginData.getUserInfo();
                        Utils.doUserData(user);
                        String token = loginData.getToken();
                        TokenUtils.handleLoginSuccess(token);
                        fetchIMTokenAndConnect(user);
                    } else {
                        hideLoadingDialog();
                    }
                } else {
                    hideLoadingDialog();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<LoginResponseDTO>> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Utils.showResponse("注册请求失败: " + t.getMessage());
            }
        });
    }

    
    private void fetchIMTokenAndConnect(User user) {
        RetrofitClient.getInstance().getApi().getIMUserToken(user.getId(),user.getNickname()).enqueue(new retrofit2.Callback<Result<String>>() {
            @Override
            public void onResponse(retrofit2.Call<Result<String>> call, retrofit2.Response<Result<String>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    String imToken = response.body().getData();

                    performIMConnect(imToken);

                    hideLoadingDialog();
                    ActivityUtils.startActivity(MainActivity.class);
                } else {
                    hideLoadingDialog();
                    Utils.showResponse("IM授权获取失败");
                    ActivityUtils.startActivity(MainActivity.class);
                }
            }

            
            private void performIMConnect(String token) {

                String localToken = TokenUtils.getImToken();
                RongIMClient.ConnectCallback connectCallback=new RongIMClient.ConnectCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Log.e("IM_LOG", "融云连接成功: " + userId);

                        TokenUtils.setImToken(token);
                    }

                    @Override
                    public void onError(RongIMClient.ConnectionErrorCode e) {
                        Log.e("IM_LOG", "连接失败码: " + e.getValue());
                    }

                    @Override
                    public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus code) {

                    }
                };

                if (token.equals(localToken)) {

                    Log.e("IM_LOG", "Token一致，执行快速连接...");
                    IMCenter.getInstance().connect(token, connectCallback);
                } else {

                    Log.e("IM_LOG", "Token变更，执行带超时的首次连接...");
                    IMCenter.getInstance().connect(token, timeLimit, connectCallback);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Result<String>> call, Throwable t) {
                hideLoadingDialog();
                Log.e("IM_ERROR", "获取IM Token网络失败", t);
                ActivityUtils.startActivity(MainActivity.class);
            }
        });
    }

    
    private void sendVerifyCode() {
        String email = binding.inputEmail.getEditValue();
        if (TextUtils.isEmpty(email)) {
            Utils.showResponse("请输入邮箱地址");
            return;
        }

        mCountDownHelper.start();

        RetrofitClient.getInstance().getApi().sendCode(email).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utils.showResponse(response.body().getMsg());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                Utils.showResponse("验证码发送失败");
                mCountDownHelper.recycle();
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