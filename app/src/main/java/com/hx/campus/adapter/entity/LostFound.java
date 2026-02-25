package com.hx.campus.adapter.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;


/**
 * 失物招领实体类
 * 用于表示校园内的失物和招领信息
 */
public class LostFound implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 失物招领信息唯一标识符 */
    private Integer id;

    /** 标题 */
    private String title;

    /** 图片URL地址 */
    private String img;
    @SerializedName("pubDate")
    /** 发布日期 */
    private Date pub_date;

    /** 内容描述 */
    private String content;

    /** 地点 */
    private String place;

    /** 联系电话 */
    private String phone;

    /** 状态：寻找中/已找到 */
    private String state;

    /** 是否置顶：0-否，1-是 */
    private Integer stick;
    @SerializedName("lostfoundtypeId")
    /** 失物招领类型ID */
    private Integer lostfoundtype_id;
    @SerializedName("userId")
    /** 用户ID */
    private Integer user_id;
    /** 发布者昵称 */
    private String nickname;
    /** 类型：失物/招领 */
    private String type;
    /** 失物招领类型对象 */
    private LostFoundType lostfoundtype;

    public LostFound(Integer id, String title, String img, Date pubDate, String content, String place, String phone, String state, Integer stick, Integer lostfoundtype_id, Integer user_id, String nickname, String type, LostFoundType lostfoundtype) {
        this.id = id;
        this.title = title;
        this.img = img;
        this.pub_date = pubDate;
        this.content = content;
        this.place = place;
        this.phone = phone;
        this.state = state;
        this.stick = stick;
        this.lostfoundtype_id = lostfoundtype_id;
        this.user_id = user_id;
        this.nickname = nickname;
        this.type = type;
        this.lostfoundtype = lostfoundtype;
    }

    public LostFound(String title, String imageUrl, Date pubDate, String summary, String place, String phone, String state, String userName) {
        this.title=title;
        this.img=imageUrl;
        this.pub_date=pubDate;
        this.content=summary;
        this.place=place;
        this.phone=phone;
        this.state=state;
        this.nickname=userName;
    }

    public LostFound(String title, String img, Date pub_date, String content, String place, String phone, String state, Integer stick, Integer lostfoundtypeId, Integer user_id) {
        this.title = title;
        this.img = img;
        this.pub_date = pub_date;
        this.content = content;
        this.place = place;
        this.phone = phone;
        this.state = state;
        this.stick = stick;
        this.user_id = user_id;
        this.lostfoundtype_id = lostfoundtypeId;
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
        return pub_date;
    }


    public void setPubDate(Date pubDate) {
        this.pub_date = pubDate;
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
        return lostfoundtype_id;
    }


    public void setLostfoundtypeId(Integer lostfoundtype_id) {
        this.lostfoundtype_id = lostfoundtype_id;
    }


    public Integer getUserId() {
        return user_id;
    }


    public void setUserId(Integer user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "LostFound{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", img='" + img + '\'' +
                ", pub_date=" + pub_date +
                ", content='" + content + '\'' +
                ", place='" + place + '\'' +
                ", phone='" + phone + '\'' +
                ", state='" + state + '\'' +
                ", stick=" + stick +
                ", lostfoundtypeId=" + lostfoundtype_id +
                ", user_id=" + user_id +
                ", nickname='" + nickname + '\'' +
                ", type='" + type + '\'' +
                ", lostfoundtype=" + lostfoundtype +
                '}';
    }
}