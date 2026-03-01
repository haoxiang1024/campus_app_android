package com.hx.campus.adapter.entity;

/**
 * 互动消息实体类
 * 用于表示用户间的互动评论消息
 */
//回复评论
public class InteractionMsg {
    /** 帖子ID */
    public int lostfoundId; // 帖子ID
    /** 这条评论自身的ID */
    public int commentId;   // 这条评论自身的ID
    /** 发这条评论的人的ID */
    public int userId;      // 发这条评论的人的ID
    /** 评论人昵称 */
    public String username;     // 评论人昵称
    /** 评论人头像链接 */
    public String avatarUrl;    // 评论人头像链接
    /** 评论内容 */
    public String content;      // 评论内容
    /** 评论时间 */
    public String time;         // 评论时间

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
