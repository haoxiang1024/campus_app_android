package com.hx.campus.utils.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

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

import io.rong.imkit.IMCenter;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
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

    // 定义响应数据的基础结构
    private static class ApiResponse {
        private int status; // 业务code字段
        private String message; // 可选，业务提示信息

        public int getCode() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    private RetrofitClient(String baseUrl) {
        Interceptor authInterceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            android.util.Log.e("RetrofitClient", "Response Code: " + originalResponse.code());
            if (originalResponse.body() != null && originalResponse.isSuccessful()) {
                // 复制响应体，避免原响应体被关闭后无法读取
                ResponseBody responseBody = originalResponse.body();
                String jsonStr = responseBody.string();
                android.util.Log.e("RetrofitClient", "Response JSON: " + jsonStr);

                try {
                    // 解析JSON获取业务code
                    ApiResponse apiResponse = gson.fromJson(jsonStr, ApiResponse.class);
                    int businessCode = apiResponse.getCode();

                    if (businessCode == 401) {
                        handleForceLogout();
                    }
                } catch (Exception e) {
                    // 解析失败时的容错处理
                    android.util.Log.e("RetrofitClient", "解析业务code失败: " + e.getMessage());
                }

                // 重新构建响应体，保证后续流程能正常读取
                MediaType mediaType = responseBody.contentType();
                ResponseBody newResponseBody = ResponseBody.create(mediaType, jsonStr);
                return originalResponse.newBuilder().body(newResponseBody).build();
            }

            // 兼容原有逻辑：如果非200系列状态码，仍保留HTTP状态码判断
            if (originalResponse.code() == 401) {
                handleForceLogout();
            }

            return originalResponse;
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
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

    private void handleForceLogout() {
        // 切换到主线程处理 UI
        new Handler(Looper.getMainLooper()).post(() -> {
            Utils.showResponse("用户已被禁用，请重新登录");
            // 清除本地存储的用户信息
            IMCenter.getInstance().disconnect();
            XUtil.getActivityLifecycleHelper().exit();
            TokenUtils.handleLogoutSuccess();
            // 跳转到登录页并清空 Activity 栈
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
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