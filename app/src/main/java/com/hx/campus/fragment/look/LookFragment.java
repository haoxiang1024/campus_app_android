
package com.hx.campus.fragment.look;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentTrendingBinding;
import com.hx.campus.fragment.message.MyCommentsFragment;
import com.hx.campus.fragment.message.MyMessagesFragment;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;



@Page(anim = CoreAnim.none)
public class LookFragment extends BaseFragment<FragmentTrendingBinding> implements SuperTextView.OnSuperTextViewClickListener {

    
    @NonNull
    @Override
    protected FragmentTrendingBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {

        return FragmentTrendingBinding.inflate(inflater, container, attachToRoot);
    }

    
    @Override
    protected TitleBar initTitle() {

        return null;
    }

    
    @Override
    protected void initViews() {
    }

    
    @Override
    protected void initListeners() {

        super.initListeners();

        binding.lost.setOnSuperTextViewClickListener(this);

        binding.found.setOnSuperTextViewClickListener(this);

        binding.myComments.setOnSuperTextViewClickListener(this);
        binding.myMessage.setOnSuperTextViewClickListener(this);

    }

    
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(SuperTextView view) {

        if (view.getId() == binding.lost.getId()) {

            openNewPage(LostInfoFragment.class);
        } 

        else if (view.getId() == binding.found.getId()) {

            openNewPage(FoundInfoFragment.class);
        }else if (view.getId() == binding.myComments.getId()) {

            openNewPage(MyCommentsFragment.class);
        } else if (view.getId()==binding.myMessage.getId()) {

            openNewPage(MyMessagesFragment.class);
        }
    }
}
