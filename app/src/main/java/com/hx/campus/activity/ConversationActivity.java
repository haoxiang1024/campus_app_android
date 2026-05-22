
package com.hx.campus.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hx.campus.R;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;


public class ConversationActivity extends AppCompatActivity {

    
    private RongUserInfoManager.UserDataObserver userDataObserver;
    ConversationFragment fragment;
    private void background(@NonNull View view) {
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.xuexiang.xui.R.attr.xui_config_color_background, typedValue, true);
        int backgroundColor = typedValue.data;
        view.setBackgroundColor(backgroundColor);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        if (savedInstanceState == null) {
            fragment  = new ConversationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
            getSupportFragmentManager().registerFragmentLifecycleCallbacks(new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentViewCreated(@NonNull androidx.fragment.app.FragmentManager fm, @NonNull androidx.fragment.app.Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                    super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                    if (f instanceof ConversationFragment) {

                        background(v);
                    }
                }
            }, false);
        }

        TitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setLeftClickListener(v -> finish());

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        String targetId = bundle.getString("targetId");

        if (TextUtils.isEmpty(targetId)) {

            finish();
            return;
        }

        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
            titleBar.setTitle(userInfo.getName());
        } else {
            titleBar.setTitle("对话");
        }

        userDataObserver = new RongUserInfoManager.UserDataObserver() {
            @Override
            public void onUserUpdate(UserInfo info) {

                if (info != null && info.getUserId().equals(targetId)) {
                    runOnUiThread(() -> {
                        if (titleBar != null) {
                            titleBar.setTitle(info.getName());
                        }
                    });
                }
            }
            @Override
            public void onGroupUpdate(Group group) {
            }
            @Override
            public void onGroupUserInfoUpdate(GroupUserInfo groupUserInfo) {
            }
        };

        RongUserInfoManager.getInstance().addUserDataObserver(userDataObserver);

        setBtn();
    }

    
    private void setBtn() {
        View root = findViewById(android.R.id.content);
        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            View sendBtn = findViewById(R.id.input_panel_send_btn);
            if (sendBtn != null && sendBtn.getVisibility() == View.VISIBLE) {
                if (sendBtn.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) sendBtn.getLayoutParams();

                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    params.gravity = Gravity.BOTTOM;

                    params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                    params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    sendBtn.setLayoutParams(params);
                }

                sendBtn.setPadding(0, 0, 0, 0);
                sendBtn.setMinimumHeight(0);
                sendBtn.setMinimumWidth(0);
            }
        });
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDataObserver != null) {
            RongUserInfoManager.getInstance().removeUserDataObserver(userDataObserver);
        }
    }
}