
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

                .isWifiOnly(false)

                .isGet(true)

                .isAutoMode(false)

                .param("versionCode", UpdateUtils.getVersionCode(application))
                .param("appKey", application.getPackageName())

                .setIUpdateHttpService(new XHttpUpdateHttpServiceImpl())
                .setIUpdateDownLoader(new CustomUpdateDownloader())

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
