package com.hx.campus.adapter.message;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hx.campus.fragment.message.ChatListFragment;
import com.hx.campus.fragment.message.InteractionFragment;

public class MessagePagerAdapter extends FragmentStateAdapter {

    public MessagePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 根据位置返回对应的 Fragment
        if (position == 0) {
            return new ChatListFragment();
        } else {
            return new InteractionFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 总共两个页面：私信、互动
    }
}
