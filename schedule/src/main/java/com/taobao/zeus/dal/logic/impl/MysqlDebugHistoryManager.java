package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusDebugHistoryMapper;
import com.taobao.zeus.dal.model.ZeusDebugHistoryWithBLOBs;
import com.taobao.zeus.model.DebugHistory;
import com.taobao.zeus.model.JobStatus.Status;
import com.taobao.zeus.dal.logic.DebugHistoryManager;
import com.taobao.zeus.dal.tool.PersistenceAndBeanConvertWithAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository("mysqlDebugHistoryManager")
public class MysqlDebugHistoryManager implements DebugHistoryManager{

	@Autowired
	ZeusDebugHistoryMapper zeusDebugHistory;

	@Override
	public DebugHistory addDebugHistory(DebugHistory history) {
		ZeusDebugHistoryWithBLOBs persist= PersistenceAndBeanConvertWithAction.convert(history);
		zeusDebugHistory.insertSelective(persist);
		history.setId(String.valueOf(persist.getId()));
		return history;
	}

	public DebugHistory selectByParams(Map<String,Object> params){
		ZeusDebugHistoryWithBLOBs result = zeusDebugHistory.selectByParams(params);
		DebugHistory history= PersistenceAndBeanConvertWithAction.convert(result);
		return history;
	}

	@Override
	public DebugHistory findDebugHistory(String id) {
		ZeusDebugHistoryWithBLOBs persist= zeusDebugHistory.selectByPrimaryKey( Long.valueOf(id));
		return PersistenceAndBeanConvertWithAction.convert(persist);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DebugHistory> pagingList(final String fileId, final int start, final int limit) {
		Map<String,Object> params = new HashMap<String,Object>();
		int limitStart = start;
		int limitEnd = limit;
		params.put("fileId", fileId);
		params.put("limitEnd", limitEnd);
		params.put("limitStart", limitStart);

		List<ZeusDebugHistoryWithBLOBs> list= zeusDebugHistory.find(params);
		List<DebugHistory> result=new ArrayList<DebugHistory>();
		for(ZeusDebugHistoryWithBLOBs item:list){
			DebugHistory history=new DebugHistory();
			history.setId(item.getId().toString());
			history.setFileId(item.getFileId().toString());
			history.setStartTime(item.getStartTime());
			history.setEndTime(item.getEndTime());
			history.setExecuteHost(item.getExecuteHost());
			history.setStatus(item.getStatus()==null?null:Status.parser(item.getStatus().toString()));
			history.setScript(item.getScript());
			history.setLog(item.getLog());
			history.setOwner(item.getOwner());
			result.add(history);
		}
		return result;
	}

	@Override
	public int pagingTotal(String fileId) {
		int number= zeusDebugHistory.pagingTotal(Long.valueOf(fileId));
		return number;
	}

	@Override
	public void updateDebugHistory(DebugHistory history) {
		ZeusDebugHistoryWithBLOBs persist= PersistenceAndBeanConvertWithAction.convert(history);
		persist.setGmtModified(new Date());
		zeusDebugHistory.updateByPrimaryKeySelective(persist);
	}

	@Override
	public void updateDebugHistoryLog(final String id,final  String log) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", id);
		params.put("log", log);
		zeusDebugHistory.updateByPrimaryKeyWithBLOBs(params);
	}

}