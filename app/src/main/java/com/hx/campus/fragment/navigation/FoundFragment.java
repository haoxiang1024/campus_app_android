package com.hx.campus.fragment.navigation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayoutMediator;
import com.hx.campus.R;
import com.hx.campus.adapter.lostfound.FoundPagerAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentFoundBinding;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xui.widget.actionbar.TitleBar;

@Page
public class FoundFragment extends BaseFragment<FragmentFoundBinding> {

    // 自动注入的标题参数
    @AutoWired(name = KEY_TITLE_NAME)
    String title;
    // 标题参数键名常量
    public static final String KEY_TITLE_NAME = "title_name";

    /**
     * 初始化参数
     * 通过XRouter进行依赖注入
     */
    @Override
    protected void initArgs() {
        // 使用XRouter进行字段自动注入
        XRouter.getInstance().inject(this);
    }

    /**
     * 创建视图绑定对象
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentFoundBinding 视图绑定实例
     */
    @NonNull
    @Override
    protected FragmentFoundBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        // 使用FragmentFoundBinding inflate方法创建绑定对象
        return FragmentFoundBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化视图组件
     * 设置ViewPager和TabLayout的关联
     */
    @Override
    protected void initViews() {
        // 从资源文件获取分类标题数组
        String[] typeTitles = getResources().getStringArray(R.array.type_titles);

        // 创建招领页面适配器
        FoundPagerAdapter adapter = new FoundPagerAdapter(this, typeTitles);
        // 为ViewPager设置适配器
        binding.viewPager.setAdapter(adapter);

        // 使用TabLayoutMediator绑定TabLayout和ViewPager2，实现滑动动画效果
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            // 设置每个标签的文本内容
            tab.setText(typeTitles[position]);
        }).attach();
    }

    /**
     * 初始化标题栏
     * 添加发布按钮和设置标题
     * @return TitleBar 标题栏对象
     */
    @Override
    protected TitleBar initTitle() {
        // 获取父类初始化的标题栏
        TitleBar titleBar = super.initTitle();
        // 添加右侧发布按钮
        titleBar.addAction(new TitleBar.ImageAction(R.drawable.add) {
            /**
             * 发布按钮点击事件处理
             * @param view 按钮视图
             */
            @Override
            public void performAction(android.view.View view) {
                // 跳转到招领信息发布页面
                openPage(AddFoundFragment.class);
            }
        });
        // 设置页面标题为"招领"
        titleBar.setTitle("招领");
        return titleBar;
    }

}