
package com.hx.campus.utils.sdkinit;

import android.app.Application;

import com.hx.campus.MyApp;
import com.hx.campus.core.BaseActivity;
import com.hx.campus.utils.common.TokenUtils;
import com.xuexiang.xhttp2.XHttpSDK;
import com.xuexiang.xpage.PageConfig;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xui.XUI;
import com.xuexiang.xutil.XUtil;


public final class XBasicLibInit {

    private XBasicLibInit() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    
    public static void init(Application application) {

        initXUtil(application);


        initXHttp2(application);


        initXPage(application);





        initXUI(application);


        initRouter(application);
    }

    
    private static void initXUtil(Application application) {
        XUtil.init(application);
        XUtil.debug(MyApp.isDebug());
        TokenUtils.init(application);
    }

    
    private static void initXHttp2(Application application) {

        XHttpSDK.init(application);

        if (MyApp.isDebug()) {
            XHttpSDK.debug();
        }


        XHttpSDK.setBaseUrl("https://gitee.com/");




    }

    
    private static void initXPage(Application application) {
        PageConfig.getInstance()
                .debug(MyApp.isDebug())
                .setContainActivityClazz(BaseActivity.class)
                .init(application);
    }

    







    
    private static void initXUI(Application application) {
        XUI.init(application);
        XUI.debug(MyApp.isDebug());
    }

    
    private static void initRouter(Application application) {

        if (MyApp.isDebug()) {
            XRouter.openLog();
            XRouter.openDebug();
        }
        XRouter.init(application);
    }

}
