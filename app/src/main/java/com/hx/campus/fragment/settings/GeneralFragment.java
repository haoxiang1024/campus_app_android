package com.hx.campus.fragment.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.activity.LoginActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentGeneralBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.CacheClean;
import com.hx.campus.utils.common.TokenUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import com.xuexiang.xutil.XUtil;

import io.rong.imkit.IMCenter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class GeneralFragment extends BaseFragment<FragmentGeneralBinding> implements SuperTextView.OnSuperTextViewClickListener {
    @NonNull
    @Override
    protected FragmentGeneralBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentGeneralBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.generalsetting);
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        binding.menuCache.setOnSuperTextViewClickListener(this);
        // 绑定注销按钮的点击事件
        binding.menuDeleteAccount.setOnSuperTextViewClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(SuperTextView view) {
        int id = view.getId();
        if (id == R.id.menu_cache) {
            handleCacheClear();
        } else if (id == R.id.menu_delete_account) {
            // 触发注销警告弹窗
            showDeleteAccountDialog();
        }
    }

    private void handleCacheClear() {
        String cacheSize = CacheClean.getTotalCacheSize(getContext());
        if (cacheSize.equals("0.00MB")) {
            Utils.showResponse(Utils.getString(getContext(), R.string.no_cache_to_clear));
        } else {
            CacheClean.clearAllCache(getContext());
            Utils.showResponse("共清理" + cacheSize + "缓存");

        }
    }

    /**
     * 显示注销账号的二次确认弹窗
     */
    private void showDeleteAccountDialog() {
        new MaterialDialog.Builder(getContext())
                .title("严重警告")
                .content("账号注销后将无法恢复，您的个人资料、发布的内容及所有相关数据将被永久清除。确定要注销吗？")
                .positiveText("确认注销")
                .positiveColorRes(R.color.xui_config_color_red)
                .negativeText("取消")
                .onPositive((dialog, which) -> {
                    // 用户确认后，发起网络请求
                    requestDeleteAccount();
                })
                .show();
    }

    /**
     * 发起注销网络请求
     */
    private void requestDeleteAccount() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        int userId = user.getId();
        RetrofitClient.getInstance().getApi().deleteAccount(userId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.isSuccessful() && response.body() != null){
                        if (response.body().getStatus()==0){
                            Utils.showResponse("注销成功");
                            new Handler(Looper.getMainLooper()).postDelayed(this::logOut, 1000);                        }
                    }
            }

            private void logOut() {

                // 在主线程执行退出操作
                new Handler(Looper.getMainLooper()).post(() -> {
                    IMCenter.getInstance().disconnect(); // 断开融云
                    IMCenter.getInstance().logout();// 登出
                    TokenUtils.handleLogoutSuccess();   // 清除Token
                    XUtil.getActivityLifecycleHelper().exit(); // 退出所有页面
                    Context activeContext = XUtil.getContext();
                    // 彻底退出之前的页面栈
                    XUtil.getActivityLifecycleHelper().exit();
                    // 跳转至登录页
                    Intent intent = new Intent(activeContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activeContext.startActivity(intent);
                });
            }


            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Utils.showResponse("网络异常");
            }
        });

    }

}