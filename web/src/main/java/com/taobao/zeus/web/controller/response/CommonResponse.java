package com.taobao.zeus.web.controller.response;

/**
 * 通用返回结构
 */
public class CommonResponse<T> {

    private String code;

    private String msg;

    private T data;
    
    private Object properties;

    private CommonResponse() {
        this.code = "00000";
        this.msg = "";
    }

    private CommonResponse(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    private CommonResponse(String code, String msg, T data,Object properties) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.properties = properties;
    }

    private CommonResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> CommonResponse<T> create(String code, String msg, T data) {
        return new CommonResponse<T>(code, msg, data);
    }
    
    public static <T> CommonResponse<T> create(String code, String msg, T data,Object properties) {
        return new CommonResponse<T>(code, msg, data,properties);
    }

    public static CommonResponse<Void> create(String code, String msg) {
        return new CommonResponse<Void>(code, msg);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }
}
