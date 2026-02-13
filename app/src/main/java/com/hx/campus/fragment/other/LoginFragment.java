package com.hx.campus.fragment.other;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.activity.MainActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLoginBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.LoadingDialog;
import com.hx.campus.utils.common.RandomUtils;
import com.hx.campus.utils.common.ResponseMsg;
import com.hx.campus.utils.common.SettingUtils;
import com.hx.campus.utils.common.TokenUtils;
import com.hx.campus.utils.sdkinit.UMengInit;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.utils.ViewUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xutil.app.ActivityUtils;

import io.rong.imkit.IMCenter;
import io.rong.imlib.RongIMClient;


@Page(anim = CoreAnim.none)
public class LoginFragment extends BaseFragment<FragmentLoginBinding> implements View.OnClickListener {

    LoadingDialog loadingDialog;//加载动画
    // 设置连接超时时间
    private final int timeLimit = 10;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    //初始化控件
    @Override
    protected void initViews() {

        //隐私政策弹窗
        if (!SettingUtils.isAgreePrivacy()) {
            Utils.showPrivacyDialog(getContext(), (dialog, which) -> {
                dialog.dismiss();
                handleSubmitPrivacy();
            });
        }
        boolean isAgreePrivacy = SettingUtils.isAgreePrivacy();
        binding.cbProtocol.setChecked(isAgreePrivacy);
        refreshButton(isAgreePrivacy);
        binding.cbProtocol.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingUtils.setIsAgreePrivacy(isChecked);
            refreshButton(isChecked);
        });
    }

    //初始化标题栏
    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle()
                .setImmersive(true);
        titleBar.setBackgroundColor(Color.TRANSPARENT);
        titleBar.setTitle("");
        //titleBar.setLeftImageDrawable(ResUtils.getVectorDrawable(getContext(), R.drawable.ic_login_close));
        titleBar.setActionTextColor(ThemeUtils.resolveColor(getContext(), R.attr.colorAccent));
        return titleBar;
    }

    //初始化监听器
    @Override
    protected void initListeners() {
        binding.btnLogin.setOnClickListener(this);
        binding.tvForgetPassword.setOnClickListener(this);
        binding.tvUserProtocol.setOnClickListener(this);
        binding.tvPrivacyProtocol.setOnClickListener(this);
        binding.tvReg.setOnClickListener(this);
    }

    @NonNull
    @Override
    protected FragmentLoginBinding viewBindingInflate(LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentLoginBinding.inflate(inflater, container, attachToRoot);

    }


    private void refreshButton(boolean isChecked) {
        ViewUtils.setEnabled(binding.btnLogin, isChecked);
    }

    //提交隐私政策
    private void handleSubmitPrivacy() {
        SettingUtils.setIsAgreePrivacy(true);
        UMengInit.init();

    }

    //控件点击事件
    @SuppressLint("NonConstantResourceId")
    @SingleClick
    @Override
    public void onClick(View v) {
        int id = v.getId();
        try {
            switch (id) {
                case R.id.btn_login:
                    // 登录
                    //showLoadingDialog();//显示加载动画
                    if (binding.etPhoneNumber.validate() && binding.etPassword.validate()) {
                        //校验成功进行登录
                        Login();
                    }else {
                        Utils.showResponse(ResponseMsg.ACCOUNT_PWD_ERROR);
                    }
                    break;
                case R.id.tv_user_protocol:
                    // 用户协议
                    Utils.gotoProtocol(this, false, true);
                    break;
                case R.id.tv_privacy_protocol:
                    // 隐私政策
                    Utils.gotoProtocol(this, true, true);
                    break;
                case R.id.tv_forget_password:
                    // 忘记密码
                    openNewPage(ResetPwdFragment.class);
                    break;
                case R.id.tv_reg:
                    //注册
                    openNewPage(RegFragment.class);
                    break;
                default:
                    Utils.showResponse(ResponseMsg.REQUEST_FAIL);
                    break;
            }
        } catch (Exception e) {
            Utils.showResponse(ResponseMsg.FAIL);

        }

    }

    /**
     * 登录成功的处理
     */
    private void Login() {
        //登录注册的处理
        String phoneNumber = binding.etPhoneNumber.getEditValue();
        String password = binding.etPassword.getEditValue();
        login(phoneNumber, password);

    }

    private void login(String phone, String password) {
        showLoadingDialog();
        RetrofitClient.getInstance().getApi().login(phone, password).enqueue(new retrofit2.Callback<Result<User>>() {
            @Override
            public void onResponse(retrofit2.Call<Result<User>> call, retrofit2.Response<Result<User>> response) {
                if (response.body() != null) {
                    Result<User> result = response.body();
                    if (result.isSuccess()) {
                        User user = response.body().getData();
                        Utils.doUserData(user);
                        TokenUtils.setToken(RandomUtils.getRandomLetters(6));
                        fetchIMTokenAndConnect(user);
                    } else {
                        Utils.showResponse(result.getMsg());
                    }
                } else {
                    Utils.showResponse("服务器响应为空");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Result<User>> call, Throwable t) {
                Log.e("LOGIN_ERROR", "失败详情: ", t);
                Utils.showResponse("网络请求失败");
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
                    // 首次连接：传入超时时间（例如 10 秒）
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideLoadingDialog();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    // 显示加载动画
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            Context context = getContext();
            loadingDialog = new LoadingDialog(context);
        }
        loadingDialog.show();
    }

    // 隐藏加载动画
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
