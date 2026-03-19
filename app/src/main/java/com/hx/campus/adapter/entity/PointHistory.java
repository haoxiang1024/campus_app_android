package com.hx.campus.adapter.entity;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PointHistory implements Serializable {

    private int id;

    @SerializedName("user_id")
    private int user_id;

    private int type; // 变动类型：1-发帖, 2-评论, 3-兑换消耗 等

    @SerializedName("points_changed")
    private int points_changed; // 变动数值，正数或负数

    private String description; // 例如 "兑换商品：校园定制笔记本"

    @SerializedName("create_time")
    private String create_time;

    // --- Getter 和 Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getuser_id() { return user_id; }
    public void setuser_id(int user_id) { this.user_id = user_id; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public int getpoints_changed() { return points_changed; }
    public void setpoints_changed(int points_changed) { this.points_changed = points_changed; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getcreate_time() { return create_time; }
    public void setcreate_time(String create_time) { this.create_time = create_time; }

    public String getDisplayPoints() {
        if (points_changed > 0) {
            return "+" + points_changed;
        } else {
            return String.valueOf(points_changed); // 负数自带 '-' 号
        }
    }
    public String getTypeText() {
        switch (type) {
            case 1: return "发帖奖励";
            case 2: return "评论奖励";
            case 3: return "兑换商品消耗";
            case 4: return "系统扣除";
            default: return "其他变动";
        }
    }
    public String getFormattedTime() {
        if (create_time == null) return "";
        // 假设原始格式是 "yyyy-MM-dd HH:mm:ss"，这里转化为更易读的格式
        try {
            return (create_time.length() > 16 ? create_time.substring(0, 16) : create_time)
                    .replace("T", " ");        } catch (Exception e) {
            return create_time;
        }
}}
