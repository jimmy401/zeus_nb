package com.taobao.zeus.dal.model;

public class ZeusActionReport {

    private Long actionId;
    private int actionCnt;
    private String gmtCreate;
    private String owner;
    private String name;

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public int getActionCnt() {
        return actionCnt;
    }

    public void setActionCnt(int actionCnt) {
        this.actionCnt = actionCnt;
    }

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
