package com.hx.campus.fragment.navigation;

import android.view.LayoutInflater;
import android.view.View;
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

    @AutoWired(name = KEY_TITLE_NAME)
    String title;
    public static final String KEY_TITLE_NAME = "title_name";

    @Override
    protected void initArgs() {
        XRouter.getInstance().inject(this);
    }

    @NonNull
    @Override
    protected FragmentFoundBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentFoundBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        // 获取分类标题
        String[] typeTitles = getResources().getStringArray(R.array.type_titles);

        //  设置适配器
        FoundPagerAdapter adapter = new FoundPagerAdapter(this, typeTitles);
        binding.viewPager.setAdapter(adapter);

        // 绑定 TabLayout 和 ViewPager2（这是动画的关键）
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(typeTitles[position]);
        }).attach();
    }

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle();
        titleBar.addAction(new TitleBar.ImageAction(R.drawable.add) {
            @Override
            public void performAction(android.view.View view) {
                // 跳转到发布页
                openPage(AddFoundFragment.class);
            }
        });
        titleBar.setTitle("招领");
        return titleBar;
    }

}