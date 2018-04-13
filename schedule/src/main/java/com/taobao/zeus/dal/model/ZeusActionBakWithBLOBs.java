package com.taobao.zeus.dal.model;

import java.util.Date;

public class ZeusActionBakWithBLOBs extends ZeusActionBak {
    private String configs;

    private String resources;

    public ZeusActionBakWithBLOBs() {
    }
    public ZeusActionBakWithBLOBs(ZeusActionWithBLOBs persist) {
        this.setId(persist.getId());
        this.setJobId(persist.getJobId());
        this.setAuto(persist.getAuto());
        this.setScheduleType(persist.getScheduleType());
        this.setRunType(persist.getRunType());
        this.setConfigs(persist.getConfigs());
        this.setCronExpression(persist.getCronExpression());
        this.setDependencies(persist.getDependencies());
        this.setJobDependencies( persist.getJobDependencies());
        this.setName(persist.getName());
        this.setDescr(persist.getDescr());
        this.setGroupId(persist.getGroupId());
        this.setOwner(persist.getOwner());
        this.setResources(persist.getResources());
/*		this.script = persist.getScript();*/
        this.setGmtCreate(persist.getGmtCreate());
        this.setGmtModified(persist.getGmtModified());
        this.setHistoryId(persist.getHistoryId());
        this.setStatus(persist.getStatus());
        this.setReadyDependency(persist.getReadyDependency());
        this.setPreProcessers(persist.getPreProcessers());
        this.setPostProcessers(persist.getPostProcessers());
        this.setTimezone(persist.getTimezone());
        this.setStartTime(persist.getStartTime());
        this.setStartTimestamp(persist.getStartTimestamp());
        this.setOffset( persist.getOffset());
        this.setLastEndTime(persist.getLastEndTime());
        this.setLastResult( persist.getLastResult());
        this.setStatisStartTime( persist.getStatisStartTime());
        this.setStatisEndTime( persist.getStatisEndTime());
        this.setCycle(persist.getCycle());
        this.setHost(persist.getHost());
        this.setStoreTime(new Date());
        this.setHostGroupId(persist.getHostGroupId());
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
}