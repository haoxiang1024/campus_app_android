package com.hx.campus.adapter.entity;

/**
 * 登录成功返回的DTO
 */
public class LoginResponseDTO {
    // 用户信息
    private User userInfo;
    // JWT Token
    private String token;

    // 构造方法
    public LoginResponseDTO(User userInfo, String token) {
        this.userInfo = userInfo;
        this.token = token;
    }

    public LoginResponseDTO() {

    }

    public User getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
