package com.taobao.zeus.web.platform.shared.rpc;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.taobao.zeus.web.platform.module.*;

/**
 *
 * @author zhoufang
 */
public interface JobService {
	/**
	 * 创建一个Job任务
	 * @param jobData
	 * @throws ServiceException
	 * @throws IOException
	 */
	JobModel createJob(String jobName, String parentGroupId, String jobType) throws Exception;
	
	JobModel getUpstreamJob(String jobId) throws Exception;
	
	JobModel updateJob(JobModel jobModel) throws Exception;
	/**
	 * 开关
	 * @param auto
	 * @throws Exception
	 */
	List<Long> switchAuto(String jobId,Boolean auto) throws Exception;
	/**
	 * 运行程序
	 * 1：手动运行
	 * 2:手动恢复
	 * @param jobId
	 * @param type
	 * @throws Exception
	 */
	void run(String jobId,int type) throws Exception;
	/**
	 * 取消一个正在执行的任务
	 * @param historyId
	 * @throws Exception
	 */
	void cancel(String historyId) throws Exception;
	/**
	 * 分页查询Job任务的历史日志
	 * @param config
	 * @return
	 */
	PagingLoadResult<JobHistoryModel> jobHistoryPaging(String jobId, PagingLoadConfig config);
	/**
	 * 获取Job任务的详细日志
	 * @param id
	 * @return
	 */
	JobHistoryModel getJobHistory(String id);
	/**
	 * 获取Job的运行状态
	 * @param jobId
	 * @return
	 */
	JobModel getJobStatus(String jobId);
	/**
	 * 获取组下的所有任务任务状态
	 * @param groupId
	 * @return
	 */
	PagingLoadResult<JobModelAction> getSubJobStatus(String groupId, PagingLoadConfig config, Date startDate, Date endDate);

	/**
	 * 获取组下正在运行的自动job
	 * @param groupId
	 * @return
	 */
//	@Deprecated
//	List<JobHistoryModel> getRunningJobs(String groupId);
	
	List<JobHistoryModel> getAutoRunning(String groupId);
	/**
	 * 获取正在运行的手动任务
	 * @param groupId
	 * @return
	 */
//	@Deprecated
//	List<JobHistoryModel> getManualRunningJobs(String groupId);
	
	List<JobHistoryModel> getManualRunning(String groupId);
	/**
	 * 删除Job任务
	 * @param jobId
	 * @return
	 * @throws Exception
	 */
	void deleteJob(String jobId) throws Exception;
	
	void addJobAdmin(String jobId,String uid)throws Exception;
	
	void removeJobAdmin(String jobId,String uid)throws Exception;
	
	List<ZUser> getJobAdmins(String jobId);
	
	void transferOwner(String jobId,String uid) throws Exception;
	/**
	 * 移动Job
	 * 将job移动到新的group下
	 * @param jobId
	 * @param newGroupId
	 * @throws Exception
	 */
	void move(String jobId,String newGroupId) throws Exception;
	/**
	 * 同步任务脚本
	 * 给开发中心使用，方便开发中心直接同步脚本到调度中心
	 * @param jobId
	 * @param script
	 * @throws Exception
	 */
	void syncScript(String jobId,String script) throws Exception;
	
	
	/**
	 * 获得该JOB ID下面的的所有ACTIONDI
	 * 给开发中心使用，方便开发中心直接同步脚本到调度中心
	 * @param jobId
	 * @param script
	 * @return 
	 * @throws Exception
	 */
	List<Long> getJobACtion(String id);
	
	void grantImportantContact(String jobId, String uid)  throws Exception;
	
	void revokeImportantContact(String jobId, String uid)  throws Exception;
	
	List<ZUserContactTuple> getAllContactList(String jobId);
	
	List<String> getJobDependencies(String jobId) throws Exception;
	
	PagingLoadResult<HostGroupModel> getHostGroup(PagingLoadConfig config);

	void syncScriptAndHostGroupId(String jobId, String script, String hostGroupId) throws Exception;
	
	String getHostGroupNameById(String hostGroupId);
}
