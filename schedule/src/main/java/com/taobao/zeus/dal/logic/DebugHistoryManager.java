package com.taobao.zeus.dal.logic;

import com.taobao.zeus.model.DebugHistory;

import java.util.List;
import java.util.Map;

public interface DebugHistoryManager {

	public DebugHistory addDebugHistory(DebugHistory history);

	public DebugHistory selectByParams(Map<String,Object> params);

	DebugHistory findDebugHistory(String id);
	/**
	 * 分页查询Job历史记录，注意返回结果中不包含 日志内容 字段
	 * @param jobId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<DebugHistory> pagingList(final String fileId, final int start, final int limit);
	public int pagingTotal(String jobId);

	public void updateDebugHistoryLog(String id, String log);
	/**
	 * 更新JobLogHistory，但是不包括log字段
	 * @param history
	 */
	public void updateDebugHistory(DebugHistory history);
	
}
