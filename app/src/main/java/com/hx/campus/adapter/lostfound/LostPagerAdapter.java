package com.hx.campus.adapter.lostfound;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hx.campus.fragment.navigation.LostListSubFragment;

public class LostPagerAdapter extends FragmentStateAdapter {
    private final String[] titles;

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
