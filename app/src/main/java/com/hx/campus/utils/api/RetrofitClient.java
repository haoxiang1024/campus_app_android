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
import com.xuexiang.xutil.XUtil;

import java.io.IOException;
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

public class RetrofitClient {
    private static volatile RetrofitClient mInstance;
    private Retrofit retrofit;
    private static Context mContext; // 保存 context 用于跳转
    // Gson 实例，用于解析响应体
    private static final Gson gson = new Gson();
    // 使用 AtomicBoolean 保证线程安全，防止并发请求导致重复跳转
    private final AtomicBoolean isExiting = new AtomicBoolean(false);
    // 定义响应数据的基础结构
    private static class ApiResponse {
        private int status;    // 服务端的 status 字段
        private Object data;   // 服务端的 data 字段
        private String msg;    // 服务端的 msg 字段


        public int getStatus() {
            return status;
        }

        public String getMsg() {
            return msg;
        }
    }

    private RetrofitClient(String baseUrl) {
        // 添加 Token 请求拦截器：往请求头里加 Token
        Interceptor tokenInterceptor = chain -> {
            // 获取原始请求
            Request originalRequest = chain.request();
            // 从 TokenUtils 获取本地缓存的 Token
            String token = TokenUtils.getToken(); // 假设 TokenUtils 有这个获取方法
            Request newRequest;
            // 如果 Token 不为空，添加到请求头
            if (token != null && !token.isEmpty()) {
                newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        // .header("token", token)
                        .build();
            } else {
                // 无 Token 时使用原始请求
                newRequest = originalRequest;
            }
            // 继续执行请求
            return chain.proceed(newRequest);
        };

        // 处理 401/业务 401 状态
        Interceptor authInterceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            if (originalResponse.body() != null && originalResponse.isSuccessful()) {
                ResponseBody responseBody = originalResponse.body();
                String jsonStr = responseBody.string();
                try {
                    ApiResponse apiResponse = gson.fromJson(jsonStr, ApiResponse.class);
                    if (apiResponse != null && (apiResponse.getStatus() == 401)) {
                        handleForceLogout("您的账号已被禁用，请联系管理员");
                    }
                } catch (Exception e) {
                    Log.e("解析业务code失败: ", e.getMessage());
                }

                // 重新构建 ResponseBody
                MediaType mediaType = responseBody.contentType();
                ResponseBody newResponseBody = ResponseBody.create(mediaType, jsonStr);
                return originalResponse.newBuilder().body(newResponseBody).build();
            }

            return originalResponse;
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor) // 先加 Token 拦截器（请求前执行）
                .addInterceptor(authInterceptor)  // 再加响应拦截器（响应后执行）
                .build();

        Gson retrofitGson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                    try {
                        if (json.isJsonPrimitive()) {
                            JsonPrimitive primitive = json.getAsJsonPrimitive();
                            // 处理数字时间戳
                            if (primitive.isNumber()) {
                                long timestamp = primitive.getAsLong();
                                if (timestamp > 1000000000000L) {
                                    return new Date(timestamp); // 毫秒
                                } else {
                                    return new Date(timestamp * 1000); // 秒
                                }
                            }

                            // 处理字符串时间
                            if (primitive.isString()) {
                                String dateStr = primitive.getAsString();
                                return parseDateString(dateStr);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client) // 绑定自定义的 OkHttp
                .addConverterFactory(GsonConverterFactory.create(retrofitGson))
                .build();
    }

    /**
     * 统一处理强制退出逻辑（对外暴露，方便融云调用）
     * @param reason 退出的提示原因
     */
    public void handleForceLogout(String reason) {
        // 如果正在退出中，直接 return，防止多个 401 同时触发
        if (!isExiting.compareAndSet(false, true)) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            Log.e( "执行强制退出逻辑: ",reason);
            Utils.showResponse(reason);
            //  断开融云连接
            IMCenter.getInstance().disconnect();
            //  清除本地 Token 缓存
            TokenUtils.handleLogoutSuccess();
            //  退出所有 Activity 并跳转 Login
            XUtil.getActivityLifecycleHelper().exit();
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);

            // 延迟重置状态，确保跳转完成后才允许下一次触发
            new Handler(Looper.getMainLooper()).postDelayed(() -> isExiting.set(false), 2000);
        });
    }

    public static RetrofitClient getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("请先调用 init() 初始化 RetrofitClient");
        }
        return mInstance;
    }

    public static void init(Context context) {
        if (mInstance == null) {
            synchronized (RetrofitClient.class) {
                if (mInstance == null) {
                    mContext = context.getApplicationContext();
                    String baseUrl = Utils.getUrlFromAssets(context);
                    mInstance = new RetrofitClient(baseUrl);
                }
            }
        }
    }

    public ApiService getApi() {
        return retrofit.create(ApiService.class);
    }

    private Date parseDateString(String dateStr) {
        if (dateStr.contains("CST")) {
            try {
                SimpleDateFormat cstSdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US);
                cstSdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8"));
                return cstSdf.parse(dateStr);
            } catch (ParseException e) {
                android.util.Log.e("RetrofitClient", "CST格式解析失败: " + dateStr);
            }
        }

        // 备用格式
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                return sdf.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }

        android.util.Log.e("RetrofitClient", "所有时间格式匹配失败: " + dateStr);
        return null;
    }
}