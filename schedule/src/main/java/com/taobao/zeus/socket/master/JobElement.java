package com.taobao.zeus.socket.master;

public class JobElement {
	private String actionId;
//	private String host;
	private int priorityLevel;
	private String hostGroupId;

	/**
	 * @return
	 */
	public JobElement() {

	}

	public JobElement(String actionId, String hostGroupId) {
		this.actionId = actionId;
		this.hostGroupId = hostGroupId;
	}
	
//	public JobElement(String actionId, String host, int priorityLevel) {
//		this.actionId = actionId;
//		this.host = host;
//		this.priorityLevel = priorityLevel;
//	}
	
	public JobElement(String actionId, String hostGroupId, int priorityLevel) {
		this.actionId = actionId;
		this.hostGroupId = hostGroupId;
		this.priorityLevel = priorityLevel;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

//	public String getHost() {
//		return host;
//	}
//
//	public void setHost(String host) {
//		this.host = host;
//	}

	public int getPriorityLevel() {
		return priorityLevel;
	}

	public void setPriorityLevel(int priorityLevel) {
		this.priorityLevel = priorityLevel;
	}

	@Override
	public String toString() {
		return actionId + ":" + hostGroupId;
	}

	public String getHostGroupId() {
		return hostGroupId;
	}

	public void setHostGroupId(String hostGroupId) {
		this.hostGroupId = hostGroupId;
	}
}
