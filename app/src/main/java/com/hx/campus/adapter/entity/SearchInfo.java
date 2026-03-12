package com.hx.campus.adapter.entity;

import java.util.Date;
import java.util.Objects;

/**
 * 搜索信息实体类
 * 用于封装搜索结果的信息
 */
public class SearchInfo {

    /** 失物招领信息唯一标识符 */
    private Integer id;

    /** 标题 */
    private String title;

    /** 图片URL地址 */
    private String img;

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

    /** 失物招领类型ID */
    private Integer lostfoundtype_id;

    /** 失物招领类型对象 */
    private LostFoundType lostfoundtype;

    /** 用户ID */
    private Integer user_id;

    /** 发布者昵称 */
    private String nickname;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SearchInfo() {
    }

    public SearchInfo(String content, Integer id, String img, LostFoundType lostfoundtype, Integer lostfoundtype_id, String nickname, String phone, String place, Date pub_date, String state, Integer stick, String title, String type, Integer user_id) {
        this.content = content;
        this.id = id;
        this.img = img;
        this.lostfoundtype = lostfoundtype;
        this.lostfoundtype_id = lostfoundtype_id;
        this.nickname = nickname;
        this.phone = phone;
        this.place = place;
        this.pub_date = pub_date;
        this.state = state;
        this.stick = stick;
        this.title = title;
        this.type = type;
        this.user_id = user_id;
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

    public Date getPub_date() {
        return pub_date;
    }

    public void setPub_date(Date pub_date) {
        this.pub_date = pub_date;
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

    public Integer getLostfoundtype_id() {
        return lostfoundtype_id;
    }

    public void setLostfoundtype_id(Integer lostfoundtype_id) {
        this.lostfoundtype_id = lostfoundtype_id;
    }

    public LostFoundType getLostfoundtype() {
        return lostfoundtype;
    }

    public void setLostfoundtype(LostFoundType lostfoundtype) {
        this.lostfoundtype = lostfoundtype;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchInfo that = (SearchInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(img, that.img) && Objects.equals(pub_date, that.pub_date) && Objects.equals(content, that.content) && Objects.equals(place, that.place) && Objects.equals(phone, that.phone) && Objects.equals(state, that.state) && Objects.equals(stick, that.stick) && Objects.equals(lostfoundtype_id, that.lostfoundtype_id) && Objects.equals(lostfoundtype, that.lostfoundtype) && Objects.equals(user_id, that.user_id) && Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, img, pub_date, content, place, phone, state, stick, lostfoundtype_id, lostfoundtype, user_id, nickname);
    }

    @Override
    public String toString() {
        return "SearchInfo{" +
                "content='" + content + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", img='" + img + '\'' +
                ", pub_date=" + pub_date +
                ", place='" + place + '\'' +
                ", phone='" + phone + '\'' +
                ", state='" + state + '\'' +
                ", stick=" + stick +
                ", lostfoundtype_id=" + lostfoundtype_id +
                ", lostfoundtype=" + lostfoundtype +
                ", user_id=" + user_id +
                ", nickname='" + nickname + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
