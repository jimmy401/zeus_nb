package com.taobao.zeus.schedule.hsf;

import java.util.Date;

import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.util.Tuple;

public class CacheJobDescriptor {
	private static Logger log=LoggerFactory.getLogger(CacheJobDescriptor.class);
	private GroupManagerWithAction groupManager;
	
	private final String actionId;
	private  JobDescriptor jobDescriptor;
	private Date lastTime=new Date();
	
	public CacheJobDescriptor(String actionId,GroupManagerWithAction groupManager){
		this.actionId =actionId;
		this.groupManager=groupManager;
	}
	

	public JobDescriptor getJobDescriptor() {
		if(jobDescriptor==null/* || System.currentTimeMillis()-lastTime.getTime()>60*1000L*/){
			try {
				Tuple<JobDescriptor, JobStatus> job=groupManager.getActionDescriptor(actionId);
				if(job!=null){
					jobDescriptor=job.getX();
				}else{
					jobDescriptor=null;
				}
				/*lastTime=new Date();*/
			} catch (Exception e) {
				log.error("load job descriptor fail",e);
			}
		}
		return jobDescriptor;
	}
	
	public void refresh(){
		Tuple<JobDescriptor, JobStatus> job=groupManager.getActionDescriptor(actionId);
		if(job!=null){
			jobDescriptor=job.getX();
		}else{
			jobDescriptor=null;
		}
	}
		
}
