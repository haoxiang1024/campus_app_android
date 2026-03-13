package com.hx.campus.adapter.entity;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PointHistory implements Serializable {

    private int id;

    @SerializedName("user_id")
    private int userId;

    private int type; // 变动类型：1-发帖, 2-评论, 3-兑换消耗 等

    @SerializedName("points_changed")
    private int pointsChanged; // 变动数值，正数或负数

    private String description; // 例如 "兑换商品：校园定制笔记本"

    @SerializedName("create_time")
    private String createTime;

    // --- Getter 和 Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public int getPointsChanged() { return pointsChanged; }
    public void setPointsChanged(int pointsChanged) { this.pointsChanged = pointsChanged; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    // 可以加一个辅助方法，方便 UI 显示（比如给正数加上 '+' 号）
    public String getDisplayPoints() {
        if (pointsChanged > 0) {
            return "+" + pointsChanged;
        } else {
            return String.valueOf(pointsChanged); // 负数自带 '-' 号
        }
    }
}
