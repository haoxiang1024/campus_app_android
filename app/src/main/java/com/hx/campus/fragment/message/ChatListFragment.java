package com.hx.campus.fragment.message;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.LayoutCommonListBinding;
import com.hx.campus.utils.Utils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.UserInfo;

// 私信聊天列表页面 - 集成融云IMKit实现聊天功能
@Page
public class ChatListFragment extends ConversationListFragment {
    
    /**
     * 视图创建完成回调
     * @param view Fragment的根视图
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 调用父类的视图创建方法
        super.onViewCreated(view, savedInstanceState);
        // 此处可以添加自定义的视图初始化逻辑
    }
}
