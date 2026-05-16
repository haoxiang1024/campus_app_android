
package com.hx.campus.utils.api;


public class Result<T> {
    
    private int status;

    
    private T data;

    
    private String msg;

    

    
    public Result() {
    }

    
    public int getStatus() {
        return status;
    }

    
    public void setStatus(int status) {
        this.status = status;
    }

    
    public T getData() {
        return data;
    }

    
    public void setData(T data) {
        this.data = data;
    }

    
    public String getMsg() {
        return msg;
    }

    
    public void setMsg(String msg) {
        this.msg = msg;
    }

    
    public boolean isSuccess() {
        return this.status == 0;
    }
}
