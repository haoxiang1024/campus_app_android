package com.hx.campus.fragment.personal;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.core.webview.AgentWebActivity;
import com.hx.campus.databinding.FragmentSuggestionBinding;
import com.hx.campus.utils.Utils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

@Page()
public class SuggestionFragment extends BaseFragment<FragmentSuggestionBinding> implements SuperTextView.OnSuperTextViewClickListener, View.OnClickListener {


    
    @NonNull
    @Override
    protected FragmentSuggestionBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentSuggestionBinding.inflate(inflater, container, attachToRoot);
    }
    
    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.help);
    }
    
    @Override
    protected void initViews() {

    }

    @Override
    protected void initListeners() {
        super.initListeners();
        binding.hot1.setOnSuperTextViewClickListener(this);
        binding.hot2.setOnSuperTextViewClickListener(this);
        binding.hot3.setOnSuperTextViewClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(SuperTextView view) {
        int id = view.getId();
        switch (id) {
            case R.id.hot1:

                AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/appcrash.html", getContext()));
                break;
            case R.id.hot2:

                AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/account.html", getContext()));
                break;
            case R.id.hot3:

                AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/privacy.html", getContext()));
                break;
        }

    }

    
    @Override
    public void onClick(View v) {

    }
}