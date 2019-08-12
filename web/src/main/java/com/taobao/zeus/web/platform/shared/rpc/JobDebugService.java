package com.taobao.zeus.web.platform.shared.rpc;

import com.taobao.zeus.web.platform.module.DebugHistoryModel;
import com.taobao.zeus.web.platform.module.PagingLoadConfig;
import com.taobao.zeus.web.platform.module.PagingLoadResult;

public interface JobDebugService{

	String debug(String fileId,String mode,String content, String hostGroupId)throws Exception;
	
	
	String getLog(String debugId);
	
	String getStatus(String debugId);
	
	PagingLoadResult<DebugHistoryModel> getDebugHistory(PagingLoadConfig loadConfigString, String fileId);
	
	public void cancelDebug(String debugId) throws Exception;
	
	DebugHistoryModel getHistoryModel(String debugId);
}
