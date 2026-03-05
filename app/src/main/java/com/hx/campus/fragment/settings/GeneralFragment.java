package com.hx.campus.fragment.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.hx.campus.R;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentGeneralBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.common.CacheClean;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

@Page()
public class GeneralFragment extends BaseFragment<FragmentGeneralBinding> implements SuperTextView.OnSuperTextViewClickListener {

    // 定义 SP 的名称
    private static final String SP_NAME = "config_settings";
    private static final String KEY_FOLLOW_SYSTEM = "is_follow_system";

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
        // 直接读取 SP
        SharedPreferences sp = requireContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        boolean isFollow = sp.getBoolean(KEY_FOLLOW_SYSTEM, true);
        binding.menuDarkMode.setRightString(isFollow ? "跟随系统" : "手动模式");
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        binding.menuCache.setOnSuperTextViewClickListener(this);
        binding.menuDarkMode.setOnSuperTextViewClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(SuperTextView view) {
        int id = view.getId();
        if (id == R.id.menu_cache) {
            handleCacheClear();
        } else if (id == R.id.menu_dark_mode) {
            showDarkModeDialog();
        }
    }

    private void showDarkModeDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_dark_mode_settings, null);
        SwitchCompat switchFollowSystem = dialogView.findViewById(R.id.switch_follow_system);

        // 读取当前状态
        SharedPreferences sp = requireContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        switchFollowSystem.setChecked(sp.getBoolean(KEY_FOLLOW_SYSTEM, true));

        new MaterialDialog.Builder(getContext())
                .customView(dialogView, true)
                .title("显示设置")
                .positiveText("确定")
                .onPositive((dialog, which) -> {
                    boolean checked = switchFollowSystem.isChecked();

                    // 写入 SP
                    sp.edit().putBoolean(KEY_FOLLOW_SYSTEM, checked).apply();

                    if (checked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        binding.menuDarkMode.setRightString("跟随系统");
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        binding.menuDarkMode.setRightString("普通模式");
                    }
                })
                .show();
    }

    private void handleCacheClear() {
        String cacheSize = CacheClean.getTotalCacheSize(getContext());
        if (cacheSize.equals("0.00MB")) {
            Utils.showResponse(Utils.getString(getContext(), R.string.no_cache_to_clear));
        } else {
            CacheClean.clearAllCache(getContext());
            if ("zh".equals(Utils.language(getContext()))) {
                Utils.showResponse("共清理" + cacheSize + "缓存");
            } else {
                Utils.showResponse("Clean " + cacheSize + " caches");
            }
        }
    }
}