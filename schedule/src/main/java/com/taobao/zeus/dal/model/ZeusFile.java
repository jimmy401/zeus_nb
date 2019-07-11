package com.taobao.zeus.dal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ZeusFile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static int FOLDER=1;
    public static int FILE=2;

    private Long id;

    private Date gmtCreate=new Date();

    private Date gmtModified=new Date();

    private String name;

    private String owner;

    private Long parent;

    private Integer type;

    private Integer hostGroupId;

    private String content;

    private Integer category;

    private String text;

    private List<ZeusFile> children = new ArrayList<>();

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

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getHostGroupId() {
        return hostGroupId;
    }

    public void setHostGroupId(Integer hostGroupId) {
        this.hostGroupId = hostGroupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getText() {
        return this.getName();
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<ZeusFile> getChildren() {
        return children;
    }

    public void setChildren(List<ZeusFile> children) {
        this.children = children;
    }
}