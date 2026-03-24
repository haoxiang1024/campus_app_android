package com.hx.campus.utils.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.hx.campus.activity.LoginActivity;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.common.TokenUtils;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xutil.XUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import io.rong.imkit.IMCenter;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 客户端工具类
 * 负责创建和管理 Retrofit 实例，处理 HTTP 请求的拦截器、Token 认证、响应解析等功能
 */
public class RetrofitClient {
    private static volatile RetrofitClient mInstance;
    private Retrofit retrofit;
    private static Context mContext;
    private static final Gson gson = new Gson();
    private final AtomicBoolean isExiting = new AtomicBoolean(false);

    /**
     * API 响应数据结构
     * 用于解析后端返回的统一响应格式
     */
    private static class ApiResponse {
        private int status;
        private Object data;
        private String msg;

        public int getStatus() {
            return status;
        }

        public String getMsg() {
            return msg;
        }
    }

    /**
     * 私有构造函数，初始化 Retrofit 实例
     * 配置 Token 拦截器和认证拦截器，处理日期格式化
     *
     * @param baseUrl API 基础 URL
     */
    private RetrofitClient(String baseUrl) {
        // Token 拦截器：为请求添加 Authorization 头
        Interceptor tokenInterceptor = chain -> {
            Request originalRequest = chain.request();
            String token = TokenUtils.getToken();
            Request newRequest = originalRequest;
            if (token != null && !token.isEmpty()) {
                newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
            }
            return chain.proceed(newRequest);
        };

        // 认证拦截器：处理 401 未授权响应，触发强制退出逻辑
        Interceptor authInterceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            if (originalResponse.body() != null) {
                ResponseBody responseBody = originalResponse.body();
                String jsonStr = responseBody.string();
                MediaType mediaType = responseBody.contentType();
                try {
                // 尝试解析响应体为统一的 API 响应格式
                ApiResponse apiResponse = gson.fromJson(jsonStr, ApiResponse.class);
                    // 捕获业务401（status=401）
                // 检查业务状态码是否为 401 或 HTTP 状态码为 401
                if ((apiResponse != null && apiResponse.getStatus() == 401)
                        || originalResponse.code() == 401) {
                        String reason = apiResponse != null ? apiResponse.getMsg() : "您的账号已被禁用，请联系管理员";
                        handleForceLogout(reason);
                    }
                } catch (Exception e) {
                    // JSON 解析失败时记录错误日志
                    Log.e("解析业务 code 失败: ", e.getMessage());
                }
                // 重建响应体
                ResponseBody newResponseBody = ResponseBody.create(mediaType, jsonStr);
                return originalResponse.newBuilder().body(newResponseBody).build();
            }
            // 处理HTTP 401
            if (originalResponse.code() == 401) {
                handleForceLogout("您的账号已被禁用，请联系管理员");
            }
            return originalResponse;
        };

        // 创建 OkHttpClient 并添加拦截器
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(authInterceptor)
                .build();

        // 创建自定义 Gson 实例，注册 Date 类型反序列化器
        Gson retrofitGson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                    try {
                        if (json.isJsonPrimitive()) {
                            JsonPrimitive primitive = json.getAsJsonPrimitive();
                            // 处理数字类型的时间戳（毫秒或秒）
                            if (primitive.isNumber()) {
                                long timestamp = primitive.getAsLong();
                                return new Date(timestamp > 1000000000000L ? timestamp : timestamp * 1000);
                            }
                            // 处理字符串类型的时间
                            if (primitive.isString()) {
                                return parseDateString(primitive.getAsString());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .create();

        // 构建 Retrofit 实例
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(retrofitGson))
                .build();
    }

    /**
     * 处理强制退出逻辑
     * 断开融云连接、清除 Token、退出所有页面并跳转到登录页
     *
     * @param reason 退出原因提示
     */
    public void handleForceLogout(String reason) {
        // 使用原子布尔保证只执行一次退出逻辑
        if (!isExiting.compareAndSet(false, true)) {
            return;
        }
        // 在主线程执行退出操作
        new Handler(Looper.getMainLooper()).post(() -> {
            Log.e("执行强制退出逻辑: ", reason);
            // 延迟显示提示信息
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                XToastUtils.info(reason);
            }, 2000);
            IMCenter.getInstance().disconnect(); // 断开融云
            IMCenter.getInstance().logout();// 登出
            TokenUtils.handleLogoutSuccess();   // 清除Token
            XUtil.getActivityLifecycleHelper().exit(); // 退出所有页面
            // 跳转至登录页
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
            // 重置退出标志
            new Handler(Looper.getMainLooper()).postDelayed(() -> isExiting.set(false), 2000);
        });
    }

    /**
     * 获取 RetrofitClient 单例实例
     *
     * @return RetrofitClient 实例
     * @throws RuntimeException 如果未先调用 init() 方法初始化
     */
    public static RetrofitClient getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("请先调用 init() 初始化 RetrofitClient");
        }
        return mInstance;
    }

    /**
     * 初始化 RetrofitClient 单例
     * 使用双重检查锁定确保线程安全
     *
     * @param context 上下文对象
     */
    public static void init(Context context) {
        if (mInstance == null) {
            // 使用同步锁确保线程安全
            synchronized (RetrofitClient.class) {
                if (mInstance == null) {
                    mContext = context.getApplicationContext();
                    String baseUrl = Utils.getUrlFromAssets(context);
                    mInstance = new RetrofitClient(baseUrl);
                }
            }
        }
    }

    /**
     * 获取 API 服务接口实例
     *
     * @return ApiService 接口实例
     */
    public ApiService getApi() {
        return retrofit.create(ApiService.class);
    }

    /**
     * 解析日期字符串
     * 支持多种日期格式，包括 CST 格式和常见 ISO 8601 格式
     *
     * @param dateStr 日期字符串
     * @return 解析后的 Date 对象，解析失败返回 null
     */
    private Date parseDateString(String dateStr) {
        // 解析 CST 时区格式
        if (dateStr.contains("CST")) {
            try {
                SimpleDateFormat cstSdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US);
                cstSdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8"));
                return cstSdf.parse(dateStr);
            } catch (ParseException e) {
                Log.e("RetrofitClient", "CST 格式解析失败：" + dateStr);
            }
        }

        // 定义支持的日期格式数组
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm"
        };

        // 各种日期格式
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                return sdf.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }

        Log.e("RetrofitClient", "所有时间格式匹配失败: " + dateStr);
        return null;
    }
}