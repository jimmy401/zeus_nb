package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusActionHistoryMapper;
import com.taobao.zeus.dal.model.BigIdWithAction;
import com.taobao.zeus.dal.tool.PersistenceAndBeanConvertWithAction;
import com.taobao.zeus.model.ZeusActionHistory;
import com.taobao.zeus.dal.logic.JobHistoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@SuppressWarnings("unchecked")
@Repository("mysqlJobHistoryManager")
public class MysqlJobHistoryManager implements JobHistoryManager{

	@Autowired
	ZeusActionHistoryMapper zeusActionHistoryMapper;

	@Override
	public void updateJobHistoryLog(final String id, final String log) {
		com.taobao.zeus.dal.model.ZeusActionHistory zeusActionHistory = new com.taobao.zeus.dal.model.ZeusActionHistory();
		zeusActionHistory.setId(Long.valueOf(id));
		zeusActionHistory.setLog(log);
		zeusActionHistoryMapper.updateByPrimaryKeySelective(zeusActionHistory);
	}

	@Override
	public void updateJobHistory(ZeusActionHistory history) {
		com.taobao.zeus.dal.model.ZeusActionHistory zeusActionHistory = zeusActionHistoryMapper.selectByPrimaryKey(Long.valueOf(history.getId()));

		com.taobao.zeus.dal.model.ZeusActionHistory persist= PersistenceAndBeanConvertWithAction.convert(history);
		persist.setGmtModified(new Date());
		persist.setGmtCreate(zeusActionHistory.getGmtCreate());
		persist.setLog(zeusActionHistory.getLog());
		zeusActionHistoryMapper.updateByPrimaryKeySelective(persist);
	}

	@Override
	public ZeusActionHistory addJobHistory(ZeusActionHistory history) {
		Date now = new Date();
		com.taobao.zeus.dal.model.ZeusActionHistory persist= PersistenceAndBeanConvertWithAction.convert(history);
		persist.setGmtCreate(now);
		persist.setGmtModified(now);
		zeusActionHistoryMapper.insertSelective(persist);
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("actionId", persist.getActionId());
		params.put("jobId", persist.getJobId());
		List<com.taobao.zeus.dal.model.ZeusActionHistory> result =zeusActionHistoryMapper.selectByParams(params);
		Long id=result.get(0).getId();
		history.setId(id.toString());
		return history;
	}
	
	@Override
	public List<ZeusActionHistory> pagingList(final String jobId, final int start, final int limit) {
		Map<String,Object> params = new HashMap<String,Object>();
		int limitStart = start;
		int limitEnd = limit;
		params.put("jobId", Long.valueOf(jobId));
		//params.put("actionId", Long.valueOf(jobId));
		params.put("limitEnd", limitEnd);
		params.put("limitStart", limitStart);
 		List<com.taobao.zeus.dal.model.ZeusActionHistory> list= zeusActionHistoryMapper.find(params);
		List<ZeusActionHistory> result=new ArrayList<ZeusActionHistory>();
		for(com.taobao.zeus.dal.model.ZeusActionHistory o:list){
			result.add(PersistenceAndBeanConvertWithAction.convert(o));
		}
		return result;
	}

	@Override
	public int pagingTotal(String jobId) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("jobId", Long.valueOf(jobId));
		int result = zeusActionHistoryMapper.countByJobId(params);
		return result;
	}

	@Override
	public ZeusActionHistory findJobHistory(String id) {
		com.taobao.zeus.dal.model.ZeusActionHistory persist = zeusActionHistoryMapper.selectByPrimaryKey(Long.valueOf(id));
		return PersistenceAndBeanConvertWithAction.convert(persist);
	}
 
	@Override
	public Map<String, ZeusActionHistory> findLastHistoryByList(final List<String> jobIds) {
		if(jobIds==null || jobIds.isEmpty()){
			return Collections.emptyMap();
		}
		List<BigIdWithAction> recordList = zeusActionHistoryMapper.findIdsWithActionId(jobIds);

		List<Long> ids=new ArrayList<Long>();
		if(recordList!=null && recordList.size()>0){
			for(BigIdWithAction o:recordList){
				ids.add(o.getId());
			}
		}

		if (ids.isEmpty())
		{
			return Collections.emptyMap();
		}
		List<com.taobao.zeus.dal.model.ZeusActionHistory> zeusActionHistories = zeusActionHistoryMapper.selectByBigIds(ids);
		List<ZeusActionHistory> result=new ArrayList<ZeusActionHistory>();
		if (zeusActionHistories!=null && zeusActionHistories.size()>0){
			for(com.taobao.zeus.dal.model.ZeusActionHistory o:zeusActionHistories){
				result.add(PersistenceAndBeanConvertWithAction.convert(o));
			}
		}
		Map<String, ZeusActionHistory> map=new HashMap<String, ZeusActionHistory>();
		for(ZeusActionHistory p:result){
			map.put(p.getActionId(),p);
		}
		return map;
	}

	@Override
	public List<ZeusActionHistory> findRecentRunningHistory() {
		Map<String,Object> params = new HashMap<String,Object>();
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		params.put("startTime",cal.getTime());
		List<com.taobao.zeus.dal.model.ZeusActionHistory> zeusActionHistories = zeusActionHistoryMapper.selectRecent(params);

		List<ZeusActionHistory> result=new ArrayList<ZeusActionHistory>();
		for(com.taobao.zeus.dal.model.ZeusActionHistory o:zeusActionHistories){
			result.add(PersistenceAndBeanConvertWithAction.convert(o));
		}
		return result;
	}

}
