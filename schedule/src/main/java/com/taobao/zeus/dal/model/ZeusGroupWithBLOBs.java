package com.taobao.zeus.dal.model;

import com.alibaba.fastjson.JSON;
import com.taobao.zeus.model.FileResource;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZeusGroupWithBLOBs extends ZeusGroup {
    private String configs;

    private String resources;

    private Boolean bDirectory;

    private Boolean bExisted;

    private Map<String, String> properties=new HashMap<String, String>();

    private List<FileResource> fileResources =new ArrayList<>();

    public String getConfigs() {
        return configs;
    }

    public void setConfigs(String configs) {
        this.configs = configs == null ? null : configs.trim();
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources == null ? null : resources.trim();
    }

    public Boolean getbDirectory() {
        return getDirectory() == 0 ? true : false;
    }

    public Boolean isBigGroup(){
        return getDirectory() == 0 ? true : false;
    }

    public Boolean isSmallGroup(){
        return getDirectory() == 1 ? true : false;
    }

    public void setbDirectory(Boolean bDirectory) {
        this.bDirectory = bDirectory;
    }

    public Boolean getbExisted() {
        return getExisted() == 0 ? false : true;
    }

    public void setbExisted(Boolean bExisted) {
        this.bExisted = bExisted;
    }

    public Map<String, String> getProperties() {
        if (getConfigs() != null) {
            JSONObject object = JSONObject.fromObject(getConfigs());
            properties = new HashMap<String, String>();
            for (Object key : object.keySet()) {
                properties.put(key.toString(),object.getString(key.toString()));
            }
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<FileResource> getFileResources() {
        return JSON.parseArray(getResources(),FileResource.class);
    }

    public void setFileResources(List<FileResource> fileResources) {
        this.fileResources = fileResources;
    }
}