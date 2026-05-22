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

    private TabLayout tabLayout;

    private ViewPager2 viewPager;
    
    @NonNull
    @Override
    protected FragmentMessageMainBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {

        return FragmentMessageMainBinding.inflate(inflater, container, attachToRoot);
    }

    
    @Override
    protected void initViews() {

        tabLayout = binding.tabLayout;

        viewPager = binding.viewPager;


        MessagePagerAdapter adapter = new MessagePagerAdapter(this);

        viewPager.setAdapter(adapter);


        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                if (position == 0) {

                    tab.setText("私信");
                } else {

                    tab.setText("评论");
                }
            }
        }).attach();
    }

    
    @Override
    protected TitleBar initTitle() {

        return null;
    }
}
