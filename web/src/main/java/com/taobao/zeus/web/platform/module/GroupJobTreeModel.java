package com.taobao.zeus.web.platform.module;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupJobTreeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String id;
    private boolean directory;
    private boolean group;
    private boolean job;
    private boolean follow;
    private String owner;
    private Integer scheduleType;
    private List<GroupJobTreeModel> children=new ArrayList<GroupJobTreeModel>();

    private String text;
    private String state;
    public String getText() {
        return name+"(" + id+")";
    }

    public String getState() {
        return "closed";
    }

    @JSONField(name = "attributes")
    private HashMap<String, Object> attributes = new HashMap<String, Object>();

    public HashMap<String, Object> getAttributes() {
        HashMap<String, Object> ret =new HashMap<String, Object>();
        ret.put("id", this.id);
        ret.put("owner", this.owner);
        ret.put("group", this.group);
        ret.put("directory", this.directory);
        ret.put("job", this.job);
        ret.put("follow", this.follow);
        ret.put("scheduleType",this.scheduleType);
        return ret;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public boolean isGroup() {
        return group;
    }
    public void setGroup(boolean group) {
        this.group = group;
    }
    public boolean isDirectory() {
        return directory;
    }
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
    public boolean isJob() {
        return job;
    }
    public void setJob(boolean job) {
        this.job = job;
    }
    public boolean isFollow() {
        return follow;
    }
    public void setFollow(boolean follow) {
        this.follow = follow;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public List<GroupJobTreeModel> getChildren() {
        return children;
    }
    public void setChildren(List<GroupJobTreeModel> children) {
        this.children = children;
    }

    public Integer getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(Integer scheduleType) {
        this.scheduleType = scheduleType;
    }
}
