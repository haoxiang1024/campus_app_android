package com.hx.campus.fragment.message;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imlib.RongCoreClient;

//私信页
@Page
public class ChatListFragment extends ConversationListFragment {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("ChatListFragment: ",RongCoreClient.getInstance().getCurrentConnectionStatus().getMessage() );

    }
}
