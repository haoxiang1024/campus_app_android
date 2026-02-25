/**
 * API接口统一响应结果封装类
 * 用于标准化服务器返回的数据格式，便于前端统一处理
 * 支持泛型，可适应不同接口返回的不同数据类型
 * 
 * @param <T> 泛型参数，表示实际返回的数据类型
 * @author 开发团队
 * @version 1.0.0
 * @since 2024
 */
package com.hx.campus.utils.api;

/**
 * API响应结果通用封装类
 * 遵循RESTful API设计规范，提供标准的成功/失败判断机制
 * 
 * @param <T> 响应数据的具体类型
 */
public class Result<T> {
    /** 状态码：0表示成功，1表示业务失败，500表示服务器内部错误 */
    private int status;

    /** 泛型数据载体，存储接口返回的具体业务数据 */
    private T data;

    /** 响应消息，用于向用户展示操作结果说明 */
    private String msg;

    /**
     * 无参构造函数
     * 主要供Gson等JSON解析库反射调用使用
     */

    /**
     * 默认构造函数
     * 供JSON反序列化使用
     */
    public Result() {
    }

    /**
     * 获取响应状态码
     * @return 状态码值
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置响应状态码
     * @param status 状态码值
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 获取响应数据
     * @return 泛型数据对象
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     * @param data 泛型数据对象
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取响应消息
     * @return 消息字符串
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置响应消息
     * @param msg 消息字符串
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 判断API调用是否成功
     * 约定状态码为0时表示操作成功
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return this.status == 0;
    }
}
