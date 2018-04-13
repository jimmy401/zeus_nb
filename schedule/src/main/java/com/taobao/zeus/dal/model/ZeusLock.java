package com.taobao.zeus.dal.model;

import java.util.Date;

public class ZeusLock {
    private Integer id;

    private Date gmtCreate=new Date();

    private Date gmtModified=new Date();

    private String host;

    private Date serverUpdate;

    private String subgroup;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    public Date getServerUpdate() {
        return serverUpdate;
    }

    public void setServerUpdate(Date serverUpdate) {
        this.serverUpdate = serverUpdate;
    }

    public String getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(String subgroup) {
        this.subgroup = subgroup == null ? null : subgroup.trim();
    }
}