package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusActionHistoryMapper;
import com.taobao.zeus.dal.model.ZeusActionReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MysqlReportManager{

	@Autowired
	ZeusActionHistoryMapper zeusActionHistoryMapper;
	/**
	 * yyyyMMdd->{success:1,fail:2},{success:1,fail:2}
	 * @param start
	 * @param end
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> runningJobs(final Date start,final Date end){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("startDate", start);
		params.put("endDate", end);
		Map<String, Map<String, String>> result=new HashMap<String, Map<String,String>>();
		List<ZeusActionReport> success_list = zeusActionHistoryMapper.selectSuccessReportByDate(params);

		for(ZeusActionReport o:success_list){
			Map<String, String> map=new HashMap<String, String>();
			map.put("success", String.valueOf(o.getActionCnt()));
			result.put(o.getGmtCreate(), map);
		}

		List<ZeusActionReport> failed_list = zeusActionHistoryMapper.selectFailedReportByDate(params);
		for(ZeusActionReport o:failed_list){
			Map<String, String> map=result.get(o.getGmtCreate());
			if(map==null){
				map=new HashMap<String, String>();
				result.put(o.getGmtCreate(), map);
			}
			map.put("fail", String.valueOf(o.getActionCnt()));
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> ownerFailJobs(final Date date){
		List<Map<String, String>> result=new ArrayList<Map<String,String>>();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("startDate", date);
		List<ZeusActionReport> success_list = zeusActionHistoryMapper.selectOwnerFailedReportByDate(params);
		for(ZeusActionReport o:success_list){
			Map<String, String> map=new HashMap<String, String>();
			map.put("count", String.valueOf(o.getActionCnt()));
			map.put("uid", o.getOwner());
			map.put("uname", o.getName());
			result.add(map);
		}

		for(final Map<String, String> map:result){
			Map<String,Object> fparams = new HashMap<String,Object>();
			params.put("startDate", date);
			params.put("owner", map.get("uid"));
			List<ZeusActionReport> action_list = zeusActionHistoryMapper.selectFailedActionByDate(fparams);

			int count=0;
			for(ZeusActionReport rs:action_list){
				String jobID = String.valueOf(rs.getActionId());
				String jobName = rs.getName();
				// 去重
				if(!map.containsKey(jobID)){
					map.put("history"+count++, jobName+"("+jobID+")");
				}
				map.put(jobID, null);
			}
			map.put("count", count+"");
		}
		return result;
	}
}
