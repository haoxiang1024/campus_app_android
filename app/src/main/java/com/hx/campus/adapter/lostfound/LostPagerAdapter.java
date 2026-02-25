package com.hx.campus.adapter.lostfound;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hx.campus.fragment.navigation.LostListSubFragment;

/**
 * 丢失物品ViewPager适配器
 * 用于管理丢失物品分类页面的Fragment切换
 * 继承FragmentStateAdapter实现页面状态保存
 */
public class LostPagerAdapter extends FragmentStateAdapter {
    /** 页面标题数组 */
    private final String[] titles;

    /**
     * 构造函数
     * 
     * @param fragment 父Fragment
     * @param titles 页面标题数组
     */
    public LostPagerAdapter(@NonNull Fragment fragment, String[] titles) {
        super(fragment);
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return LostListSubFragment.newInstance(titles[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
