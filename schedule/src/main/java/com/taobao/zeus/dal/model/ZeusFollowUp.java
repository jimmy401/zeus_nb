package com.taobao.zeus.dal.model;

import java.util.Date;
/**
 * 用户关注的Group或者Job
 * @author zhoufang
 *
 */
public class ZeusFollowUp {
    public static final Integer GroupType=1;
    public static final Integer JobType=2;
    private Long id;

    private Date gmtCreate;

    private Date gmtModified;
    /**
     * 关注的目标id
     * 如果关注group  则这里是group id
     * 如果关注的是Job  则这里是Job id
     */
    private Long targetId;
    /**
     * 关注的类型
     * 1：group  2：Job
     */
    private Integer type;

    private String uid;
    /**
     * 0表示不是重要联系人，1表示是重要联系人
     */
    private Integer important;

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

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public Integer getImportant() {
        return important;
    }

    public void setImportant(Integer important) {
        this.important = important;
    }
}