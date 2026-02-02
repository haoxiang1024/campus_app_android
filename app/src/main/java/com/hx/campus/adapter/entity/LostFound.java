package com.hx.campus.adapter.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;


public class LostFound implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;

    private String title;

    private String img;
    @SerializedName("pubDate")
    private Date pubDate;

    private String content;

    private String place;

    private String phone;

    private String state;

    private Integer stick;

    private Integer lostfoundtypeId;

    private Integer userId;
    private String nickname;
    private  String type;
    private LostFoundType lostfoundtype;

    public LostFound(Integer id, String title, String img, Date pubDate, String content, String place, String phone, String state, Integer stick, Integer lostfoundtype_id, Integer user_id, String nickname, String type, LostFoundType lostfoundtype) {
        this.id = id;
        this.title = title;
        this.img = img;
        this.pubDate = pubDate;
        this.content = content;
        this.place = place;
        this.phone = phone;
        this.state = state;
        this.stick = stick;
        this.lostfoundtypeId = lostfoundtype_id;
        this.userId = user_id;
        this.nickname = nickname;
        this.type = type;
        this.lostfoundtype = lostfoundtype;
    }

    public LostFoundType getLostfoundtype() {
        return lostfoundtype;
    }

    public void setLostfoundtype(LostFoundType lostfoundtype) {
        this.lostfoundtype = lostfoundtype;
    }

    public LostFound() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getImg() {
        return img;
    }


    public void setImg(String img) {
        this.img = img;
    }


    public Date getPubDate() {
        return pubDate;
    }


    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public String getPlace() {
        return place;
    }


    public void setPlace(String place) {
        this.place = place;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getState() {
        return state;
    }


    public void setState(String state) {
        this.state = state;
    }


    public Integer getStick() {
        return stick;
    }


    public void setStick(Integer stick) {
        this.stick = stick;
    }


    public Integer getLostfoundtypeId() {
        return lostfoundtypeId;
    }


    public void setLostfoundtypeId(Integer lostfoundtype_id) {
        this.lostfoundtypeId = lostfoundtype_id;
    }


    public Integer getUserId() {
        return userId;
    }


    public void setUserId(Integer user_id) {
        this.userId = user_id;
    }

    @Override
    public String toString() {
        return "LostFound{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", img='" + img + '\'' +
                ", pubDate=" + pubDate +
                ", content='" + content + '\'' +
                ", place='" + place + '\'' +
                ", phone='" + phone + '\'' +
                ", state='" + state + '\'' +
                ", stick=" + stick +
                ", lostfoundtypeId=" + lostfoundtypeId +
                ", userId=" + userId +
                ", nickname='" + nickname + '\'' +
                ", type='" + type + '\'' +
                ", lostfoundtype=" + lostfoundtype +
                '}';
    }
}