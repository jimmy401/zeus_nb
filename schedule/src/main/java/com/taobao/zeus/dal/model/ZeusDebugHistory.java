package com.taobao.zeus.dal.model;

import java.util.Date;

public class ZeusDebugHistory {
    private Long id;

    private Date endTime;

    private String executeHost;

    private Long fileId;

    private Date gmtCreate;

    private Date gmtModified;

    private String runtype;

    private Date startTime;

    private String status;

    private String owner;

    private Integer hostGroupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getExecuteHost() {
        return executeHost;
    }

    public void setExecuteHost(String executeHost) {
        this.executeHost = executeHost == null ? null : executeHost.trim();
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
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

    public String getRuntype() {
        return runtype;
    }

    public void setRuntype(String runtype) {
        this.runtype = runtype == null ? null : runtype.trim();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? null : owner.trim();
    }

    public Integer getHostGroupId() {
        return hostGroupId;
    }

    public void setHostGroupId(Integer hostGroupId) {
        this.hostGroupId = hostGroupId;
    }
}