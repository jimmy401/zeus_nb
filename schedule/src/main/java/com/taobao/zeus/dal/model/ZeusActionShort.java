package com.taobao.zeus.dal.model;

import java.io.Serializable;

public class ZeusActionShort implements Serializable {
    private static final long serialVersionUID = 1L;

    public ZeusActionShort(Long id){
        this.setId(id);
    }

    private Long id;

    private String name;

    private Long jobId;

    private String text;

    public String getText() {
        return this.getId().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
}