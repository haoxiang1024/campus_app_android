package com.hx.campus.utils.api;

import com.hx.campus.adapter.entity.BaiduPoiResponse;
import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.adapter.entity.ExchangeOrder;
import com.hx.campus.adapter.entity.LoginResponseDTO;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.LostFoundType;
import com.hx.campus.adapter.entity.MessageVO;
import com.hx.campus.adapter.entity.PointHistory;
import com.hx.campus.adapter.entity.SearchInfo;
import com.hx.campus.adapter.entity.ShopItem;
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

    @POST("login")
    Call<Result<LoginResponseDTO>> login(@Query("phone") String phone, @Query("pwd") String pwd);

    @POST("showTopList")
    Call<Result<List<LostFound>>> showTopList(@Query("stick") int stick);


    @POST("getLostFoundByUserId")
    Call<Result<List<LostFound>>> getLostFoundListByUserId( @Query("user_id") int userId);


    @POST("updateState")
    Call<Result<String>> updateState(@Query("id") int id, @Query("state") String state, @Query("user_id") int userId);


    @POST("DetailByTitle")
    Call<Result<List<LostFound>>> DetailByTitle( @Query("title") String title,@Query("type") String type);


    @Multipart
    @POST("addLostFound")
    Call<Result<List<LostFound>>> addLostFound(
            @Part MultipartBody.Part file,
            @Query("lostFoundJson")String lostFoundJson

    );


    @POST("send_code")
    Call<Result<Object>> sendCode(@Query("email") String email);


    @POST("verify_code")
    Call<Result<Object>> verifyCode(@Query("email") String email, @Query("code") String code);


    @POST("resetPwd")
    Call<Result<Object>> resetPwd(
            @Query("phone") String phone,
            @Query("newPwd") String password
    );

    @POST("searchInfo")
    Call<Result<List<SearchInfo>>> searchInfo(@Query("value") String value);

    @POST("updateAc")
    Call<Result<User>> updateAccount(
            @Query("nickname") String nickname,
            @Query("sex") String sex,
            @Query("id") int id
    );

    @Multipart
    @POST("updatePic")
    Call<Result<User>> updatePhoto(
            @Part MultipartBody.Part file,
            @Query("id") int userId
    );

    @POST("register")
    Call<Result<LoginResponseDTO>> register(
            @Query("phone") String phone,
            @Query("email") String email,
            @Query("password") String password,
            @Query("role") int role
    );

    @POST("getIMUserToken")
    Call<Result<String>> getIMUserToken(
            @Query("uid") int uid,
            @Query("nickname") String nickname
    );


    @POST("getUserById")
    Call<User> getUserInfo(@Query("id") int id);


    @GET("getCommentsByLostFoundId")
    Call<Result<List<Comment>>> getComments(@Query("lostfound_id") int lostfoundId);


    @GET("getReceivedComments")
    Call<Result<List<Comment>>> getReceivedComments(@Query("user_id") int user_id);


    @FormUrlEncoded
    @POST("addComment")
    Call<Result<String>> addComment(
            @Field("lostfound_id") int lostfoundId,
            @Field("user_id") int userId,
            @Field("content") String content,
            @Field("parent_id") int parentId,
            @Field("reply_user_id") int replyUserId
    );


    @GET("getAllType")
    Call<Result<List<LostFoundType>>> getAllType();


    @GET("getComments")
    Call<Result<List<Comment>>> getCommentsByUserId(@Query("user_id") int user_id);


    @GET("delComment")
    Call<Result<String>>deleteComment(@Query("commentId") int commentId);


    @GET("admin/getLostFoundById")
    Call<Result<LostFound>>getLostFoundById(@Query("lostFoundId") int lostFoundId);

    @POST("updatePhone")
    @FormUrlEncoded
    Call<Result<User>> updatePhone(@Field("id") int id, @Field("newPhone") String newPhone, @Field("code") String code);


    @POST("updateEmail")
    @FormUrlEncoded
    Call<Result<User>> updateEmail(@Field("id") int id, @Field("newEmail") String newEmail, @Field("code") String code);


    @FormUrlEncoded
    @POST("deleteAccount")
    Call<Result<String>> deleteAccount(@Field("id") int id);


    @GET("message/userList")
    Call<Result<List<MessageVO>>> getMessagesByUserId(@Query("userId") Integer userId);


    @FormUrlEncoded
    @POST("message/deleteMessage")
    Call<Result<String>> deleteMessage(@Field("id") int id);

    
    @GET("shop/items")
    Call<Result<List<ShopItem>>> getShopItems();

    
    @POST("shop/exchange")
    Call<Result<String>> exchangeItem(
            @Query("userId") Integer userId,
            @Query("itemId") Integer itemId
    );

    
    @GET("shop/history")
    Call<Result<List<PointHistory>>> getPointHistory(@Query("userId") Integer userId);


    @POST("admin/verifyOrder")
    @FormUrlEncoded
    Call<Result<String>> verifyOrder(@Field("verifyCode") String verifyCode, @Field("adminId") int adminId);

    @GET("shop/myOrders")
    Call<Result<List<ExchangeOrder>>> getMyOrders(@Query("userId") Integer userId,@Query("keyword") String keyword);

    @FormUrlEncoded
    @POST("shop/deleteOrder")
    Call<Result<String>> deleteOrder(@Field("id") Integer id, @Field("userId")Integer userId);

    
    @GET
    Call<BaiduPoiResponse> searchPlaceBaidu(
            @retrofit2.http.Url String url,
            @Query("query") String query,
            @Query("region") String region,
            @Query("output") String output,
            @Query("ak") String ak
    );
}
