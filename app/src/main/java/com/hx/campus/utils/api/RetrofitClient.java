package com.hx.campus.utils.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 确保这里的 IP 地址与你当前后端服务的 IP 一致
    private static final String BASE_URL = "http://192.168.229.122:8081/school/";
    private static volatile RetrofitClient mInstance;
    private Retrofit retrofit;

    private RetrofitClient() {
        Gson gson = new GsonBuilder()
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

                                // 优先处理你的格式
                                if (dateStr.contains("CST") && dateStr.contains("Dec")) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                                    return sdf.parse(dateStr);
                                }

                                // 尝试自动解析
                                return parseDateString(dateStr);
                            }
                        }
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .create();

        // 初始化 Retrofit 并关联自定义的 Gson
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static RetrofitClient getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitClient.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitClient();
                }
            }
        }
        return mInstance;
    }

    public ApiService getApi() {
        return retrofit.create(ApiService.class);
    }
    private Date parseDateString(String dateStr) {
        String[] formats = {
                "EEE MMM dd HH:mm:ss zzz yyyy",  // Tue Dec 30 11:25:22 CST 2025
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy/MM/dd HH:mm:ss",
                "MM/dd/yyyy HH:mm:ss"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}