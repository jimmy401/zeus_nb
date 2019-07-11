package com.taobao.zeus.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.taobao.zeus.model.JobStatus.Status;
import com.taobao.zeus.model.JobStatus.TriggerType;
import com.taobao.zeus.util.DateUtil;


public class JobHistory {

    private String id;
    private String actionId;
    private String jobId;
    private Date startTime;
    private Date endTime;
    private String executeHost;
    private String operator;
    private Status status;
    private TriggerType triggerType;
    private String illustrate;
    private String statisEndTime;
    private LogContent log = new LogContent();
    private String timezone;
    private String cycle;
    private String hostGroupId;
    private String script;
    private String instanceAction;

    private Map<String, String> properties = new HashMap<String, String>();

    private String startTimeStr;

    private String endTimeStr;

    private String triggerTypeStr;

    public String getStartTimeStr() {
        if (this.getStartTime() != null) {
            return DateUtil.date2String(this.getStartTime(), "yyyy-MM-dd HH:mm:ss");
        } else {
            return "";
        }
    }

    public String getEndTimeStr() {
        if (this.getStartTime() != null){
            return DateUtil.date2String(this.getEndTime(), "yyyy-MM-dd HH:mm:ss");
        }
        else{
            return "";
        }
    }

    public String getTriggerTypeStr() {
        if (this.getTriggerType() != null) {
            if (this.getTriggerType() == JobStatus.TriggerType.MANUAL) {
                return "手动调度";
            } else if (this.getTriggerType() == JobStatus.TriggerType.MANUAL_RECOVER) {
                return "手动恢复";
            } else if (this.getTriggerType() == JobStatus.TriggerType.SCHEDULE) {
                return "自动调度";
            }
        }

        return "";
    }

    public String getInstanceAction() {
        return "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public LogContent getLog() {
        return log;
    }

    public void setLog(String log) {
        log = log == null ? "" : log;
        this.log.setContent(new StringBuffer(log));
    }

    public String getExecuteHost() {
        return executeHost;
    }

    public void setExecuteHost(String executeHost) {
        this.executeHost = executeHost;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getIllustrate() {
        return illustrate;
    }

    public void setIllustrate(String illustrate) {
        this.illustrate = illustrate;
    }

    @Override
    public String toString() {
        return "id:" + id + ",actionId:" + actionId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getStatisEndTime() {
        return statisEndTime;
    }

    public void setStatisEndTime(String statisEndTime) {
        this.statisEndTime = statisEndTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getHostGroupId() {
        return hostGroupId;
    }

    public void setHostGroupId(String hostGroupId) {
        this.hostGroupId = hostGroupId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
