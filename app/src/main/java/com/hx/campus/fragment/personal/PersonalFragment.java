
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



@Page(anim = CoreAnim.none)
public class PersonalFragment extends BaseFragment<FragmentProfileBinding> implements SuperTextView.OnSuperTextViewClickListener {

    
    @NonNull
    @Override
    protected FragmentProfileBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {

        return FragmentProfileBinding.inflate(inflater, container, attachToRoot);
    }
    
    @Override
    protected String getPageTitle() {

        return getResources().getString(R.string.menu_profile);
    }
    
    @Override
    protected TitleBar initTitle() {

        return null;
    }

    
    @Override
    protected void initViews() {

        initAc();
        SuperTextView menuScan = findViewById(R.id.menu_scan);
        if (menuScan != null) {
            menuScan.setOnClickListener(v -> startQrCodeScanner());
        }
    }

    
    private void initAc() {

        User user = Utils.getBeanFromSp(getContext(), "User", "user");

        if (TextUtils.isEmpty(user.getPhoto())) {

            binding.rivHeadPic.setVisibility(View.GONE);
        } else {

            binding.rivHeadPic.setVisibility(View.VISIBLE);

            Glide.with(this).load(user.getPhoto()).into(binding.rivHeadPic);
        }
    }

    
    @Override
    protected void initListeners() {

        binding.photo.setOnSuperTextViewClickListener(this);

        binding.account.setOnSuperTextViewClickListener(this);

        binding.tips.setOnSuperTextViewClickListener(this);

        binding.suggestion.setOnSuperTextViewClickListener(this);

        binding.menuSettings.setOnSuperTextViewClickListener(this);

        binding.menuAbout.setOnSuperTextViewClickListener(this);

        binding.points.setOnSuperTextViewClickListener(this);
    }

    
    @SuppressLint("NonConstantResourceId")
 
    @Override
    public void onClick(SuperTextView view) {

        int id = view.getId();
        switch (id) {
            case R.id.photo:

                openNewPage(PhotoFragment.class);
                break;
            case R.id.account:

                openNewPage(AccountFragment.class);
                break;
            case R.id.tips:

                AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/notification.html", getContext()));
                break;
            case R.id.suggestion:

                openNewPage(SuggestionFragment.class);
                break;
            case R.id.menu_settings:

                openNewPage(SettingsFragment.class);
                break;
            case R.id.menu_about:

                openNewPage(AboutFragment.class);
                break;
            case R.id.points:

                    openNewPage(ShopFragment.class);
                    break;

        }
    }
    
    private void startQrCodeScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("请对准二维码进行扫描");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CustomScannerActivity.class);
        integrator.initiateScan();
    }

    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                XToastUtils.info("已取消扫码");
            } else {

                String scannedContent = result.getContents().trim();
                handleScannedResult(scannedContent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    
    private void handleScannedResult(String content) {

        if (content.toLowerCase().startsWith("http://") || content.toLowerCase().startsWith("https://")) {

            AgentWebActivity.goWeb(getContext(), content);
            return;
        }


        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user != null && user.getRole() == 1) {
            if (content.length() == 8) {
                requestVerifyOrder(content.toUpperCase(), user.getId());
            } else {
                XToastUtils.warning("无法识别的核验码：" + content);
            }
        } else {

            new MaterialDialog.Builder(getContext())
                    .title("扫描结果")
                    .content("您不是管理员，无法核验商品")
                    .positiveText("关闭")
                    .show();
        }
    }

    
    private void requestVerifyOrder(String verifyCode, int adminId) {

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
