

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

/**
 * 应用程序主类，继承自Application
 * 处理应用启动时的各项初始化工作
 */
public class MyApp extends Application {
    private static Context mContext; // 保存 context 用于跳转

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
        // 百度地图/定位 隐私合规检查
        initBaiduMap();
        // 初始化基础功能库
        initLibs();
        // 初始化即时通讯服务
        initIM();
        // 初始化网络请求客户端
        RetrofitClient.init(this);
        //消息监听
        //initMsgListener();
    }

    private void initBaiduMap() {
        LocationClient.setAgreePrivacy(true);
        SDKInitializer.setAgreePrivacy(this, true);
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    /**
     * 初始化全局的消息监听
     */
    public static void initMsgListener() {
        RongCoreClient.setConnectionStatusListener(status -> {
            // 如果状态是：用户被封禁 (CONN_USER_BLOCKED)
            // 或者：在其他设备登录被踢下线 (KICKED_OFFLINE_BY_OTHER_CLIENT)
            if (status == IRongCoreListener.ConnectionStatusListener.ConnectionStatus.CONN_USER_BLOCKED ||
                    status == IRongCoreListener.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                IMCenter.getInstance().logout();
                // 必须切换到主线程执行 UI 和 跳转逻辑
                new Handler(Looper.getMainLooper()).post(() -> {
                    Utils.showResponse("您的账号已被管理员禁用或在其他设备登录");
                    logout();
                });
            }
        });
// 注册消息监听
        RongCoreClient.addOnReceiveMessageListener(new OnReceiveMessageWrapperListener() {
            @Override
            public void onReceivedMessage(Message message, ReceivedProfile profile) {
                if (message.getObjectName().equals("App:ForceOffline")) {
                    // 执行登出操作
                    IMCenter.getInstance().logout();
                    logout();
                }
            }

        });
    }

    private static void logout() {
        //  断开融云连接
        IMCenter.getInstance().disconnect();
        //  清除本地 Token 缓存
        TokenUtils.handleLogoutSuccess();
        //  退出所有 Activity 并跳转 Login
        XUtil.getActivityLifecycleHelper().exit();
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
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
                    String fullAvatarUrl = "";
                    String photo = user.getPhoto();
                    if (!TextUtils.isEmpty(photo)) {
                        if (photo.startsWith("http")) {
                            fullAvatarUrl = photo;
                        } else {
                            fullAvatarUrl = Utils.rebuildUrl("upload/" + photo, getApplicationContext());
                        }
                    }
                    // 构建融云用户信息对象
                    UserInfo userInfo = new UserInfo(
                            userId,
                            user.getNickname(),
                            Uri.parse(fullAvatarUrl) // 如果 fullAvatarUrl 是空字符串，融云会显示你设置的默认头像
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
