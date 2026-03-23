package com.hx.campus.fragment.personal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
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

    private ImageView imgView;//头像组件
    private User currentUser;
    private CountDownTimer countDownTimer; // 验证码倒计时

    // 用于记录当前正在修改的安全信息类型：0=无，1=手机号，2=邮箱
    private int currentModifySecurityType = 0;

    @NonNull
    @Override
    protected FragmentAccountBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentAccountBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        imgView = binding.rivHeadPic;
        initData(); // 初始化账户数据
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        // 基础信息与通用
        binding.tvResetPwd.setOnClickListener(this); // 重置密码
        binding.btnSubmit.setOnClickListener(this); // 提交基础信息（昵称、性别）
        imgView.setOnClickListener(this); // 修改头像

        // 安全信息验证相关
        binding.tvBtnModifyPhone.setOnClickListener(this); // 点击修改手机号
        binding.tvBtnModifyEmail.setOnClickListener(this); // 点击修改邮箱
        binding.tvSendVerifyCode.setOnClickListener(this); // 发送验证码到原邮箱
        binding.btnConfirmSecurityModify.setOnClickListener(this); // 提交安全信息修改
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initData() {
        // 获取用户信息
        currentUser = Utils.getBeanFromSp(getContext(), "User", "user");
        if (currentUser == null) return;

        // 设置头像
        if (TextUtils.isEmpty(currentUser.getPhoto())) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
            Glide.with(this).load(currentUser.getPhoto()).into(imgView);
        }

        binding.etNickName.setText(currentUser.getNickname());

        // 设置性别
        if ("男".equals(currentUser.getSex())) {
            binding.rbMan.setChecked(true);
        } else {
            binding.rbWomen.setChecked(true);
        }

        // 设置注册日期、手机号与邮箱
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
            updateBasicInfo(); // 仅更新无需验证的昵称和性别
        } else if (id == R.id.riv_head_pic) {
            openNewPage(PhotoFragment.class);
        } else if (id == R.id.tv_btn_modify_phone) {
            showSecurityVerifyContainer(1); // 展开手机修改面板
        } else if (id == R.id.tv_btn_modify_email) {
            showSecurityVerifyContainer(2); // 展开邮箱修改面板
        } else if (id == R.id.tv_send_verify_code) {
            sendVerifyCodeToOldEmail(); // 发送验证码
        } else if (id == R.id.btn_confirm_security_modify) {
            submitSecurityInfoUpdate(); // 提交手机/邮箱的修改
        }
    }

    /**
     * 展开安全验证区域并动态设置UI文案
     * @param type 1: 修改手机号, 2: 修改邮箱
     */
    private void showSecurityVerifyContainer(int type) {
        currentModifySecurityType = type;
        binding.llSecurityVerifyContainer.setVisibility(View.VISIBLE);

        // 清空之前的输入
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

    /**
     * 向已有邮箱发送验证码
     */
    private void sendVerifyCodeToOldEmail() {
        if (currentUser == null || TextUtils.isEmpty(currentUser.getEmail())) {
            Utils.showResponse("当前账号未绑定邮箱，无法验证！");
            return;
        }

        String oldEmail = currentUser.getEmail();
        // 发起网络请求发送验证码
        RetrofitClient.getInstance().getApi().sendCode(oldEmail).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Utils.showResponse("验证码已发送至 " + oldEmail);
                        startCountDownTimer(); // 启动倒计时
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

    /**
     * 验证码倒计时方法
     */
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

    /**
     * 提交安全信息（手机或邮箱）的修改
     */
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

        Call<Result<User>> call;
        if (currentModifySecurityType == 1) {
            call = RetrofitClient.getInstance().getApi().updatePhone(currentUser.getId(), newValue, verifyCode);
        } else {
            call = RetrofitClient.getInstance().getApi().updateEmail(currentUser.getId(), newValue, verifyCode);
        }

        call.enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(@NonNull Call<Result<User>> call, @NonNull Response<Result<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<User> result = response.body();
                    if (result.isSuccess()) {
                        //  更新本地SP缓存和当前对象
                        Utils.doUserData(result.getData());
                        currentUser = result.getData();

                        //  隐藏验证面板并清空输入
                        binding.llSecurityVerifyContainer.setVisibility(View.GONE);
                        binding.etEmailVerifyCode.setText("");
                        binding.etNewAccountValue.setText("");

                        // 刷新UI上的手机/邮箱显示
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

    /**
     * 更新基础信息（昵称、性别） - 无需验证
     */
    private void updateBasicInfo() {
        String nickName = binding.etNickName.getText().toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            Utils.showResponse(Utils.getString(getContext(), R.string.nickname_cannot_be_empty));
            return;
        }

        if (currentUser == null) return;

        int id = currentUser.getId();
        String sex = binding.rbMan.isChecked() ? "男" : "女";

        // 发起 Retrofit 请求更新基础资料
        RetrofitClient.getInstance().getApi().updateAccount(nickName, sex, id).enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(@NonNull Call<Result<User>> call, @NonNull Response<Result<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<User> result = response.body();
                    if (result.isSuccess()) {
                        // 使用返回的最新的 User 数据更新本地缓存
                        Utils.doUserData(result.getData());
                        User user = Utils.getBeanFromSp(getContext(), "User", "user");

                        // IM刷新
                        UserInfo userInfo = new UserInfo(
                                String.valueOf(user.getId()),
                                user.getNickname(),
                                Uri.parse(user.getPhoto())
                        );
                        RongUserInfoManager.getInstance().refreshUserInfoCache(userInfo);

                        // 提示成功并跳转
                        Utils.showResponse("基础资料修改成功！");
                        binding.getRoot().postDelayed(() -> {
                            if (isAdded()) { // 检查 Fragment 是否还在 Activity 中
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
        // 防止内存泄漏，页面销毁时取消倒计时
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}