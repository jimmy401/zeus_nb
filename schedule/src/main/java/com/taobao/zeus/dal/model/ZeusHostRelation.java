package com.taobao.zeus.dal.model;

public class ZeusHostRelation {
    private Long id;

    private String host;

    private Integer hostGroupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    public Integer getHostGroupId() {
        return hostGroupId;
    }

    public void setHostGroupId(Integer hostGroupId) {
        this.hostGroupId = hostGroupId;
    }
}