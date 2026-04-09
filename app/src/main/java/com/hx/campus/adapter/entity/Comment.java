/**
 * 评论实体类
 * 用于封装失物招领系统的评论信息，支持层级评论结构
 * 包含评论基本信息、用户信息以及回复评论的嵌套结构
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2024
 */
package com.hx.campus.adapter.entity;

import java.util.Date;
import java.util.List;

/**
 * 评论数据模型类
 * 支持一级评论和多级回复评论的树形结构
 */
public class Comment {
    /** 评论唯一标识符 */
    private Integer id;
    
    /** 关联的失物招领信息ID */
    private Integer lostfound_id;
    
    /** 评论发布者用户ID */
    private Integer user_id;
    
    /** 发布者昵称 */
    private String nickname;
    
    /** 发布者头像URL */
    private String photo;
    
    /** 评论内容 */
    private String content;
    
    /** 评论状态：0-正常，1-删除 */
    private int state;
    
    /** 父评论ID，用于构建评论层级关系 */
    private int parent_id;
    
    /** 被回复用户的ID */
    private int reply_user_id;
    
    /** 被回复用户的昵称 */
    private String reply_nickname;

    /** 回复评论列表，用于存储该评论下的所有回复 */
    private List<Comment> replies;

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public int getReply_user_id() {
        return reply_user_id;
    }

    public void setReply_user_id(int reply_user_id) {
        this.reply_user_id = reply_user_id;
    }

    public String getReply_nickname() {
        return reply_nickname;
    }

    public void setReply_nickname(String reply_nickname) {
        this.reply_nickname = reply_nickname;
    }

    /**
     * 获取回复评论列表
     * @return 回复评论集合
     */
    public List<Comment> getReplies() {
        return replies;
    }

    /**
     * 设置回复评论列表
     * @param replies 回复评论集合
     */
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    /**
     * 重写toString方法，便于调试和日志输出
     * @return 包含所有字段信息的字符串表示
     */
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
                ", parent_id=" + parent_id +
                ", reply_user_id=" + reply_user_id +
                ", reply_nickname='" + reply_nickname + '\'' +
                ", replies=" + replies +
                ", create_time=" + create_time +
                '}';
    }

    /**
     * 获取评论状态
     * @return 状态码
     */
    public int getState() {
        return state;
    }

    /**
     * 设置评论状态
     * @param state 状态码
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * 获取用户ID
     * @return 用户唯一标识符
     */
    public Integer getUser_id() {
        return user_id;
    }

    /**
     * 设置用户ID
     * @param user_id 用户ID
     */
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    /**
     * 获取创建时间
     * @return 评论发布时间
     */
    public Date getCreate_time() {
        return create_time;
    }

    /**
     * 设置创建时间
     * @param create_time 创建时间
     */
    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    /**
     * 获取关联的失物招领ID
     * @return 失物招领信息ID
     */
    public Integer getLostfound_id() {
        return lostfound_id;
    }

    /**
     * 设置关联的失物招领ID
     * @param lostfound_id 失物招领ID
     */
    public void setLostfound_id(Integer lostfound_id) {
        this.lostfound_id = lostfound_id;
    }

    /** 评论创建时间 */
    private Date create_time;

    /**
     * 默认构造函数
     */
    public Comment() {
    }

    /**
     * 获取评论ID
     * @return 评论唯一标识符
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置评论ID
     * @param id 评论ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取用户昵称
     * @return 发布者昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置用户昵称
     * @param nickname 用户昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取用户头像
     * @return 头像URL地址
     */
    public String getPhoto() {
        return photo;
    }

    /**
     * 设置用户头像
     * @param photo 头像URL
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * 获取评论内容
     * @return 评论文本内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置评论内容
     * @param content 评论文本
     */
    public void setContent(String content) {
        this.content = content;
    }
}
