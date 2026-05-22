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


public class RetrofitClient {
    private static volatile RetrofitClient mInstance;
    private Retrofit retrofit;
    private static Context mContext;
    private static final Gson gson = new Gson();
    private final AtomicBoolean isExiting = new AtomicBoolean(false);

    
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

    
    private RetrofitClient(String baseUrl) {

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


        Interceptor authInterceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            if (originalResponse.body() != null) {
                ResponseBody responseBody = originalResponse.body();
                String jsonStr = responseBody.string();
                MediaType mediaType = responseBody.contentType();
                try {

                ApiResponse apiResponse = gson.fromJson(jsonStr, ApiResponse.class);


                if ((apiResponse != null && apiResponse.getStatus() == 401)
                        || originalResponse.code() == 401) {
                        String reason = apiResponse != null ? apiResponse.getMsg() : "您的账号已被禁用，请联系管理员";
                        handleForceLogout(reason);
                    }
                } catch (Exception e) {

                    Log.e("解析业务 code 失败: ", e.getMessage());
                }

                ResponseBody newResponseBody = ResponseBody.create(mediaType, jsonStr);
                return originalResponse.newBuilder().body(newResponseBody).build();
            }

            if (originalResponse.code() == 401) {
                handleForceLogout("您的账号已被禁用，请联系管理员");
            }
            return originalResponse;
        };


        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(authInterceptor)
                .build();


        Gson retrofitGson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                    try {
                        if (json.isJsonPrimitive()) {
                            JsonPrimitive primitive = json.getAsJsonPrimitive();

                            if (primitive.isNumber()) {
                                long timestamp = primitive.getAsLong();
                                return new Date(timestamp > 1000000000000L ? timestamp : timestamp * 1000);
                            }

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


        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(retrofitGson))
                .build();
    }

    
    public void handleForceLogout(String reason) {

        if (!isExiting.compareAndSet(false, true)) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            Log.e("执行强制退出逻辑: ", reason);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                XToastUtils.info(reason);
            }, 2000);
            IMCenter.getInstance().disconnect();
            IMCenter.getInstance().logout();
            TokenUtils.handleLogoutSuccess();
            XUtil.getActivityLifecycleHelper().exit();

            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);

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
                Log.e("RetrofitClient", "CST 格式解析失败：" + dateStr);
            }
        }


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

        Log.e("RetrofitClient", "所有时间格式匹配失败: " + dateStr);
        return null;
    }
}