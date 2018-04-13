package com.taobao.zeus.dal.model;

import java.io.Serializable;
import java.util.Date;

public class ZeusGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String descr;

    /**
     * 0: 是目录
     * 1：不是目录
     */

    private Integer directory;

    private Date gmtCreate=new Date();

    private Date gmtModified=new Date();

    private String name;

    private String owner;

    private Integer parent;

    private Integer existed=1;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr == null ? null : descr.trim();
    }

    public Integer getDirectory() {
        return directory;
    }

    public void setDirectory(Integer directory) {
        this.directory = directory;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? null : owner.trim();
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getExisted() {
        return existed;
    }

    public void setExisted(Integer existed) {
        this.existed = existed;
    }
}