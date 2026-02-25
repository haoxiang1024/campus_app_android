package com.hx.campus.adapter.message;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hx.campus.fragment.message.ChatListFragment;
import com.hx.campus.fragment.message.InteractionFragment;

/**
 * 消息页面ViewPager适配器
 * 管理消息模块的两个主要页面：私信列表和互动消息
 * 继承FragmentStateAdapter实现页面状态保存
 */
public class MessagePagerAdapter extends FragmentStateAdapter {

    /**
     * 构造函数
     * 
     * @param fragment 父Fragment
     */
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
