package com.taobao.zeus.dal.logic;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusGroupWithBLOBs;
import com.taobao.zeus.dal.model.ZeusWorker;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.model.ActionDescriptor;
import com.taobao.zeus.model.ActionDescriptor.JobRunType;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.util.Tuple;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface GroupManagerWithAction {
	/**
	 * 获取根节点的组ID
	 * @return
	 */
	String getRootGroupId();
	/**
	 * 获取根节点Group
	 * 包含完整的树结构信息
	 * @return
	 */
	GroupBean getGlobeGroupBean();
	/**
	 * 根据组ID查询组信息
	 * 向上查询该组上的所有组信息
	 * @param groupId
	 * @return
	 */
	GroupBean getUpstreamGroupBean(String groupId);
	/**
	 * 根据组ID查询组信息
	 * 向下查询该组下的所有组信息以及Job信息
	 * @param groupId
	 * @return
	 */
	GroupBean getDownstreamGroupBean(String groupId);
	
	GroupBean getDownstreamGroupBean(GroupBean parent);
	/**
	 * 根据groupId查询该组的记录
	 * @param groupId
	 * @return
	 */
	ZeusGroupWithBLOBs getGroupDescriptor(String groupId);
	/**
	 * 获取组下的组
	 * @param groupId
	 * @return
	 */
	List<ZeusGroupWithBLOBs> getChildrenGroup(String groupId);
	/**
	 * 根据JobId查询Job信息
	 * 向上查询所有的组信息
	 * @param jobId
	 * @return
	 */
	JobBean getUpstreamJobBean(String jobId);
	/**
	 * 根据jobid查询job的记录信息
	 * @param jobId
	 * @return
	 */
	Tuple<ActionDescriptor,JobStatus> getActionDescriptor(String jobId);
	/**
	 * 获取组下的job
	 * @param groupId
	 * @return
	 */
	List<Tuple<ActionDescriptor,JobStatus>> getChildrenAction(String groupId);
	/**
	 * 查询Job状态
	 * @param jobId
	 * @return
	 */
	JobStatus getActionStatus(String jobId);
	/**
	 * 批量查询Job信息
	 * @param jobIds
	 * @return
	 */
	Map<String, Tuple<ActionDescriptor, JobStatus>> getActionDescriptor(Collection<String> jobIds);
	/**
	 * 创建一个group
	 * @param user
	 * @return
	 */
	ZeusGroupWithBLOBs createGroup(String user, String groupName, String parentGroup, boolean isDirectory) throws ZeusException;
	/**
	 * 创建一个Job
	 * @param user
	 * @param group
	 * @return
	 */
	ActionDescriptor createAction(String user, String jobName, String parentGroup, JobRunType jobType) throws ZeusException;
	/**
	 * 删除组，成功删除需要的条件：
	 * 1.操作人是该组的创建者
	 * 2.该组下的任务没有被其他组依赖
	 * @param user
	 * @param groupId
	 * @return
	 */
	void deleteGroup(String user, String groupId) throws ZeusException;
	/**
	 * 删除一个Job
	 * 1.该job没有被其他job依赖
	 * 删除操作完成后，全量重新加载配置
	 * @param user
	 * @param jobId
	 * @return
	 * @throws ZeusException
	 */
	void deleteAction(String user, String jobId) throws ZeusException;
	/**
	 * 更新Job
	 * @param job
	 * @return
	 */
	void updateAction(String user, ActionDescriptor job) throws ZeusException;
	/**
	 * 更新Group
	 * @param group
	 * @return
	 */
	void updateGroup(String user, ZeusGroupWithBLOBs group) throws ZeusException;
	/**
	 * 更新Job状态
	 * @param jobStatus
	 * @throws ZeusException
	 */
	void updateActionStatus(JobStatus jobStatus);

	void grantJobOwner(String granter, String uid, String jobId)throws ZeusException;

	void grantGroupOwner(String granter, String uid, String groupId)throws ZeusException;

	void moveJob(String uid, String jobId, String groupId) throws ZeusException;

	void moveGroup(String uid, String groupId, String newParentGroupId) throws ZeusException;
	
	/**
	 * 获取worker列表
	 * @throws ZeusException
	 */
	List<String> getHosts() throws ZeusException;
	/**
	 * 保存或者更新worker，如果存在则更新
	 * @throws ZeusException
	 */
	void replaceWorker(ZeusWorker worker) throws ZeusException;
	/**
	 * 删除过期worker
	 * @throws ZeusException
	 */
	void removeWorker(String host) throws ZeusException;
	/**
	 * 保存jobAction
	 * @throws ZeusException
	 */
	void saveOrUpdateAction(ZeusActionWithBLOBs actionPer) throws ZeusException;

	/**
	 * 获取最近的jobAction
	 * @throws ZeusException
	 */
	List<ZeusActionWithBLOBs> getLastJobAction(String jobId);

	/**
	 * 保存jobAction
	 * @throws ZeusException
	 */
	void updateAction(ActionDescriptor actionPer) throws ZeusException;

	/**
	 * 获取jobAction列表
	 * @throws ZeusException
	 */
	List<Tuple<ActionDescriptor, JobStatus>> getActionList(String jobId);

	/**
	 * 移除jobAction
	 * @throws ZeusException
	 */
	void removeAction(Long actionId) throws ZeusException;

	boolean IsExistedBelowRootGroup(String GroupName);

}
