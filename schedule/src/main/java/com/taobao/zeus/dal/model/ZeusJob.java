package com.taobao.zeus.dal.model;

import org.quartz.CronExpression;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

public class ZeusJob  implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ScheduleType{
        SCHEDULE(0),DEPENDENT(1),CYCLE(2);

        private final int id;
        ScheduleType(int id){
            this.id=id;
        }
        @Override
        public String toString() {
            return String.valueOf(id);
        }
        public static ZeusJob.ScheduleType parser(int v){
            for(ZeusJob.ScheduleType s: ZeusJob.ScheduleType.values()){
                if(s.id==v){
                    return s;
                }
            }
            return null;
        }
        public int getValue() {
            return id;
        }
    }

    private Long id;

    /**
     * 是否开启调度 1:true 0:false
     */
    private Integer auto;

    private String cronExpression;
    /**
     * 周期任务的任务周期，
     * 比如:day,hour
     */
    private String cycle;

    private String dependencies;

    private String descr;

    private Date gmtCreate= new Date();

    private Date gmtModified= new Date();

    private Integer groupId;

    private Long historyId;

    private String host;

    private Date lastEndTime;

    private String lastResult;

    private String name;
    /**
     * 启动延时
     */
    private Integer offset;

    private String owner;

    private String postProcessers;

    private String preProcessers;

    private String readyDependency;
    /**
     * 运行的类型，比如Shell， Hive Mapreduce
     */
    private String runType;

    private Integer scheduleType;

    private Date startTime;

    private Long startTimestamp;

    private Date statisEndTime;

    private Date statisStartTime;

    private String status;

    private String timezone;

    private Integer hostGroupId;

    /**
     * 获取Cron表达式的执行周期
     * 现在暂不支持范围性任务的周期解析:* 10,20,30 12 * * ?
     *            cron表达式
     * @return 执行周期（秒）
     * @throws ParseException
     */
    public int getCronExcuteCycle() {
        int excute_cycle = 1000000000;
        if (cronExpression==null||cronExpression.trim().equalsIgnoreCase(""))
            return excute_cycle;
        try {
            CronExpression cExpression = new CronExpression(cronExpression);
            Date now_time = new Date();
            Date firstTime = cExpression.getNextValidTimeAfter(now_time);
            Date nextTime = cExpression.getNextValidTimeAfter(firstTime);
            excute_cycle = (int)((nextTime.getTime() - firstTime.getTime())/1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return excute_cycle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAuto() {
        return auto;
    }

    public void setAuto(Integer auto) {
        this.auto = auto;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression == null ? null : cronExpression.trim();
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle == null ? null : cycle.trim();
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies == null ? null : dependencies.trim();
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr == null ? null : descr.trim();
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

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    public Date getLastEndTime() {
        return lastEndTime;
    }

    public void setLastEndTime(Date lastEndTime) {
        this.lastEndTime = lastEndTime;
    }

    public String getLastResult() {
        return lastResult;
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult == null ? null : lastResult.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? null : owner.trim();
    }

    public String getPostProcessers() {
        return postProcessers;
    }

    public void setPostProcessers(String postProcessers) {
        this.postProcessers = postProcessers == null ? null : postProcessers.trim();
    }

    public String getPreProcessers() {
        return preProcessers;
    }

    public void setPreProcessers(String preProcessers) {
        this.preProcessers = preProcessers == null ? null : preProcessers.trim();
    }

    public String getReadyDependency() {
        return readyDependency;
    }

    public void setReadyDependency(String readyDependency) {
        this.readyDependency = readyDependency == null ? null : readyDependency.trim();
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType == null ? null : runType.trim();
    }

    public Integer getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(Integer scheduleType) {
        this.scheduleType = scheduleType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Date getStatisEndTime() {
        return statisEndTime;
    }

    public void setStatisEndTime(Date statisEndTime) {
        this.statisEndTime = statisEndTime;
    }

    public Date getStatisStartTime() {
        return statisStartTime;
    }

    public void setStatisStartTime(Date statisStartTime) {
        this.statisStartTime = statisStartTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone == null ? null : timezone.trim();
    }

    public Integer getHostGroupId() {
        return hostGroupId;
    }

    public void setHostGroupId(Integer hostGroupId) {
        this.hostGroupId = hostGroupId;
    }
}