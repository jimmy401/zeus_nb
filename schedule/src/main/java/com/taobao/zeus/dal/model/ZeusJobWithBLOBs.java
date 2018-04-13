package com.taobao.zeus.dal.model;

public class ZeusJobWithBLOBs extends ZeusJob {
    private String configs;

    private String resources;

    private String script;

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

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script == null ? null : script.trim();
    }
}