

/**
 * 校园应用主程序入口类
 * 负责整个应用的初始化工作，包括第三方SDK配置、即时通讯服务初始化等
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2024
 */
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

/**
 * 应用程序主类，继承自Application
 * 处理应用启动时的各项初始化工作
 */
public class MyApp extends Application {

    /**
     * 检查当前是否为调试模式
     * @return true表示调试模式，false表示发布模式
     */
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }
    /**
     * 应用程序启动时最先调用的方法
     * 主要用于解决Android 4.x版本的多dex支持问题
     * @param base 应用上下文
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 为Android 4.x设备安装多dex支持，避免ClassNotFound异常
        MultiDex.install(this);
    }

    /**
     * 应用程序创建时的核心初始化方法
     * 按顺序初始化各种基础库和服务
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化基础功能库
        initLibs();
        // 初始化即时通讯服务
        initIM();
        // 初始化网络请求客户端
        RetrofitClient.init(this);
    }

    /**
     * 初始化融云即时通讯SDK
     * 包括SDK初始化、路由配置、用户信息提供者设置等
     */
    private void initIM() {
        // 获取融云AppKey配置
        String appKey = Utils.getAppKey(this);
        Boolean enablePush = true;
        // 初始化融云SDK核心功能
        RongIM.init(this, appKey, enablePush);
        // 注册聊天页面路由
        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationActivity, ConversationActivity.class);
        // 设置用户信息提供者，用于动态获取用户资料
        RongUserInfoManager.getInstance().setUserInfoProvider(userId -> {
            fetchUserInfoFromServer(userId);
            return null;
        }, true);
        // 配置消息通知相关设置
        notification();
    }
    /**
     * 配置消息通知通道
     * 主要针对Android 8.0及以上版本的通知渠道设置
     */
    private void notification() {
        String NEW_CHANNEL_ID = "rc_notification_id_v2";
        NotificationChannel channel = null;
        
        // Android 8.0以上需要创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 创建高优先级通知渠道
            channel = new NotificationChannel(
                    NEW_CHANNEL_ID,
                    "重要消息通知",  // 渠道名称
                    NotificationManager.IMPORTANCE_HIGH  // 重要程度
            );
            // 启用指示灯提醒
            channel.enableLights(true);
            // 显示桌面角标
            channel.setShowBadge(true);
            // 允许在锁屏状态下显示通知内容
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // 注册通知渠道
            manager.createNotificationChannel(channel);
        }
        
        // 配置融云通知相关参数
        NotificationConfig config = RongConfigCenter.notificationConfig();
        // 前台其他页面时也显示通知
        config.setForegroundOtherPageAction(NotificationConfig.ForegroundOtherPageAction.Notification);
        config.setNotificationChannel(channel);
    }


    /**
     * 从服务器获取用户信息
     * 用于即时通讯中动态更新用户资料
     * @param userId 用户唯一标识符
     */
    private void fetchUserInfoFromServer(String userId) {
        RetrofitClient.getInstance().getApi().getUserInfo(Integer.parseInt(userId)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String fullAvatarUrl = user.getPhoto();
                    
                    // 处理头像URL，相对路径需要拼接完整地址
                    if (!user.getPhoto().startsWith("http")) {
                        fullAvatarUrl = Utils.rebuildUrl("/upload/" + user.getPhoto(), getApplicationContext());
                    }
                    
                    // 构建融云用户信息对象
                    UserInfo userInfo = new UserInfo(
                            userId,
                            user.getNickname(),
                            Uri.parse(fullAvatarUrl)
                    );
                    // 刷新本地用户信息缓存
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
     * 初始化应用所需的各种基础库
     * 按照依赖关系有序初始化各个功能模块
     */
    private void initLibs() {
        // 初始化X系列UI基础库（XUI框架）
        XBasicLibInit.init(this);
        // 初始化应用版本更新检测功能
        XUpdateInit.init(this);
        // 初始化友盟统计分析服务
        UMengInit.init(this);
        // 初始化ANR（应用无响应）监控服务
        ANRWatchDogInit.init();
    }


}
