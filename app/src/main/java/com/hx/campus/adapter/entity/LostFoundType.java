package com.hx.campus.adapter.entity;

import java.io.Serializable;


/**
 * 失物招领类型实体类
 * 用于分类管理失物招领信息的类型
 * 实现Serializable接口支持序列化存储
 */
public class LostFoundType implements Serializable {
    private static final long serialVersionUID = 112447865142901463L;

    /** 类型唯一标识符 */
    private Integer id;

    /** 类型名称 */
    private String name;

    public LostFoundType() {
    }

    public LostFoundType(Integer id, String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return name;
    }
}

