package com.taobao.zeus.web.controller.response;

/**
 * 返回对象
 */
public class ResponseStatus {

    // 返回码
    private String code;

    // 返回信息
    private String message;

    public ResponseStatus() {
    }

    public ResponseStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseStatus{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
