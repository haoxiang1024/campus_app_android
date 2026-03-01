
package com.hx.campus.fragment.personal;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.core.webview.AgentWebActivity;
import com.hx.campus.databinding.FragmentProfileBinding;
import com.hx.campus.fragment.other.AboutFragment;
import com.hx.campus.fragment.settings.SettingsFragment;
import com.hx.campus.utils.Utils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;


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
                // 跳转到公告页面，根据当前语言选择对应的页面
                // 获取应用当前语言设置
                String currentLanguage = Utils.language(getContext());
                if(currentLanguage.equals("zh")){
                    // 中文环境跳转到中文公告页
                    AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/notification.html", getContext()));
                } else if (currentLanguage.equals("en")) {
                    // 英文环境跳转到英文公告页
                    AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/notification_en.html", getContext()));
                }
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
        }
    }
}
