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
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                return sdf.parse(dateStr);
            } catch (ParseException ignored) { }
        }

        android.util.Log.e("RetrofitClient", "所有时间格式匹配失败: " + dateStr);
        return null;
    }
}