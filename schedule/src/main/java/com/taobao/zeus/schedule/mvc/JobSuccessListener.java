package com.taobao.zeus.schedule.mvc;

import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.logic.JobHistoryManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.taobao.zeus.model.ActionDescriptor;
import com.taobao.zeus.model.ZeusActionHistory;
import com.taobao.zeus.model.JobStatus.TriggerType;
import com.taobao.zeus.mvc.DispatcherListener;
import com.taobao.zeus.mvc.MvcEvent;
import com.taobao.zeus.schedule.mvc.event.JobSuccessEvent;
import com.taobao.zeus.socket.master.MasterContext;
/**
 * 任务失败的监听
 * 当任务失败，需要发送邮件给相关人员
 * @author zhoufang
 *
 */
public class JobSuccessListener extends DispatcherListener{
	private static Logger log=LogManager.getLogger(JobSuccessListener.class);
	private GroupManagerWithAction groupManager;
	
	private JobHistoryManager jobHistoryManager;
	public JobSuccessListener(MasterContext context){
		groupManager=context.getGroupManagerWithAction();
		jobHistoryManager=context.getJobHistoryManager();
	}
	@Override
	public void beforeDispatch(MvcEvent mvce) {
		try {
			if(mvce.getAppEvent() instanceof JobSuccessEvent){
				final JobSuccessEvent event=(JobSuccessEvent) mvce.getAppEvent();
				if(event.getTriggerType()==TriggerType.SCHEDULE){
					return;
				}
				log.info("The event history id is " + event.getHistoryId());
				ZeusActionHistory history=jobHistoryManager.findJobHistory(event.getHistoryId());
				final ActionDescriptor jd=groupManager.getActionDescriptor(history.getActionId()).getX();
				if(history.getOperator()!=null){
					//此处可以发送IM消息
				}
			}
		} catch (Exception e) {
			//处理异常，防止后续的依赖任务受此影响，无法正常执行
			log.error("失败任务，发送通知出现异常",e);
		}
	}
}
