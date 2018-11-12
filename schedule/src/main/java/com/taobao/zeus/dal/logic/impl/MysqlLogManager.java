package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusLogMapper;
import com.taobao.zeus.dal.model.ZeusLog;
import com.taobao.zeus.model.LogDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@SuppressWarnings("unchecked")
@Repository("mysqlLogManager")
public class MysqlLogManager {

	@Autowired
	ZeusLogMapper zeusLogMapper;

	public void addLog(LogDescriptor logDescriptor) {
		try {
			ZeusLog logPersistence = new ZeusLog();
//			logPersistence.setId(11L);
			logPersistence.setLogtype(logDescriptor.getLogType());
			logPersistence.setUsername(logDescriptor.getUserName());
			logPersistence.setIp(logDescriptor.getIp());
			logPersistence.setUrl(logDescriptor.getUrl());
			logPersistence.setRpc(logDescriptor.getRpc());
			logPersistence.setDelegate(logDescriptor.getDelegate());
			logPersistence.setMethod(logDescriptor.getMethod());
			logPersistence.setDescription(logDescriptor.getDescription());
			logPersistence.setStatus(logDescriptor.getStatus());
			logPersistence.setOldScript(logDescriptor.getOldScript());
			logPersistence.setNewScript(logDescriptor.getNewScript());
			logPersistence.setCreatetime(logDescriptor.getCreateTime());

			zeusLogMapper.insertSelective(logPersistence);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ZeusLog selectLogByActionId(String actionId) {
		ZeusLog zeusLog = null;
		try {
			zeusLog = zeusLogMapper.selectByActionId(actionId);
			return zeusLog;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return zeusLog;
	}

	public List<ZeusLog> selectLogByLogType(String logType) {
		List<ZeusLog> zeusLog = null;
		try {
			zeusLog = zeusLogMapper.selectByLogType(logType);
			return zeusLog;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return zeusLog;
	}

	public void updateByPrimaryKeySelective(ZeusLog item) {
		try {
			zeusLogMapper.updateByPrimaryKeySelective(item);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
