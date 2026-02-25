package com.hx.campus.fragment.message;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hx.campus.adapter.message.MessagePagerAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentMessageMainBinding;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;

@Page
public class MessageMainFragment extends BaseFragment<FragmentMessageMainBinding> {
    // 标签页布局控件，用于显示顶部标签
    private TabLayout tabLayout;
    // ViewPager2控件，用于实现页面滑动切换
    private ViewPager2 viewPager;
    /**
     * 创建视图绑定对象
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentMessageMainBinding 视图绑定实例
     */
    @NonNull
    @Override
    protected FragmentMessageMainBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        // 使用FragmentMessageMainBinding inflate方法创建绑定对象
        return FragmentMessageMainBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化视图组件
     * 设置TabLayout和ViewPager2的关联
     */
    @Override
    protected void initViews() {
        // 获取TabLayout引用
        tabLayout = binding.tabLayout;
        // 获取ViewPager2引用
        viewPager = binding.viewPager;

        // 创建消息页面适配器
        MessagePagerAdapter adapter = new MessagePagerAdapter(this);
        // 为ViewPager2设置适配器
        viewPager.setAdapter(adapter);

        // 使用TabLayoutMediator将TabLayout和ViewPager2进行绑定
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            /**
             * 配置每个标签页的显示内容
             * @param tab TabLayout.Tab标签对象
             * @param position 标签位置索引
             */
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                // 根据位置设置对应标签的文字
                if (position == 0) {
                    // 第一个标签显示"私信"
                    tab.setText("私信");
                } else {
                    // 第二个标签显示"评论"
                    tab.setText("评论");
                }
            }
        }).attach();
    }

    /**
     * 初始化标题栏
     * @return TitleBar 标题栏对象，返回null表示不使用默认标题栏
     */
    @Override
    protected TitleBar initTitle() {
        // 消息主页不需要显示标题栏
        return null;
    }
}
