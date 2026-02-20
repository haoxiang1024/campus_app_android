

package com.hx.campus;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.hx.campus.activity.chat.ConversationActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.sdkinit.ANRWatchDogInit;
import com.hx.campus.utils.sdkinit.UMengInit;
import com.hx.campus.utils.sdkinit.XBasicLibInit;
import com.hx.campus.utils.sdkinit.XUpdateInit;

import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.UserDataProvider;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.InitOption;
import io.rong.imlib.model.UserInfo;

import com.xuexiang.xui.BuildConfig;

public class MyApp extends Application {

    /**
     * @return 当前app是否是调试开发模式
     */
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //解决4.x运行崩溃的问题
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLibs();
        initIM();
    }

    private void initIM() {
        //初始化Imsdk
        String appKey = Utils.getAppKey(this);
        Boolean enablePush = true;
        RongIM.init(this, appKey, enablePush);
        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationActivity, ConversationActivity.class);
    }

    /**
     * 初始化基础库
     */
    private void initLibs() {
        // X系列基础库初始化
        XBasicLibInit.init(this);
        // 版本更新初始化
        XUpdateInit.init(this);
        // 运营统计数据
        UMengInit.init(this);
        // ANR监控
        ANRWatchDogInit.init();
    }


}
