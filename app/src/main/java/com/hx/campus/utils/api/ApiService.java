package com.hx.campus.utils.api;

import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.adapter.entity.LoginResponseDTO;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.LostFoundType;
import com.hx.campus.adapter.entity.SearchInfo;
import com.hx.campus.adapter.entity.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    //登录
    @POST("login")
    Call<Result<LoginResponseDTO>> login(@Query("phone") String phone, @Query("pwd") String pwd);
    //获取置顶信息
    @POST("showTopList")
    Call<Result<List<LostFound>>> showTopList(@Query("stick") int stick);
    // 获取公共列表
    @POST("showLostFoundList")
    Call<Result<List<LostFound>>> getLostFoundList(@Query("type") String type);

    // 获取指定用户的列表
    @POST("getLostFoundByUserId")
    Call<Result<List<LostFound>>> getLostFoundListByUserId( @Query("user_id") int userId);

    // 更新物品状态
    @POST("updateState")
    Call<Result<String>> updateState(@Query("id") int id, @Query("state") String state, @Query("user_id") int userId);

    //获取分类下面的内容
    @POST("DetailByTitle")
    Call<Result<List<LostFound>>> DetailByTitle( @Query("title") String title,@Query("type") String type);

    //添加物品信息
    @Multipart
    @POST("addLostFound")
    Call<Result<List<LostFound>>> addLostFound(
            @Part MultipartBody.Part file,
            @Part("lostJson") RequestBody lostJson,
            @Part("foundJson") RequestBody foundJson,
            @Part("op") RequestBody op
    );

    //获取分类id
    @POST("getIdByName")
    Call<Result<String>>getTypeid(@Query("name") String name);
    // 发送验证码
    @POST("send_code")
    Call<Result<Object>> sendCode(@Query("email") String email);

    // 校验验证码
    @POST("verify_code")
    Call<Result<Object>> verifyCode(@Query("email") String email, @Query("code") String code);

    // 重置密码
    @POST("resetPwd")
    Call<Result<Object>> resetPwd(
            @Query("phone") String phone,
            @Query("newPwd") String password,
            @Query("email") String email,
            @Query("email_code") String code
    );
    //搜索
    @POST("searchInfo")
    Call<Result<List<SearchInfo>>> searchInfo(@Query("value") String value);
    //用户修改信息
    @POST("updateAc")
    Call<Result<User>> updateAccount(
            @Query("nickname") String nickname,
            @Query("sex") String sex,
            @Query("id") int id
    );
    //用户修改头像
    @Multipart
    @POST("updatePic")
    Call<Result<User>> updatePhoto(
            @Part MultipartBody.Part file,
            @Query("id") int userId
    );
    //注册接口
    @POST("register")
    Call<Result<Object>> register(
            @Query("phone") String phone,
            @Query("email") String email,
            @Query("password") String password
    );
    //获取IM用户token
    @POST("getIMUserToken")
    Call<Result<String>> getIMUserToken(
            @Query("uid") int uid,
            @Query("nickname") String nickname
    );

    // 根据用户ID获取用户信息
    @POST("getUserById")
    Call<User> getUserInfo(@Query("id") int id);

    // 获取评论列表
    @GET("getCommentsByLostFoundId")
    Call<Result<List<Comment>>> getComments(@Query("lostfound_id") int lostfoundId);

    // 获取某用户收到的所有评论/回复
    @GET("getReceivedComments")
    Call<Result<List<Comment>>> getReceivedComments(@Query("user_id") int user_id);

    // 发布评论
    @FormUrlEncoded
    @POST("addComment")
    Call<Result<String>> addComment(
            @Field("lostfound_id") int lostfoundId,
            @Field("user_id") int userId,
            @Field("content") String content,
            @Field("parent_id") int parentId,
            @Field("reply_user_id") int replyUserId
    );

    //获取所有分类
    @GET("getAllType")
    Call<Result<List<LostFoundType>>> getAllType();

    //获取用户发的评论
    @GET("getComments")
    Call<Result<List<Comment>>> getCommentsByUserId(@Query("user_id") int user_id);

    //删除用户评论
    @GET("/school/admin/deleteCommentById")
    Call<Result<String>>deleteComment(@Query("commentId") int commentId);

    //根据Id获取失物招领信息
    @GET("/school/admin/getLostFoundById")
    Call<Result<LostFound>>getLostFoundById(@Query("lostFoundId") int lostFoundId);

}
