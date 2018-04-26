package com.taobao.zeus.dal.model;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ZeusJobWithBLOBs extends ZeusJob {
    private String configs;

    private String resources;

    private String script;

    private Integer rollBackTimes;

    private Integer rollBackWaitTime;

    private Integer runPriorityLevel;

    private boolean zeusSecretScript;

    public Integer getRollBackTimes() {
        JSONObject jsonObject = JSON.parseObject(getConfigs());
        return jsonObject.getInteger("roll.back.times");
    }

    public Integer getRollBackWaitTime() {
        JSONObject jsonObject = JSON.parseObject(getConfigs());
        return jsonObject.getInteger("roll.back.wait.time");
    }

    public Integer getRunPriorityLevel() {
        JSONObject jsonObject = JSON.parseObject(getConfigs());
        return jsonObject.getInteger("run.priority.level");
    }

    public boolean getZeusSecretScript() {
        JSONObject jsonObject = JSON.parseObject(getConfigs());
        return jsonObject.getBooleanValue("zeus.secret.script");
    }

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