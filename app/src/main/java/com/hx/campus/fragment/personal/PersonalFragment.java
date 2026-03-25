
package com.hx.campus.fragment.personal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hx.campus.R;
import com.hx.campus.activity.CustomScannerActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.core.webview.AgentWebActivity;
import com.hx.campus.databinding.FragmentProfileBinding;
import com.hx.campus.fragment.other.AboutFragment;
import com.hx.campus.fragment.settings.SettingsFragment;
import com.hx.campus.fragment.shop.ShopFragment;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


// 个人中心页面 - 用户个人信息管理和设置入口
@Page(anim = CoreAnim.none)
public class PersonalFragment extends BaseFragment<FragmentProfileBinding> implements SuperTextView.OnSuperTextViewClickListener {

    /**
     * 创建视图绑定对象
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentProfileBinding 视图绑定实例
     */
    @NonNull
    @Override
    protected FragmentProfileBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        // 使用FragmentProfileBinding inflate方法创建绑定对象
        return FragmentProfileBinding.inflate(inflater, container, attachToRoot);
    }
    /**
     * 获取页面标题
     * @return String 页面标题字符串
     */
    @Override
    protected String getPageTitle() {
        // 从资源文件获取个人中心标题
        return getResources().getString(R.string.menu_profile);
    }
    /**
     * 初始化标题栏
     * @return TitleBar 标题栏对象，返回null表示不使用默认标题栏
     */
    @Override
    protected TitleBar initTitle() {
        // 个人中心页面不需要显示标题栏
        return null;
    }

    /**
     * 初始化视图控件
     * 初始化用户账户相关数据显示
     */
    @Override
    protected void initViews() {
        // 初始化账户数据和用户信息显示
        initAc();
        SuperTextView menuScan = findViewById(R.id.menu_scan);
        if (menuScan != null) {
            menuScan.setOnClickListener(v -> startQrCodeScanner());
        }
    }

    /**
     * 初始化账户信息显示
     * 从本地存储获取用户数据并更新界面
     */
    private void initAc() {
        // 从SharedPreferences获取存储的用户对象
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        // 设置用户头像显示
        if (TextUtils.isEmpty(user.getPhoto())) {
            // 头像URL为空时隐藏头像控件
            binding.rivHeadPic.setVisibility(View.GONE);
        } else {
            // 头像URL存在时显示头像控件
            binding.rivHeadPic.setVisibility(View.VISIBLE);
            // 使用Glide加载用户头像
            Glide.with(this).load(user.getPhoto()).into(binding.rivHeadPic);
        }
    }

    /**
     * 初始化事件监听器
     * 为各个功能按钮设置点击监听
     */
    @Override
    protected void initListeners() {
        // 为头像设置按钮添加点击监听
        binding.photo.setOnSuperTextViewClickListener(this);
        // 为账户管理按钮添加点击监听
        binding.account.setOnSuperTextViewClickListener(this);
        // 为公告按钮添加点击监听
        binding.tips.setOnSuperTextViewClickListener(this);
        // 为意见反馈按钮添加点击监听
        binding.suggestion.setOnSuperTextViewClickListener(this);
        // 为设置按钮添加点击监听
        binding.menuSettings.setOnSuperTextViewClickListener(this);
        // 为关于按钮添加点击监听
        binding.menuAbout.setOnSuperTextViewClickListener(this);
        //积分商城
        binding.points.setOnSuperTextViewClickListener(this);
    }

    /**
     * 处理SuperTextView点击事件
     * 根据点击的按钮执行相应功能
     * @param view 被点击的SuperTextView控件
     */
    @SuppressLint("NonConstantResourceId")
 
    @Override
    public void onClick(SuperTextView view) {
        // 获取被点击控件的ID
        int id = view.getId();
        switch (id) {
            case R.id.photo:
                // 跳转到头像设置页面
                openNewPage(PhotoFragment.class);
                break;
            case R.id.account:
                // 跳转到账户管理页面
                openNewPage(AccountFragment.class);
                break;
            case R.id.tips:
                // 跳转到公告页面
                AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/notification.html", getContext()));
                break;
            case R.id.suggestion:
                // 跳转到意见反馈页面
                openNewPage(SuggestionFragment.class);
                break;
            case R.id.menu_settings:
                // 跳转到设置页面
                openNewPage(SettingsFragment.class);
                break;
            case R.id.menu_about:
                // 跳转到关于页面
                openNewPage(AboutFragment.class);
                break;
            case R.id.points:
                    // 跳转到积分商城页面
                    openNewPage(ShopFragment.class);
                    break;

        }
    }
    /**
     * 调起 ZXing 扫码摄像头
     */
    private void startQrCodeScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("请对准二维码进行扫描");
        integrator.setCameraId(0); // 0是后置摄像头
        integrator.setBeepEnabled(true); // 扫码成功时“滴”一声
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true); // 锁定方向
        integrator.setCaptureActivity(CustomScannerActivity.class); // 指定自定义的扫描页面
        integrator.initiateScan();
    }

    /**
     * 接收扫码结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            // 获取扫到的内容，并交给智能路由处理
            String scannedContent = result.getContents().trim();
            handleScannedResult(scannedContent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 智能处理扫码结果：网页跳转 or 订单核验
     */
    private void handleScannedResult(String content) {
        // 判断是否是网页链接
        if (content.toLowerCase().startsWith("http://") || content.toLowerCase().startsWith("https://")) {
            // 使用AgentWebActivity 直接打开网页
            AgentWebActivity.goWeb(getContext(), content);
            return;
        }

        //  如果不是链接，判断当前用户是否是管理员进行核验
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user != null && user.getRole() == 1) { // role == 1 为管理员
            if (content.length() == 8) {
                requestVerifyOrder(content.toUpperCase(), user.getId());
            } else {
                XToastUtils.warning("无法识别的核验码：" + content);
            }
        } else {
            // 普通用户扫了无法识别的普通文本
            new MaterialDialog.Builder(getContext())
                    .title("扫描结果")
                    .content("您不是管理员，无法核验商品")
                    .positiveText("关闭")
                    .show();
        }
    }

    /**
     * 调用 ApiService 发起核验请求
     */
    private void requestVerifyOrder(String verifyCode, int adminId) {
        Utils.showResponse("正在核验中...");

        RetrofitClient.getInstance().getApi().verifyOrder(verifyCode, adminId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().getStatus() == 0) {
                    new MaterialDialog.Builder(getContext())
                            .title("✅ 核验成功")
                            .content(response.body().getMsg())
                            .positiveText("完成")
                            .show();
                } else {
                    XToastUtils.error("核验失败：" + (response.body() != null ? response.body().getMsg() : "未知原因"));
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                XToastUtils.error("网络请求失败，请检查网络");
            }
        });
    }
}
