package com.taobao.zeus.client.crud;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.model.ActionDescriptor;

public interface ZeusJobService {

	public ActionDescriptor createJob(String uid, String jobName, String parentGroup, ActionDescriptor.JobRunType jobType) throws ZeusException;
	
	public void updateJob(String uid,ActionDescriptor actionDescriptor) throws ZeusException;
	
	public void deleteJob(String uid,String jobId) throws ZeusException;
	
	public ActionDescriptor getJobDescriptor(String jobId);
	
}
