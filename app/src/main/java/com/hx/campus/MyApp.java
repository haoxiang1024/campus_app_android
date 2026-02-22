

package com.hx.campus;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.hx.campus.activity.chat.ConversationActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.sdkinit.ANRWatchDogInit;
import com.hx.campus.utils.sdkinit.UMengInit;
import com.hx.campus.utils.sdkinit.XBasicLibInit;
import com.hx.campus.utils.sdkinit.XUpdateInit;

import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.notification.NotificationConfig;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.UserDataProvider;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.InitOption;
import io.rong.imlib.model.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        RongUserInfoManager.getInstance().setUserInfoProvider(userId -> {
            fetchUserInfoFromServer(userId);
            return null;
        }, true);
        // 通知配置
        notification();

    }
    private void notification() {
        String NEW_CHANNEL_ID = "rc_notification_id_v2";
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            channel = new NotificationChannel(
                    NEW_CHANNEL_ID,
                    "重要消息通知",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.setShowBadge(true); // 桌面角标
            // 允许在锁屏显示
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
        NotificationConfig config = RongConfigCenter.notificationConfig();
        config.setForegroundOtherPageAction(NotificationConfig.ForegroundOtherPageAction.Notification);
        config.setNotificationChannel(channel);
    }


    private void fetchUserInfoFromServer(String userId) {
        RetrofitClient.getInstance().getApi().getUserInfo(Integer.parseInt(userId)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String fullAvatarUrl=user.getPhoto();
                    // 拼接头像全路径
                    if (!user.getPhoto().startsWith("http")){
                        fullAvatarUrl= Utils.rebuildUrl("/upload/"+user.getPhoto(),getApplicationContext());
                    }
                    UserInfo userInfo = new UserInfo(
                            userId,
                            user.getNickname(),
                            Uri.parse(fullAvatarUrl)
                    );
                    RongUserInfoManager.getInstance().refreshUserInfoCache(userInfo);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Utils.showResponse("用户请求失败: " + t.getMessage());

            }
        });
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
