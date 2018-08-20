package com.taobao.zeus.model;

import java.io.Serializable;
import java.util.Date;

public class LogDescriptor implements Serializable{

	private static final long serialVersionUID = 1L;
	private Long id;
	private String logType;
	private Date createTime;
	private String userName;
	private String ip;
	private String url;
	private String rpc;
	private String delegate;
	private String method;
	private String description;
	private int status;
	private String checkUid;
	private String checkTime;
	private String oldScript;
	private String newScript;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCheckUid() {
		return checkUid;
	}

	public void setCheckUid(String checkUid) {
		this.checkUid = checkUid;
	}

	public String getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}

	public String getOldScript() {
		return oldScript;
	}

	public void setOldScript(String oldScript) {
		this.oldScript = oldScript;
	}

	public String getNewScript() {
		return newScript;
	}

	public void setNewScript(String newScript) {
		this.newScript = newScript;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRpc() {
		return rpc;
	}

	public void setRpc(String rpc) {
		this.rpc = rpc;
	}

	public String getDelegate() {
		return delegate;
	}

	public void setDelegate(String delegate) {
		this.delegate = delegate;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
