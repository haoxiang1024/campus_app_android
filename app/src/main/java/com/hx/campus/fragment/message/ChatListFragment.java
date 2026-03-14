package com.hx.campus.fragment.message;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hx.campus.R;
import com.xuexiang.xpage.annotation.Page;

import io.rong.imkit.conversationlist.ConversationListAdapter;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.widget.adapter.ViewHolder;

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
        background(view);

    }

    private void background(@NonNull View view) {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(com.xuexiang.xui.R.attr.xui_config_color_background, typedValue, true);
        int backgroundColor = typedValue.data;
        view.setBackgroundColor(backgroundColor);
    }
    @Override
    protected ConversationListAdapter onResolveAdapter() {
        return new ConversationListAdapter() {
            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                //设置聊天列表项的背景色
                TypedValue typedValue = new TypedValue();
                getContext().getTheme().resolveAttribute(com.xuexiang.xui.R.attr.xui_config_color_background, typedValue, true);
                int backgroundColor = typedValue.data;
                holder.itemView.setBackgroundColor(backgroundColor);
            }
        };
    }
}
