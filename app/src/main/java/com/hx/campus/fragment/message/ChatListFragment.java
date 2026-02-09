package com.hx.campus.fragment.message;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;

//私信页
@Page
public class ChatListFragment extends BaseFragment<LayoutCommonListBinding> {
    @NonNull
    @Override
    protected LayoutCommonListBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return LayoutCommonListBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected TitleBar initTitle() {
        return null;
    }
}
