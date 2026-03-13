package com.hx.campus.adapter.entity;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class ShopItem  {
    private Integer id;
    private String name;
    private String description;
    private String image_url;
    private Integer required_points;
    private Integer stock;
    private Integer status; // 0-下架, 1-上架
    private Date create_time;
    private Date update_time;

    public ShopItem() {
    }

    @Override
    public String toString() {
        return "ShopItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image_url='" + image_url + '\'' +
                ", required_points=" + required_points +
                ", stock=" + stock +
                ", status=" + status +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                '}';
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Integer getRequired_points() {
        return required_points;
    }

    public void setRequired_points(Integer required_points) {
        this.required_points = required_points;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}