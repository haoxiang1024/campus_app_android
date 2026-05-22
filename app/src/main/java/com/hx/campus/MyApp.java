


package com.hx.campus;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.multidex.MultiDex;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.hx.campus.activity.ConversationActivity;
import com.hx.campus.activity.LoginActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.TokenUtils;
import com.hx.campus.utils.sdkinit.ANRWatchDogInit;
import com.hx.campus.utils.sdkinit.UMengInit;
import com.hx.campus.utils.sdkinit.XBasicLibInit;
import com.hx.campus.utils.sdkinit.XUpdateInit;
import com.xuexiang.xui.BuildConfig;
import com.xuexiang.xutil.XUtil;

import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.notification.NotificationConfig;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.listener.OnReceiveMessageWrapperListener;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.ReceivedProfile;
import io.rong.imlib.model.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyApp extends Application {
    private static Context mContext;

    
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    
    @Override
    public void onCreate() {
        super.onCreate();

        initBaiduMap();

        initLibs();

        initIM();

        RetrofitClient.init(this);


    }

    private void initBaiduMap() {
        LocationClient.setAgreePrivacy(true);
        SDKInitializer.setAgreePrivacy(this, true);
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    
    public static void initMsgListener() {
        RongCoreClient.setConnectionStatusListener(status -> {


            if (status == IRongCoreListener.ConnectionStatusListener.ConnectionStatus.CONN_USER_BLOCKED ||
                    status == IRongCoreListener.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                IMCenter.getInstance().logout();

                new Handler(Looper.getMainLooper()).post(() -> {
                    Utils.showResponse("您的账号已被管理员禁用或在其他设备登录");
                    logout();
                });
            }
        });

        RongCoreClient.addOnReceiveMessageListener(new OnReceiveMessageWrapperListener() {
            @Override
            public void onReceivedMessage(Message message, ReceivedProfile profile) {
                if (message.getObjectName().equals("App:ForceOffline")) {

                    IMCenter.getInstance().logout();
                    logout();
                }
            }

        });
    }

    private static void logout() {

        IMCenter.getInstance().disconnect();

        TokenUtils.handleLogoutSuccess();

        XUtil.getActivityLifecycleHelper().exit();
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }

    
    private void initIM() {

        String appKey = Utils.getAppKey(this);
        Boolean enablePush = true;

        RongIM.init(this, appKey, enablePush);

        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationActivity, ConversationActivity.class);

        RongUserInfoManager.getInstance().setUserInfoProvider(userId -> {
            fetchUserInfoFromServer(userId);
            return null;
        }, true);

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

            channel.setShowBadge(true);

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
                    String fullAvatarUrl = "";
                    String photo = user.getPhoto();
                    if (!TextUtils.isEmpty(photo)) {
                        if (photo.startsWith("http")) {
                            fullAvatarUrl = photo;
                        } else {
                            fullAvatarUrl = Utils.rebuildUrl("upload/" + photo, getApplicationContext());
                        }
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
            }
        });
    }

    
    private void initLibs() {

        XBasicLibInit.init(this);

        XUpdateInit.init(this);

        UMengInit.init(this);

        ANRWatchDogInit.init();
    }


}
