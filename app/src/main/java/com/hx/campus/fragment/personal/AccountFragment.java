package com.hx.campus.fragment.personal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class AccountFragment extends BaseFragment<FragmentAccountBinding> implements View.OnClickListener {

    private ImageView imgView;
    private User currentUser;
    private CountDownTimer countDownTimer;

    // 记录修改安全信息类型：0=无，1=手机号，2=邮箱
    private int currentModifySecurityType = 0;

    @NonNull
    @Override
    protected FragmentAccountBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentAccountBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        imgView = binding.rivHeadPic;
        initData();
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        binding.tvResetPwd.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        imgView.setOnClickListener(this);

        binding.tvBtnModifyPhone.setOnClickListener(this);
        binding.tvBtnModifyEmail.setOnClickListener(this);
        binding.tvSendVerifyCode.setOnClickListener(this);
        binding.btnConfirmSecurityModify.setOnClickListener(this);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initData() {
        // 初始化回显数据
        currentUser = Utils.getBeanFromSp(getContext(), "User", "user");
        if (currentUser == null) return;

        if (TextUtils.isEmpty(currentUser.getPhoto())) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
            Glide.with(this).load(currentUser.getPhoto()).into(imgView);
        }

        binding.etNickName.setText(currentUser.getNickname());

        if ("男".equals(currentUser.getSex())) {
            binding.rbMan.setChecked(true);
        } else {
            binding.rbWomen.setChecked(true);
        }

        binding.regDate.setText("注册日期：" + Utils.dateFormat(currentUser.getReg_date()));
        binding.tvPhoneValue.setText("手机号：" + currentUser.getPhone());

        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "未绑定";
        binding.tvEmailValue.setText("邮箱：" + email);
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
            updateBasicInfo();
        } else if (id == R.id.riv_head_pic) {
            openNewPage(PhotoFragment.class);
        } else if (id == R.id.tv_btn_modify_phone) {
            showSecurityVerifyContainer(1);
        } else if (id == R.id.tv_btn_modify_email) {
            showSecurityVerifyContainer(2);
        } else if (id == R.id.tv_send_verify_code) {
            sendVerifyCodeToOldEmail();
        } else if (id == R.id.btn_confirm_security_modify) {
            submitSecurityInfoUpdate();
        }
    }

    // 展开验证面板及UI重置
    private void showSecurityVerifyContainer(int type) {
        currentModifySecurityType = type;
        binding.llSecurityVerifyContainer.setVisibility(View.VISIBLE);

        binding.etEmailVerifyCode.setText("");
        binding.etNewAccountValue.setText("");

        if (type == 1) {
            binding.tvVerifyTitle.setText("验证已有邮箱以修改手机号");
            binding.etNewAccountValue.setHint("请输入新手机号");
        } else {
            binding.tvVerifyTitle.setText("验证已有邮箱以修改邮箱");
            binding.etNewAccountValue.setHint("请输入新邮箱");
        }
    }

    // 发送验证码校验与执行
    private void sendVerifyCodeToOldEmail() {
        // 实时获取缓存校验邮箱绑定状态
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            Utils.showResponse("当前账号未绑定邮箱，无法验证！");
            return;
        }
        String oldEmail = user.getEmail();

        // 发送验证码
        RetrofitClient.getInstance().getApi().sendCode(oldEmail).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Utils.showResponse("验证码已发送至 " + oldEmail);
                        startCountDownTimer();
                    } else {
                        Utils.showResponse(response.body().getMsg());
                    }
                } else {
                    Utils.showResponse("发送失败，请稍后重试");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                Utils.showResponse("网络异常：" + t.getMessage());
            }
        });
    }

    // 验证码倒计时处理
    private void startCountDownTimer() {
        binding.tvSendVerifyCode.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvSendVerifyCode.setText(millisUntilFinished / 1000 + "秒后重试");
            }

            @Override
            public void onFinish() {
                binding.tvSendVerifyCode.setEnabled(true);
                binding.tvSendVerifyCode.setText("获取验证码");
            }
        }.start();
    }

    // 提交安全信息更新校验与执行
    @SuppressLint("SetTextI18n")
    private void submitSecurityInfoUpdate() {
        String verifyCode = binding.etEmailVerifyCode.getText().toString().trim();
        String newValue = binding.etNewAccountValue.getText().toString().trim();

        if (TextUtils.isEmpty(verifyCode)) {
            Utils.showResponse("请输入验证码");
            return;
        }
        if (TextUtils.isEmpty(newValue)) {
            Utils.showResponse(currentModifySecurityType == 1 ? "请输入新手机号" : "请输入新邮箱");
            return;
        }

        // 实时获取缓存校验用户状态
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) {
            Utils.showResponse("未获取到用户信息，请重新登录");
            return;
        }

        Call<Result<User>> call;
        if (currentModifySecurityType == 1) {
            call = RetrofitClient.getInstance().getApi().updatePhone(user.getId(), newValue, verifyCode);
        } else {
            call = RetrofitClient.getInstance().getApi().updateEmail(user.getId(), newValue, verifyCode);
        }

        // 提交修改请求
        call.enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(@NonNull Call<Result<User>> call, @NonNull Response<Result<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<User> result = response.body();
                    if (result.isSuccess()) {
                        Utils.doUserData(result.getData());
                        currentUser = result.getData();

                        binding.llSecurityVerifyContainer.setVisibility(View.GONE);
                        binding.etEmailVerifyCode.setText("");
                        binding.etNewAccountValue.setText("");

                        binding.tvPhoneValue.setText("手机号：" + currentUser.getPhone());
                        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "未绑定";
                        binding.tvEmailValue.setText("邮箱：" + email);

                        Utils.showResponse(currentModifySecurityType == 1 ? "手机号修改成功" : "邮箱修改成功");
                    } else {
                        Utils.showResponse(result.getMsg());
                    }
                } else {
                    Utils.showResponse("操作失败，请重试");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<User>> call, @NonNull Throwable t) {
                Utils.showResponse("网络异常：" + t.getMessage());
            }
        });
    }

    // 提交基础信息校验与执行
    private void updateBasicInfo() {
        String nickName = binding.etNickName.getText().toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            Utils.showResponse(Utils.getString(getContext(), R.string.nickname_cannot_be_empty));
            return;
        }

        // 实时获取缓存校验用户状态
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) {
            Utils.showResponse("未获取到用户信息，请重新登录");
            return;
        }

        int id = user.getId();
        String sex = binding.rbMan.isChecked() ? "男" : "女";

        // 更新基础资料请求
        RetrofitClient.getInstance().getApi().updateAccount(nickName, sex, id).enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(@NonNull Call<Result<User>> call, @NonNull Response<Result<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<User> result = response.body();
                    if (result.isSuccess()) {
                        Utils.doUserData(result.getData());
                        User latestUser = Utils.getBeanFromSp(getContext(), "User", "user");

                        UserInfo userInfo = new UserInfo(
                                String.valueOf(latestUser.getId()),
                                latestUser.getNickname(),
                                Uri.parse(latestUser.getPhoto())
                        );
                        RongUserInfoManager.getInstance().refreshUserInfoCache(userInfo);

                        Utils.showResponse("基础资料修改成功！");
                        binding.getRoot().postDelayed(() -> {
                            if (isAdded()) {
                                startActivity(new Intent(getContext(), MainActivity.class));
                            }
                        }, 800);
                    } else {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 销毁倒计时避免泄露
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}