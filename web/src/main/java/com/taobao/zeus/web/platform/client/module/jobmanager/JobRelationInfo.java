package com.taobao.zeus.web.platform.client.module.jobmanager;

import java.util.List;

public class JobRelationInfo {

    private String id;
    private String name;

    private String jobId;
    private String historyId;
    private String lastStatus;
    private String lastRuntime;

    private List<JobRelationInfo> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getLastRuntime() {
        return lastRuntime;
    }

    public void setLastRuntime(String lastRuntime) {
        this.lastRuntime = lastRuntime;
    }

    public List<JobRelationInfo> getChildren() {
        return children;
    }

    public void setChildren(List<JobRelationInfo> children) {
        this.children = children;
    }
}
