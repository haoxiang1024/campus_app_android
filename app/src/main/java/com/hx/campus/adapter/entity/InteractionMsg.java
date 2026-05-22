package com.hx.campus.adapter.entity;



public class InteractionMsg {
    
    public int lostfoundId;
    
    public int commentId;
    
    public int userId;
    
    public String username;
    
    public String avatarUrl;
    
    public String content;
    
    public String time;

    @Override
    public String toString() {
        return "InteractionMsg{" +
                "lostfoundId=" + lostfoundId +
                ", commentId=" + commentId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public int getLostfoundId() {
        return lostfoundId;
    }

    public void setLostfoundId(int lostfoundId) {
        this.lostfoundId = lostfoundId;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public InteractionMsg() {
    }


    public InteractionMsg(String username, String content, String time) {
        this.username = username;
        this.content = content;
        this.time = time;
    }






    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
