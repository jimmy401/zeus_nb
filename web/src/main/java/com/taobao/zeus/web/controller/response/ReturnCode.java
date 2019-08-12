package com.taobao.zeus.web.controller.response;

/**
 * 返回代码
 */
public enum ReturnCode {

    SUCCESS("00000", "success"),
    FAILED("10000", "failed"),
    HEADER("10001", "header"),
    TAIL("10002", "tail"),

    // 系统错误
    SYSTEM_ERROR("11000", "系统出错"),
    EMPTY_PARAMETER("12000", "缺少参数"),
    INVALID_PARAMETER("12001", "参数无效"),
    INVALID_ERROR("55555", "您没有权限操作"),
    TARGET_ERROR("40001", "目标对象不是文件夹");
    
    private String code;

    private String msg;

    ReturnCode(String code, String message) {
        this.code = code;
        this.msg = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return msg;
    }

}
