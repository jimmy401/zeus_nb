package com.taobao.zeus.web.controller.response;

/**
 * 返回代码
 */
public enum ReturnCode {

    SUCCESS("00000", "success"),

    // 系统错误
    SYSTEM_ERROR("11000", "系统出错"),
    EMPTY_PARAMETER("12000", "缺少参数"),
    INVALID_PARAMETER("12001", "参数无效"),
    DEP_NOT_FOUND("12003", "部门不存在"),
    
    HOUSE_NOT_FOUND("12004", "房源不存在"),
    MOBILE_ILLEGAL("12005", "手机号输入错误"),
    OWNER_NOT_FOUND("12006","业主信息不存在"),
    HOUSE_NOT_IN_SELL("12007", "房源已下架"),

    // 业务类错误
    MOBILE_ALREADY_REGISTERED("20011", "已存在此客户"),
    CUSTOMER_REMARK_EMPTY("20012","客户备注为空"),
    WORKORDER_RECORD_EMPTY("20013","工单跟进为空"),
    AGENT_WORK_ORDER("20014","经纪人工单"),
    CUST_INTENTION("20015","客户意向错误"),
    SMS_ALREADYSEND("20016","亲，你今天已经发过联系方式了，请明天再发！"),
    CUSTOMER_NOT_EXIST("20017","客户不存在"),
    NOT_C_CUSTOMER("20018","该客户不是C端客户"),
    USER_BUSINESS_MOBILE_NOT_EXIST("20019","坐席工作手机号不存在"),

    // 预约类错误
    APPOINTMENT_FAIL("40001", "预约失败"),
    CAN_NOT_MAKE_ANOTHER_APPOIMENT("40002", "该房源已经预约且有效，不能再次预约"), 
    APPOINTMENT_DO_NOT_EXIST("40003", "预约不存在"), 
    APPOINTMENT_HAVE_FINISHED("40004", "预约单已完成"), 
    WORKORDER_LIST("40005", "获取工单列表发生错误"), 
    CUSTOMER_DETAIL("40006", "获取客户详细信息失败"),
    REGION_ERROR("40007","城市区域模块发生错误"),
    CELL_ERROR("40008","小区相关发生错误"),
    WORKORDER_ERROR("40009", "工单错误"), 
    INVALID_APPOINT_TIME("40010", "预约时间无效"), 
    APPOINT_CANT_COMMENTED("40011", "预约未完成，不可评价"),
    APPOINT_CANCELED("40012", "预约单已取消，不可分配经纪人"),
    EXCEED_ONEDAY_PRESS_UP_LINE("40013", "您今天的催约次数太多了"),
    EXCEED_ONEDAY_APPOINTMENT_UP_LINE("40014", "今天的预约次数已经到了"),
    HAVE_APPOINT_AGENT("40015", "该房源您已约过经纪人，不可直约业主"),
    CUST_MOBILE_DENIED("40016", "该客户手机号被列入黑名单"),
    DIRECT_ALREADY_COMMENTED("40017", "该直约预约已经评价了"),
    TIME_NOT_FIT_INVAILABLE("40018","“业主同意，时间不合适”的功能暂时未开放"),
    APPOINT_STATUS_CHANGE_INVALID("40019", "预约单变更状态不符合规则"),
    DIRECT_COMMENT_NOT_EXISTS("40020", "直约评价记录不存在"),
    REPORTER_NOT_FIT_COMMENT("40021", "举报者不是此评价记录的被评价人"),
    DATA_DUPLICATION("40022","您所添加的数据已经存在了"),
    // 垫底
    I_AM_BOTTOM("99999", "i am bottom");
	
	
    
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
