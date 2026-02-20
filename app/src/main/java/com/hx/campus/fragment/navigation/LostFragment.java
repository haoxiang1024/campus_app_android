package com.hx.campus.fragment.navigation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayoutMediator;
import com.hx.campus.R;
import com.hx.campus.adapter.lostfound.LostPagerAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLostBinding;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xui.widget.actionbar.TitleBar;

@Page
public class LostFragment extends BaseFragment<FragmentLostBinding> {

    @AutoWired(name = KEY_TITLE_NAME)
    String title;
    public static final String KEY_TITLE_NAME = "title_name";

    @Override
    protected void initArgs() {
        XRouter.getInstance().inject(this);
    }

    @NonNull
    @Override
    protected FragmentLostBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentLostBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        String[] typeTitles = getResources().getStringArray(R.array.type_titles);
        LostPagerAdapter adapter = new LostPagerAdapter(this, typeTitles);
        binding.viewPager.setAdapter(adapter);
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
                openPage(AddLostFragment.class);
            }
        });
        titleBar.setTitle("失物");
        return titleBar;
    }
}