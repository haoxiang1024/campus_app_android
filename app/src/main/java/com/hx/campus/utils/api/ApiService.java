package com.hx.campus.utils.api;

import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    //登录
    @POST("login")
    Call<Result<User>> login(@Query("phone") String phone, @Query("pwd") String pwd);
    //获取置顶信息
    @POST("showTopList")
    Call<Result<List<LostFound>>> showTopList(@Query("stick") int stick);
    // 获取公共列表
    @POST("showLostFoundList")
    Call<Result<List<LostFound>>> getLostFoundList(@Query("type") String type);

    // 获取指定用户的列表
    // type: "0" for Lost, "1" for Found
    @POST("getLostFoundByUserId")
    Call<Result<List<LostFound>>> getLostFoundListByUserId( @Query("user_id") int userId);

    // 更新物品状态
    @POST("updateState")
    Call<Result<String>> updateState(@Query("id") int id, @Query("state") String state, @Query("user_id") int userId);

    //获取分类下面的内容
    @POST("DetailByTitle")
    Call<Result<List<LostFound>>> DetailByTitle( @Query("title") String title,@Query("type") String type);


}
