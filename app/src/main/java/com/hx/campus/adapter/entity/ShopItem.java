package com.hx.campus.adapter.entity;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ShopItem implements Serializable {

    private int id;

    private String name;

    private String description;

    // 映射后端的 image_url 字段
    @SerializedName("image_url")
    private String imageUrl;

    // 映射后端的 required_points 字段
    @SerializedName("required_points")
    private int requiredPoints;

    private int stock;

    private int status; // 0-下架，1-上架

    @SerializedName("create_time")
    private String createTime; // 可以用 String 接收格式化的时间，或者用 java.util.Date

    @SerializedName("update_time")
    private String updateTime;

    // --- Getter 和 Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getRequiredPoints() { return requiredPoints; }
    public void setRequiredPoints(int requiredPoints) { this.requiredPoints = requiredPoints; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}