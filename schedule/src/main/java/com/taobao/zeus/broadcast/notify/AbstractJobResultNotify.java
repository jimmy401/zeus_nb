package com.taobao.zeus.broadcast.notify;

import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.logic.JobHistoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractJobResultNotify implements JobResultNotify{
	@Autowired
	protected JobHistoryManager jobHistoryManager;
	@Autowired
	@Qualifier("mysqlGroupManagerWithAction")
	protected GroupManagerWithAction groupManager;

}