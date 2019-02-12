package com.taobao.zeus.web.controller.response;

/**
 * 业务异常对象
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -3431129937307041307L;

    private ReturnCode returnCode;

    public BusinessException(ReturnCode returnCode) {
        super(returnCode.getMessage());
        this.returnCode = returnCode;
    }

    public BusinessException(String message, ReturnCode returnCode) {
        super(message);
        this.returnCode = returnCode;
    }

    public BusinessException(ReturnCode returnCode, Throwable cause) {
        super(cause);
        this.returnCode = returnCode;
    }

    public BusinessException(String message, Throwable cause, ReturnCode returnCode) {
        super(message, cause);
        this.returnCode = returnCode;
    }

    public ReturnCode getReturnCode() {
        return returnCode;
    }

}
