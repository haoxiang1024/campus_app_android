package com.hx.campus.adapter.entity;


public class LoginResponseDTO {

    private User userInfo;

    private String token;


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
