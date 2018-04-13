package com.taobao.zeus.dal.model;

public class ZeusGroupWithBLOBs extends ZeusGroup {
    private String configs;

    private String resources;

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
}