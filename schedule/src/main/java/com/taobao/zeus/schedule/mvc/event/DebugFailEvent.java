package com.taobao.zeus.schedule.mvc.event;

import com.taobao.zeus.model.DebugHistory;
import com.taobao.zeus.model.JobStatus.TriggerType;
import com.taobao.zeus.mvc.AppEvent;
/**
 * Job失败触发的事件
 * @author zhoufang
 *
 */
public class DebugFailEvent extends AppEvent{

	private final DebugHistory history;
	private final Long fileId;
	private final Throwable throwable;
	public DebugFailEvent(Long jobId) {
		this(jobId,null,null);
	}
	
	public DebugFailEvent(Long fileId,DebugHistory history,Throwable t){
		super(Events.JobFailed);
		this.fileId=fileId;
		this.history=history;
		this.throwable=t;
	}
	
	public Long getFileId() {
		return fileId;
	}

	public DebugHistory getHistory() {
		return history;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	
}
