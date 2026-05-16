
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
        // 挂载聊天Fragment
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
        // 配置标题栏
        TitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setLeftClickListener(v -> finish());
        // 获取目标用户ID
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        String targetId = bundle.getString("targetId");
        // 验证目标ID的有效性
        if (TextUtils.isEmpty(targetId)) {
            // 目标ID无效，关闭页面避免空指针异常
            finish();
            return;
        }
        // 设置初始标题
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
            titleBar.setTitle(userInfo.getName());
        } else {
            titleBar.setTitle("对话");
        }
        // 创建用户信息观察者
        userDataObserver = new RongUserInfoManager.UserDataObserver() {
            @Override
            public void onUserUpdate(UserInfo info) {
                // 当目标用户信息更新时，刷新标题栏
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
        // 注册用户信息观察者
        RongUserInfoManager.getInstance().addUserDataObserver(userDataObserver);
        // 自定义发送按钮样式
        setBtn();
    }

    
    private void setBtn() {
        View root = findViewById(android.R.id.content);
        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            View sendBtn = findViewById(R.id.input_panel_send_btn);
            if (sendBtn != null && sendBtn.getVisibility() == View.VISIBLE) {
                if (sendBtn.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) sendBtn.getLayoutParams();
                    // 设置按钮尺寸
                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    params.gravity = Gravity.BOTTOM;
                    // 设置边距
                    params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                    params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    sendBtn.setLayoutParams(params);
                }
                // 清除内边距和最小尺寸限制
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