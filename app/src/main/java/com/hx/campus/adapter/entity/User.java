package com.hx.campus.adapter.entity;


import java.io.Serializable;
import java.util.Date;


/**
 * 用户实体类
 * 用于表示校园应用中的用户信息
 * 实现Serializable接口支持序列化存储
 */
public class User implements Serializable {
    /** 用户唯一标识符 */
    private Integer id;
    /** 用户密码 */
    private String password;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像URL地址 */
    private String photo;
    /** 用户性别 */
    private String sex;
    /** 用户手机号码 */
    private String phone;
    private int points; // 用户当前积分余额

    /** 用户注册日期 */
    private Date reg_date;
    /** 用户邮箱地址 */
    private String email;
    /** 用户状态：0-正常，1-禁用 */
    private int state;
    /** 用户角色：0-普通用户，1-管理员 */
    private int role;
    /** 即时通讯token */
    private String im_token;

    public String getIm_token() {
        return im_token;
    }

    public void setIm_token(String im_token) {
        this.im_token = im_token;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getstate() {
        return state;
    }

    public void setstate(int state) {
        this.state = state;
    }

    public User(String phone, String password, String email) {
        this.phone = phone;
        this.password = password;
        this.email = email;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String photo, String phone, String sex, int points, Date reg_date, String email, int state, int role, String nickname, String password) {
        this.photo = photo;
        this.phone = phone;
        this.sex = sex;
        this.points = points;
        this.reg_date = reg_date;
        this.email = email;
        this.state = state;
        this.role = role;
        this.nickname = nickname;
        this.password = password;
    }

    public User() {
    }

    /**
     *
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     */
    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    /**
     *
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     */
    public String getNickname() {
        return nickname;
    }

    /**
     *
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     *
     */
    public String getPhoto() {
        return photo;
    }

    /**
     *
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     *
     */
    public String getSex() {
        return sex;
    }

    /**
     *
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     *
     */
    public String getPhone() {
        return phone;
    }

    /**
     *
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     *
     */



    /**
     *
     */
    public Date getReg_date() {
        return reg_date;
    }

    /**
     *
     */
    public void setReg_date(Date reg_date) {
        this.reg_date = reg_date;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", id=" + id +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", photo='" + photo + '\'' +
                ", sex='" + sex + '\'' +
                ", phone='" + phone + '\'' +
                ", points=" + points +
                ", reg_date=" + reg_date +
                ", state=" + state +
                ", role=" + role +
                ", im_token='" + im_token + '\'' +
                '}';
    }
}
