package com.hx.campus.adapter.entity;

import java.util.Date;

public class ExchangeOrder {
    private Integer id;
    private String order_no;       // 订单号
    private Integer user_id;       // 用户ID
    private Integer item_id;       // 商品ID
    private String item_name;      // 商品名称(快照)
    private Integer points_cost;   // 消耗积分
    private String verify_code;    // 8位提货核验码
    private Integer status;       // 状态：0-待核验, 1-已核验, 2-已取消
    private Date create_time;
    private Date verify_time;//核验时间
    private Integer verify_admin_id; // 核验管理员ID
    private String item_image;

    public String getItem_image() {
        return item_image;
    }

    public void setItem_image(String item_image) {
        this.item_image = item_image;
    }
    public ExchangeOrder() {
    }

    @Override
    public String toString() {
        return "ExchangeOrder{" +
                "id=" + id +
                ", order_no='" + order_no + '\'' +
                ", user_id=" + user_id +
                ", item_id=" + item_id +
                ", item_name='" + item_name + '\'' +
                ", points_cost=" + points_cost +
                ", verify_code='" + verify_code + '\'' +
                ", status=" + status +
                ", create_time=" + create_time +
                ", verify_time=" + verify_time +
                ", verify_admin_id=" + verify_admin_id +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public Integer getPoints_cost() {
        return points_cost;
    }

    public void setPoints_cost(Integer points_cost) {
        this.points_cost = points_cost;
    }

    public String getVerify_code() {
        return verify_code;
    }

    public void setVerify_code(String verify_code) {
        this.verify_code = verify_code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Date getVerify_time() {
        return verify_time;
    }

    public void setVerify_time(Date verify_time) {
        this.verify_time = verify_time;
    }

    public Integer getVerify_admin_id() {
        return verify_admin_id;
    }

    public void setVerify_admin_id(Integer verify_admin_id) {
        this.verify_admin_id = verify_admin_id;
    }
}
