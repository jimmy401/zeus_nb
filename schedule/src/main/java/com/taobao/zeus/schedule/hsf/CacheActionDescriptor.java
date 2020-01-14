package com.taobao.zeus.schedule.hsf;

import java.util.Date;

import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.model.ActionDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.util.Tuple;

public class CacheActionDescriptor {
	private static Logger log=LoggerFactory.getLogger(CacheActionDescriptor.class);
	private GroupManagerWithAction groupManager;
	
	private final String actionId;
	private ActionDescriptor actionDescriptor;
	private Date lastTime=new Date();
	
	public CacheActionDescriptor(String actionId, GroupManagerWithAction groupManager){
		this.actionId =actionId;
		this.groupManager=groupManager;
	}
	

	public ActionDescriptor getActionDescriptor() {
		if(actionDescriptor ==null/* || System.currentTimeMillis()-lastTime.getTime()>60*1000L*/){
			try {
				Tuple<ActionDescriptor, JobStatus> job=groupManager.getActionDescriptor(actionId);
				if(job!=null){
					actionDescriptor =job.getX();
				}else{
					actionDescriptor =null;
				}
				/*lastTime=new Date();*/
			} catch (Exception e) {
				log.error("load job descriptor fail",e);
			}
		}
		return actionDescriptor;
	}
	
	public void refresh(){
		Tuple<ActionDescriptor, JobStatus> job=groupManager.getActionDescriptor(actionId);
		if(job!=null){
			actionDescriptor =job.getX();
		}else{
			actionDescriptor =null;
		}
	}
		
}
