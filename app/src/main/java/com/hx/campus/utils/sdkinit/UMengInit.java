
package com.hx.campus.utils.sdkinit;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.hx.campus.BuildConfig;
import com.hx.campus.MyApp;
import com.hx.campus.utils.common.SettingUtils;
import com.meituan.android.walle.WalleChannelReader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.xuexiang.xui.XUI;


public final class UMengInit {

    private static final String DEFAULT_CHANNEL_ID = "github";

    private UMengInit() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    
    public static void init() {
        init(XUI.getContext());
    }

    
    public static void init(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext instanceof Application) {
            init((Application) appContext);
        }
    }

    
    public static void init(Application application) {
        // 运营统计数据调试运行时不初始化
        if (MyApp.isDebug()) {
            return;
        }
        UMConfigure.setLogEnabled(false);
        UMConfigure.preInit(application, BuildConfig.APP_ID_UMENG, getChannel(application));
        // 用户同意了隐私协议
        if (SettingUtils.isAgreePrivacy()) {
            realInit(application);
        }
    }

    
    private static void realInit(Application application) {
        // 运营统计数据调试运行时不初始化
        if (MyApp.isDebug()) {
            return;
        }
        //第二个参数是appkey，最后一个参数是pushSecret
        UMConfigure.init(application, BuildConfig.APP_ID_UMENG, getChannel(application), UMConfigure.DEVICE_TYPE_PHONE, "");
        //统计SDK是否支持采集在子进程中打点的自定义事件，默认不支持
        //支持多进程打点
        UMConfigure.setProcessEvent(true);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    
    public static String getChannel(final Context context) {
        return WalleChannelReader.getChannel(context, DEFAULT_CHANNEL_ID);
    }
}
