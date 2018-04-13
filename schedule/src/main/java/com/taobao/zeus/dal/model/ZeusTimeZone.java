package com.taobao.zeus.dal.model;

public class ZeusTimeZone {
    private Integer id;

    private String timezone;

    private Long uid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone == null ? null : timezone.trim();
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}