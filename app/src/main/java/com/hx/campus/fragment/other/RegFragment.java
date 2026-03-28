package com.hx.campus.fragment.other;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

// 注册页面
@Page
public class RegFragment extends BaseFragment<FragmentRegBinding> implements View.OnClickListener {

    private CountDownButtonHelper mCountDownHelper;
    LoadingDialog loadingDialog;//加载动画
    // 设置连接超时时间
    private final int timeLimit = 10;
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
        // 初始化验证码倒计时助手，绑定获取验证码按钮，时长60秒
        mCountDownHelper = new CountDownButtonHelper(binding.btnGetVerifyCode, 60);
    }

    @Override
    protected void initListeners() {
        // 绑定监听器
        binding.btnRegister.setOnClickListener(this);
        binding.btnGetVerifyCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == com.hx.campus.R.id.btn_register) {
            handleRegister(); // 执行注册校验
        } else if (id == com.hx.campus.R.id.btn_get_verify_code) {
            sendVerifyCode(); // 发送验证码
        }
    }

    /**
     * 注册前的预校验逻辑
     */
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

    /**
     * 校验验证码并执行注册
     */
    private void verifyCodeAndRegister(String phone, String password, String email, String code) {
        RetrofitClient.getInstance().getApi().verifyCode(email, code).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        // 验证码通过，调用注册接口
                        doRegisterRequest(phone, email, password);
                    } else {
                        Utils.showResponse("验证码错误：" + response.body().getMsg());
                    }
                } else {
                    Utils.showResponse("服务器验证异常");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                Utils.showResponse("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 最终提交注册请求
     */
    private void doRegisterRequest(String phone, String email, String password) {
        RetrofitClient.getInstance().getApi().register(phone, email, password,0).enqueue(new Callback<Result<LoginResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<Result<LoginResponseDTO>> call, @NonNull Response<Result<LoginResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utils.showResponse(response.body().getMsg());
                    if (response.body().isSuccess()) {
                        // 注册成功，直接跳转主页
                        LoginResponseDTO loginData = response.body().getData();
                        User user = loginData.getUserInfo();
                        Utils.doUserData(user);
                        String token = loginData.getToken();
                        TokenUtils.handleLoginSuccess(token);
                        fetchIMTokenAndConnect(user);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<LoginResponseDTO>> call, @NonNull Throwable t) {
                Utils.showResponse("注册请求失败: " + t.getMessage());
            }
        });
    }
    /**
     * 获取 IM Token 并根据本地状态选择连接方式
     */
    private void fetchIMTokenAndConnect(User user) {
        RetrofitClient.getInstance().getApi().getIMUserToken(user.getId(),user.getNickname()).enqueue(new retrofit2.Callback<Result<String>>() {
            @Override
            public void onResponse(retrofit2.Call<Result<String>> call, retrofit2.Response<Result<String>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    String imToken = response.body().getData();
                    // 执行连接逻辑
                    performIMConnect(imToken);
                    // 登录全流程完成，跳转主页
                    hideLoadingDialog();
                    ActivityUtils.startActivity(MainActivity.class);
                } else {
                    hideLoadingDialog();
                    Utils.showResponse("IM授权获取失败");
                    ActivityUtils.startActivity(MainActivity.class);
                }
            }
            /**
             * 融云连接核心逻辑
             */
            private void performIMConnect(String token) {
                // 从本地存储获取旧 Token
                String localToken = TokenUtils.getImToken();
                RongIMClient.ConnectCallback connectCallback=new RongIMClient.ConnectCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Log.e("IM_LOG", "融云连接成功: " + userId);
                        // 连接成功后，持久化新的 Token 到 MMKV
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
                    // 非首次连接：不传超时参数
                    Log.e("IM_LOG", "Token一致，执行快速连接...");
                    IMCenter.getInstance().connect(token, connectCallback);
                } else {
                    // 首次连接：传入超时时间
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

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * 发送验证码逻辑
     */
    private void sendVerifyCode() {
        String email = binding.inputEmail.getEditValue();
        if (TextUtils.isEmpty(email)) {
            Utils.showResponse("请输入邮箱地址");
            return;
        }

        mCountDownHelper.start(); // 开始倒计时

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
                mCountDownHelper.recycle(); // 失败时重置按钮
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (mCountDownHelper != null) {
            mCountDownHelper.recycle(); // 销毁视图时回收倒计时，防止内存泄漏
        }
        super.onDestroyView();
    }
}