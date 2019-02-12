package com.taobao.zeus.web.controller;

<<<<<<< HEAD
import com.taobao.zeus.web.common.BusinessException;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.PageResponse;
import com.taobao.zeus.web.controller.response.ResponseStatus;
import com.taobao.zeus.web.controller.response.ReturnCode;
=======
import com.taobao.zeus.web.controller.response.*;
>>>>>>> migu
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公用控制器
 */
public class BaseController {

    /**
     * 设置返回状态
     *
<<<<<<< HEAD
     * @param status
     *            返回状态对象
     * @param returnCode
     *            返回码
=======
     * @param status     返回状态对象
     * @param returnCode 返回码
>>>>>>> migu
     */
    protected void setResponseStatus(ResponseStatus status, ReturnCode returnCode) {
        status.setCode(returnCode.getCode());
        status.setMessage(returnCode.getMessage());
    }

    /**
     * 设置返回状态
     *
<<<<<<< HEAD
     * @param status
     *            返回状态对象
     * @param returnCode
     *            返回码
     * @param message
     *            其他信息
=======
     * @param status     返回状态对象
     * @param returnCode 返回码
     * @param message    其他信息
>>>>>>> migu
     */
    protected void setResponseStatus(ResponseStatus status, ReturnCode returnCode, String message) {
        status.setCode(returnCode.getCode());
        status.setMessage(message + ", " + returnCode.getMessage());
    }

    /**
<<<<<<< HEAD
     * @param code
     *            错误码
     * @param data
     *            返回数据内容
     * @param <T>
     *            返回数据类型
=======
     * @param code 错误码
     * @param data 返回数据内容
     * @param <T>  返回数据类型
>>>>>>> migu
     * @return
     */
    protected <T> CommonResponse<T> buildResponse(ReturnCode code, T data) {
        return CommonResponse.create(code.getCode(), code.getMessage(), data);
    }

    /**
<<<<<<< HEAD
     * @param data
     *            返回数据内容
     * @param <T>
     *            返回数据类型
=======
     * @param data 返回数据内容
     * @param <T>  返回数据类型
>>>>>>> migu
     * @return
     */
    protected <T> CommonResponse<T> buildResponse(T data) {
        return CommonResponse.create(ReturnCode.SUCCESS.getCode(), ReturnCode.SUCCESS.getMessage(), data);
    }

    /**
<<<<<<< HEAD
     * @param data
     *            返回数据内容
     * @param <T>
     *            返回数据类型
=======
     * @param data 返回数据内容
     * @param <T>  返回数据类型
>>>>>>> migu
     * @return
     */
    protected <T> CommonResponse<T> buildResponse(T data, Object properties) {
        return CommonResponse.create(ReturnCode.SUCCESS.getCode(), ReturnCode.SUCCESS.getMessage(), data, properties);
    }

    /**
<<<<<<< HEAD
     * @param code
     *            错误码
=======
     * @param code 错误码
>>>>>>> migu
     * @return
     */
    protected CommonResponse<Void> buildResponse(ReturnCode code) {
        return CommonResponse.create(code.getCode(), code.getMessage());
    }

    /**
     * @return
     */
    protected CommonResponse<Void> buildResponse() {
        return CommonResponse.create(ReturnCode.SUCCESS.getCode(), ReturnCode.SUCCESS.getMessage());
    }

    protected String checkMobile(String mobile) {
        if (mobile == null || StringUtils.isEmpty(mobile)) {
            throw new BusinessException(ReturnCode.EMPTY_PARAMETER);
        }

        mobile = mobile.replaceAll("\\s+", "");
        mobile = mobile.replaceAll("-", "");
        mobile = mobile.replaceAll("\\+", "");

        Pattern pattern = Pattern.compile("1\\d{10}");
        Matcher matcher = pattern.matcher(mobile);
        if (!matcher.matches()) {
            throw new BusinessException(ReturnCode.MOBILE_ILLEGAL);
        }

        return mobile;
    }

    protected <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new BusinessException(ReturnCode.EMPTY_PARAMETER);
        }
        return reference;
    }

    protected void checkState(boolean expression) {
        if (!expression) {
            throw new BusinessException(ReturnCode.EMPTY_PARAMETER);
        }
    }

    /**
<<<<<<< HEAD
     * @param total
     *            总页数
     * @param rows
     *            返回数据内容
     * @param <T>
     *            返回数据类型
=======
     * @param total 总页数
     * @param rows  返回数据内容
     * @param <T>   返回数据类型
>>>>>>> migu
     * @return
     */
    protected <T> PageResponse<T> buildPageResponse(int total, T rows) {
        return PageResponse.create(total, rows);
    }

    /**
<<<<<<< HEAD
     * @param total
     *            总页数
     * @param rows
     *            返回数据内容
     * @param <T>
     *            返回数据类型
=======
     * @param total 总页数
     * @param rows  返回数据内容
     * @param <T>   返回数据类型
>>>>>>> migu
     * @return
     */
    protected <T> PageResponse<T> buildPageResponse(int rank, int total, T rows) {
        return PageResponse.create(rank, total, rows);
    }

    /**
<<<<<<< HEAD
     * @param code
     *            错误码
     * @param data
     *            返回数据内容
     * @param <T>
     *            返回数据类型
=======
     * @param code 错误码
     * @param data 返回数据内容
     * @param <T>  返回数据类型
>>>>>>> migu
     * @return
     */
    protected <T> PageResponse<T> buildPageResponse(ReturnCode code, T rows) {
        return PageResponse.create(code.getCode(), code.getMessage(), rows);
    }

    protected <T> PageResponse<T> buildPageResponse(ReturnCode code, int total, T rows) {
        return PageResponse.create(code.getCode(), total, rows);
    }
}
