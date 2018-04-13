package com.taobao.zeus.dal.model;

import java.util.Date;

public class ZeusProfile {
    private Long id;

    private Date gmtCreate=new Date();

    private Date gmtModified=new Date();

    private String hadoopConf;

    private String uid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getHadoopConf() {
        return hadoopConf;
    }

    public void setHadoopConf(String hadoopConf) {
        this.hadoopConf = hadoopConf == null ? null : hadoopConf.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }
}