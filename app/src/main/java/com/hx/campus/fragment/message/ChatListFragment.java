package com.hx.campus.fragment.message;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xuexiang.xpage.annotation.Page;
import com.hx.campus.R;
import io.rong.imkit.conversationlist.ConversationListAdapter;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.widget.adapter.ViewHolder;


@Page
public class ChatListFragment extends ConversationListFragment {

    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        background(view);
    }

    private void background(@NonNull View view) {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.xui_config_color_background, typedValue, true);
        int backgroundColor = typedValue.data;
        view.setBackgroundColor(backgroundColor);
    }

    @Override
    protected ConversationListAdapter onResolveAdapter() {
        ConversationListAdapter adapter = new ConversationListAdapter() {
            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                TypedValue typedValue = new TypedValue();
                getContext().getTheme().resolveAttribute(R.attr.xui_config_color_background, typedValue, true);
                int backgroundColor = typedValue.data;
                holder.itemView.setBackgroundColor(backgroundColor);
            }
        };
        adapter.setEmptyView(io.rong.imkit.R.layout.rc_conversationlist_empty_view);
        return adapter;
    }
}