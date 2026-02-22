

package com.hx.campus.adapter.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class NewInfo {


    private String UserName ;

    private String Tag;

    private String Title;

    private String Summary;


    private String ImageUrl;

    private int Praise;

    private int Comment;

    private int Read;
    private String DetailUrl;
    private Integer user_id;
    private String uniquekey;//新闻唯一码用于获取新闻内容
    private String State;//状态
    private String Phone;//联系方式
    private String Place;//地点
    @SerializedName("pubDate")
    private Date pub_date;

    public Date getPub_Date() {
        return pub_date;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public NewInfo setUser_id(Integer user_id) {
        this.user_id = user_id;
        return this;
    }

    public NewInfo setPub_Date(Date pub_Date) {
        this.pub_date = pub_Date;
        return this;
    }

    public String getPhone() {
        return Phone;
    }

    public NewInfo setPhone(String phone) {
        Phone = phone;
        return this;
    }

    public String getPlace() {
        return Place;
    }

    public NewInfo setPlace(String place) {
        Place = place;
        return this;
    }

    public String getState() {
        return State;
    }

    public NewInfo setState(String state) {
        State = state;
        return this;
    }

    public NewInfo() {

    }

    public NewInfo(String userName, String tag, String title, String summary, String imageUrl, int praise, int comment, int read, String detailUrl) {
        UserName = userName;
        Tag = tag;
        Title = title;
        Summary = summary;
        ImageUrl = imageUrl;
        Praise = praise;
        Comment = comment;
        Read = read;
        DetailUrl = detailUrl;
    }

    public NewInfo(String tag, String title, String summary, String imageUrl, String detailUrl) {
        Tag = tag;
        Title = title;
        Summary = summary;
        ImageUrl = imageUrl;
        DetailUrl = detailUrl;
    }

    public NewInfo(String tag, String title) {
        Tag = tag;
        Title = title;

        Praise = (int) (Math.random() * 100 + 5);
        Comment = (int) (Math.random() * 50 + 5);
        Read = (int) (Math.random() * 500 + 50);
    }

    public String getUniquekey() {
        return uniquekey;
    }

    public NewInfo setUniquekey(String uniquekey) {
        this.uniquekey = uniquekey;
        return this;
    }

    public String getUserName() {
        return UserName;
    }

    public NewInfo setUserName(String userName) {
        UserName = userName;
        return this;
    }

    public String getTag() {
        return Tag;
    }

    public NewInfo setTag(String tag) {
        Tag = tag;
        return this;
    }

    public String getTitle() {
        return Title;
    }

    public NewInfo setTitle(String title) {
        Title = title;
        return this;
    }

    public String getSummary() {
        return Summary;
    }

    public NewInfo setSummary(String summary) {
        Summary = summary;
        return this;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public NewInfo setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
        return this;
    }

    public int getPraise() {
        return Praise;
    }

    public NewInfo setPraise(int praise) {
        Praise = praise;
        return this;
    }

    public int getComment() {
        return Comment;
    }

    public NewInfo setComment(int comment) {
        Comment = comment;
        return this;
    }

    public int getRead() {
        return Read;
    }

    public NewInfo setRead(int read) {
        Read = read;
        return this;
    }

    public String getDetailUrl() {
        return DetailUrl;
    }

    public NewInfo setDetailUrl(String detailUrl) {
        DetailUrl = detailUrl;
        return this;
    }

    @Override
    public String toString() {
        return "NewInfo{" +
                "UserName='" + UserName + '\'' +
                ", Tag='" + Tag + '\'' +
                ", Title='" + Title + '\'' +
                ", Summary='" + Summary + '\'' +
                ", ImageUrl='" + ImageUrl + '\'' +
                ", Praise=" + Praise +
                ", Comment=" + Comment +
                ", Read=" + Read +
                ", DetailUrl='" + DetailUrl + '\'' +
                ", user_id=" + user_id +
                ", uniquekey='" + uniquekey + '\'' +
                ", State='" + State + '\'' +
                ", Phone='" + Phone + '\'' +
                ", Place='" + Place + '\'' +
                ", pub_date=" + pub_date +
                '}';
    }
}
