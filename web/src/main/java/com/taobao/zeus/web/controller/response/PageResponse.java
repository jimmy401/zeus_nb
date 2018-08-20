package com.taobao.zeus.web.controller.response;

/**
 * @author duxiaofu
 * @date 2016年1月18日 下午4:12:31
 */
public class PageResponse<T> {
    private T rows;
    private int total;
    private int rank;
    private String code;
    private String msg;

    private PageResponse(int total, T rows) {
        this.rows = rows;
        this.total = total;
    }

    private PageResponse(int rank, int total, T rows) {
        this.rank = rank;
        this.rows = rows;
        this.total = total;
    }

    private PageResponse(String code, String msg, T rows) {
        this.rows = rows;
        this.code = code;
        this.msg = msg;
    }

    private PageResponse(String code, int total, T rows) {
        this.rows = rows;
        this.code = code;
        this.total = total;
    }

    public static <T> PageResponse<T> create(int total, T rows) {
        return new PageResponse<T>(total, rows);
    }

    public static <T> PageResponse<T> create(int rank, int total, T rows) {
        return new PageResponse<T>(rank, total, rows);
    }

    public static <T> PageResponse<T> create(String code, String msg, T rows) {
        return new PageResponse<T>(code, msg, rows);
    }

    public static <T> PageResponse<T> create(String code, int total, T rows) {
        return new PageResponse<T>(code, total, rows);
    }

    public T getRows() {
        return rows;
    }

    public void setRows(T rows) {
        this.rows = rows;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
