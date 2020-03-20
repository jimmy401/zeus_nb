package com.taobao.zeus.model;

import com.taobao.zeus.model.processer.Processer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobDescriptor implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<String, String> properties = new HashMap<String, String>();
	private String cronExpression;
	private List<String> dependencies = new ArrayList<String>();
	private String id;
	private String name;
	private String desc;
	private String groupId;
	private String owner;
	private Boolean auto = false;
	private List<FileResource> resources = new ArrayList<>();

	private JobRunType jobRunType;
	private JobScheduleType jobScheduleType;

	private String script;

	private String timezone;

	private List<Processer> preProcessers = new ArrayList<Processer>();
	private List<Processer> postProcessers = new ArrayList<Processer>();
	private String offRaw="0";
	private String cycle;
	// 周期任务的依赖，key为任务ID，value为任务周期
	private Map<String, String> depdCycleJob = new HashMap<String, String>();

	private long startTimestamp;

	private String startTime;

	private String statisStartTime;

	private String statisEndTime;
	
	private String host;
	
	private String hostGroupId;

	public JobDescriptor getCopy() {
		JobDescriptor actionDescriptor = new JobDescriptor();
		actionDescriptor.setAuto(this.getAuto());
		actionDescriptor.setCronExpression(this.getCronExpression());
		actionDescriptor.setCycle(this.getCycle());
		actionDescriptor.setDepdCycleJob(this.getDepdCycleJob());
		actionDescriptor.setDependencies(this.getDependencies());
		actionDescriptor.setDesc(this.getDesc());
		actionDescriptor.setGroupId(this.getGroupId());
		actionDescriptor.setId(this.getId());
		actionDescriptor.setJobType(this.getJobType());
		actionDescriptor.setName(this.getName());
		actionDescriptor.setOffRaw(this.getOffRaw());
		actionDescriptor.setOwner(this.getOwner());
		actionDescriptor.setPostProcessers(this.getPostProcessers());
		actionDescriptor.setPreProcessers(this.getPreProcessers());
		actionDescriptor.setProperties(this.getProperties());
		actionDescriptor.setResources(this.getResources());
		actionDescriptor.setScheduleType(this.getScheduleType());
		actionDescriptor.setScript(this.getScript());
		actionDescriptor.setStartTime(this.getStartTime());
		actionDescriptor.setStartTimestamp(this.getStartTimestamp());
		actionDescriptor.setStatisEndTime(this.getStatisEndTime());
		actionDescriptor.setStatisStartTime(this.getStatisStartTime());
		actionDescriptor.setTimezone(this.getTimezone());
		actionDescriptor.setHostGroupId(this.getHostGroupId());
		return actionDescriptor;
	}

	public List<FileResource> getResources() {
		return resources;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public String getDesc() {
		return desc;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getId() {
		return id;
	}

	public JobRunType getJobType() {
		return jobRunType;
	}

	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public JobScheduleType getScheduleType() {
		return jobScheduleType;
	}

	public boolean hasDependencies() {
		return !dependencies.isEmpty();
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public void setDependencies(List<String> depends) {
		this.dependencies = depends;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setJobType(JobRunType type) {
		this.jobRunType = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setScheduleType(JobScheduleType type) {
		this.jobScheduleType = type;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setResources(List<FileResource> resources) {
		this.resources = resources;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Boolean getAuto() {
		return auto;
	}

	public void setAuto(Boolean auto) {
		this.auto = auto;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public enum JobRunType {
		MapReduce("main"), Shell("shell"), Hive("hive");
		private final String id;

		JobRunType(String s) {
			this.id = s;
		}

		@Override
		public String toString() {
			return id;
		}

		public static JobRunType parser(String v) {
			for (JobRunType type : JobRunType.values()) {
				if (type.toString().equals(v)) {
					return type;
				}
			}
			return null;
		}
	}

	public enum JobScheduleType {
		Independent(0), Dependent(1), CyleJob(2);
		private Integer type;

		private JobScheduleType(Integer type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type.toString();
		}

		public static JobScheduleType parser(String value) {
			if ("0".equals(value)) {
				return Independent;
			}
			if ("1".equals(value)) {
				return Dependent;
			}
			if ("2".equals(value)) {
				return CyleJob;
			}
			return null;
		}

		public static JobScheduleType parser(Integer v) {
			for (JobScheduleType t : JobScheduleType.values()) {
				if (t.getType().equals(v)) {
					return t;
				}
			}
			return null;
		}

		public Integer getType() {
			return type;
		}
	}

	public List<Processer> getPreProcessers() {
		return preProcessers;
	}

	public void setPreProcessers(List<Processer> preProcessers) {
		this.preProcessers = preProcessers;
	}

	public List<Processer> getPostProcessers() {
		return postProcessers;
	}

	public void setPostProcessers(List<Processer> postProcessers) {
		this.postProcessers = postProcessers;
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

	public String getOffRaw() {
		return offRaw;
	}

	public void setOffRaw(String offRaw) {
		this.offRaw = offRaw;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getStatisStartTime() {
		return statisStartTime;
	}

	public void setStatisStartTime(String statisStartTime) {
		this.statisStartTime = statisStartTime;
	}

	public String getStatisEndTime() {
		return statisEndTime;
	}

	public void setStatisEndTime(String statisEndTime) {
		this.statisEndTime = statisEndTime;
	}

	public Map<String, String> getDepdCycleJob() {
		return depdCycleJob;
	}

	public void setDepdCycleJob(Map<String, String> depdCycleJob) {
		this.depdCycleJob = depdCycleJob;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHostGroupId() {
		return hostGroupId;
	}

	public void setHostGroupId(String hostGroupId) {
		this.hostGroupId = hostGroupId;
	}
}
