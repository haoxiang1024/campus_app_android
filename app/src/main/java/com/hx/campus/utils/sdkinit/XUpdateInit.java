
package com.hx.campus.utils.sdkinit;

import android.app.Application;
import android.content.Context;

import com.hx.campus.MyApp;
import com.hx.campus.utils.update.CustomUpdateDownloader;
import com.hx.campus.utils.update.CustomUpdateFailureListener;
import com.hx.campus.utils.update.XHttpUpdateHttpServiceImpl;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.xuexiang.xutil.common.StringUtils;


public final class XUpdateInit {

    
    private static final String KEY_UPDATE_URL = "";

    private XUpdateInit() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void init(Application application) {
        XUpdate.get()
                .debug(MyApp.isDebug())
                //默认设置只在wifi下检查版本更新
                .isWifiOnly(false)
                //默认设置使用get请求检查版本
                .isGet(true)
                //默认设置非自动模式，可根据具体使用配置
                .isAutoMode(false)
                //设置默认公共请求参数
                .param("versionCode", UpdateUtils.getVersionCode(application))
                .param("appKey", application.getPackageName())
                //这个必须设置！实现网络请求功能。
                .setIUpdateHttpService(new XHttpUpdateHttpServiceImpl())
                .setIUpdateDownLoader(new CustomUpdateDownloader())
                //这个必须初始化
                .init(application);
    }

    
    public static void checkUpdate(Context context, boolean needErrorTip) {
        checkUpdate(context, KEY_UPDATE_URL, needErrorTip);
    }

    
    private static void checkUpdate(Context context, String url, boolean needErrorTip) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        XUpdate.newBuild(context).updateUrl(url).update();
        XUpdate.get().setOnUpdateFailureListener(new CustomUpdateFailureListener(needErrorTip));
    }
}
