package com.hx.campus.adapter.lostfound;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hx.campus.fragment.navigation.FoundListSubFragment;
import com.hx.campus.fragment.navigation.LostListSubFragment;

public class FoundPagerAdapter extends FragmentStateAdapter {
    private final String[] titles;

    public FoundPagerAdapter(@NonNull Fragment fragment, String[] titles) {
        super(fragment);
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FoundListSubFragment.newInstance(titles[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}

