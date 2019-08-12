package com.taobao.zeus.web.platform.server.rpc;

import com.taobao.zeus.model.JobStatus.TriggerType;
import com.taobao.zeus.util.Environment;
import com.taobao.zeus.web.platform.module.*;
import com.taobao.zeus.web.platform.shared.rpc.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("job.rpc")
public class FilterJobServiceImpl implements JobService{

    @Autowired
	@Qualifier("jobServiceImpl")
	private JobService jobService;
	@Override
	public JobModel createJob(String jobName, String parentGroupId,
							  String jobType) throws Exception {
		if(Environment.isPrePub()){
			throw new Exception("预发环境无法创建Job");
		}
		return jobService.createJob(jobName, parentGroupId, jobType);
	}

	@Override
	public void deleteJob(String jobId) throws Exception {
		if(Environment.isPrePub()){
			throw new Exception("预发环境无法删除Job");
		}
		jobService.deleteJob(jobId);
	}

	@Override
	public JobHistoryModel getJobHistory(String id) {
		return jobService.getJobHistory(id);
	}

	@Override
	public JobModel getJobStatus(String jobId) {
		return jobService.getJobStatus(jobId);
	}

	@Override
	public JobModel getUpstreamJob(String jobId) throws Exception {
		return jobService.getUpstreamJob(jobId);
	}

	@Override
	public PagingLoadResult<JobHistoryModel> jobHistoryPaging(String jobId, PagingLoadConfig config) {
		return jobService.jobHistoryPaging(jobId,config);
	}

	@Override
	public void run(String jobId, int type) throws Exception {
		TriggerType triggerType=null;
		if(type==1){
			triggerType=TriggerType.MANUAL;
		}else if(type==2){
			triggerType=TriggerType.MANUAL_RECOVER;
		}
		if(Environment.isPrePub() && triggerType==TriggerType.MANUAL_RECOVER){
			throw new Exception("预发环境无法 执行 手动恢复操作");
		}
		jobService.run(jobId, type);
	}

	@Override
	public List<Long> switchAuto(String jobId, Boolean auto) throws Exception {
		if(Environment.isPrePub()){
			throw new Exception("预发环境无法修改状态");
		}
		return jobService.switchAuto(jobId, auto);
	}

	@Override
	public JobModel updateJob(JobModel jobModel) throws Exception {
		if(Environment.isPrePub()){
			throw new Exception("预发环境无法更新Job");
		}
		return jobService.updateJob(jobModel);
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	@Override
	public void addJobAdmin(String jobId, String uid) throws Exception {
		jobService.addJobAdmin(jobId, uid);
	}

	@Override
	public List<ZUser> getJobAdmins(String jobId) {
		return jobService.getJobAdmins(jobId);
	}
	@Override
	public List<Long> getJobACtion(String jobId) {
		return jobService.getJobACtion(jobId);
	}
	@Override
	public void removeJobAdmin(String jobId, String uid) throws Exception {
		jobService.removeJobAdmin(jobId, uid);
	}

	@Override
	public void transferOwner(String jobId, String uid) throws Exception {
		jobService.transferOwner(jobId, uid);
	}

//	@Override
//	public List<JobHistoryModel> getRunningJobs(String groupId) {
//		return jobService.getRunningJobs(groupId);
//	}

	@Override
	public void cancel(String jobId) throws Exception {
		jobService.cancel(jobId);
	}

//	@Override
//	public List<JobHistoryModel> getManualRunningJobs(String groupId) {
//		return jobService.getManualRunningJobs(groupId);
//	}

	@Override
	public List<JobHistoryModel> getAutoRunning(String groupId) {
		return jobService.getAutoRunning(groupId);
	}

	@Override
	public List<JobHistoryModel> getManualRunning(String groupId) {
		return jobService.getManualRunning(groupId);
	}

	@Override
	public void move(String jobId, String newGroupId) throws Exception {
		jobService.move(jobId, newGroupId);
	}

	@Override
	public void syncScript(String jobId, String script) throws Exception {
		jobService.syncScript(jobId, script);
	}

	@Override
	public PagingLoadResult<JobModelAction> getSubJobStatus(String groupId,
															PagingLoadConfig config, Date startDate, Date endDate) {
		return jobService.getSubJobStatus(groupId,config,startDate,endDate);
	}

	@Override
	public void grantImportantContact(String jobId, String uid)
			throws Exception {
		jobService.grantImportantContact(jobId, uid);
		
	}

	@Override
	public void revokeImportantContact(String jobId, String uid)
			throws Exception {
		jobService.revokeImportantContact(jobId, uid);
		
	}

	@Override
	public List<ZUserContactTuple> getAllContactList(String jobId) {
		return jobService.getAllContactList(jobId);
	}

	@Override
	public List<String> getJobDependencies(String jobId) throws Exception {
		return jobService.getJobDependencies(jobId);
	}

	@Override
	public PagingLoadResult<HostGroupModel> getHostGroup(
			PagingLoadConfig config) {
		return jobService.getHostGroup(config);
	}

	@Override
	public void syncScriptAndHostGroupId(String jobId, String script,
			String hostGroupId) throws Exception {
		jobService.syncScriptAndHostGroupId(jobId, script, hostGroupId);
	}

	@Override
	public String getHostGroupNameById(String hostGroupId) {
		return jobService.getHostGroupNameById(hostGroupId);
	}
}