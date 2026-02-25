package com.hx.campus.adapter.entity;

import java.util.Date;
import java.util.List;

public class Comment {
    private Integer id;
    private Integer lostfound_id;
    private Integer user_id;
    private String nickname;
    private String photo;
    private String content;
    private int state;
    private int parentId;          // 父评论ID
    private int replyUserId;       // 被回复者的用户ID
    private String replyNickname;  // 被回复者的昵称

    // 子评论列表
    private List<Comment> replies;

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(int replyUserId) {
        this.replyUserId = replyUserId;
    }

    public String getReplyNickname() {
        return replyNickname;
    }

    public void setReplyNickname(String replyNickname) {
        this.replyNickname = replyNickname;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", lostfound_id=" + lostfound_id +
                ", user_id=" + user_id +
                ", nickname='" + nickname + '\'' +
                ", photo='" + photo + '\'' +
                ", content='" + content + '\'' +
                ", state=" + state +
                ", parentId=" + parentId +
                ", replyUserId=" + replyUserId +
                ", replyNickname='" + replyNickname + '\'' +
                ", replies=" + replies +
                ", create_time=" + create_time +
                '}';
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Integer getLostfound_id() {
        return lostfound_id;
    }

    public void setLostfound_id(Integer lostfound_id) {
        this.lostfound_id = lostfound_id;
    }

    private Date create_time;



    public Comment() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
