package com.taobao.zeus.web.util;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusGroupWithBLOBs;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.model.ActionDescriptor;
import com.taobao.zeus.model.ActionDescriptor.JobRunType;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.socket.worker.ClientWorker;
import com.taobao.zeus.util.Tuple;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 在操作数据库的同时，向调度系统发出更新命令，保证调度系统的数据是最新的
 * 此类主要是给Web界面使用
 * @author zhoufang
 *
 */
@Repository("scheduleGroupManagerWithAction")
public class ScheduleGroupManagerWithAction {

    @Autowired
	@Qualifier("mysqlGroupManagerWithAction")
    GroupManagerWithAction groupManager;

	private static Logger log=LogManager.getLogger(ScheduleGroupManagerWithAction.class);

	@Autowired
	private ClientWorker worker;

	public ZeusGroupWithBLOBs createGroup(String user, String groupName,String parentGroup, boolean isDirectory) throws ZeusException {
		return groupManager.createGroup(user, groupName, parentGroup, isDirectory);
	}

	public ActionDescriptor createAction(String user, String jobName,
                                         String parentGroup, JobRunType jobType) throws ZeusException {
		ActionDescriptor jd=groupManager.createAction(user, jobName, parentGroup, jobType);
		try {
			worker.updateJobFromWeb(jd.getId());
		} catch (Exception e) {
			String msg="创建Job成功，但是调度Job失败";
			log.error(msg,e);
			throw new ZeusException(msg,e);
		}
		return jd;
	}

	public void deleteGroup(String user, String groupId) throws ZeusException {
		groupManager.deleteGroup(user, groupId);
	}

	public void deleteAction(String user, String jobId) throws ZeusException {
		groupManager.deleteAction(user, jobId);
		try {
			worker.updateJobFromWeb(jobId);
		} catch (Exception e) {
			String msg="删除Job成功，但是调度Job失败";
			log.error(msg,e);
			throw new ZeusException(msg, e);
		}
	}

	public GroupBean getDownstreamGroupBean(String groupId) {
		return groupManager.getDownstreamGroupBean(groupId);
	}

	public GroupBean getGlobeGroupBean() {
		return groupManager.getGlobeGroupBean();
	}

	public ZeusGroupWithBLOBs getGroupDescriptor(String groupId) {
		return groupManager.getGroupDescriptor(groupId);
	}

	public Tuple<ActionDescriptor, JobStatus> getActionDescriptor(String jobId) {
		return groupManager.getActionDescriptor(jobId);
	}

	public String getRootGroupId() {
		return groupManager.getRootGroupId();
	}

	public GroupBean getUpstreamGroupBean(String groupId) {
		return groupManager.getUpstreamGroupBean(groupId);
	}

	public JobBean getUpstreamJobBean(String jobId) {
		return groupManager.getUpstreamJobBean(jobId);
	}

	public void updateGroup(String user, ZeusGroupWithBLOBs group) throws ZeusException {
		groupManager.updateGroup(user, group);
	}

	public void updateAction(String user, ActionDescriptor job) throws ZeusException {
		groupManager.updateAction(user, job);
		try {
			worker.updateJobFromWeb(job.getId());
		} catch (Exception e) {
			String msg="更新Job成功，但是调度Job失败";
			log.error(msg,e);
			throw new ZeusException(msg,e);
		}
	}

	public Map<String, Tuple<ActionDescriptor, JobStatus>> getActionDescriptor(Collection<String> jobIds) {
		return groupManager.getActionDescriptor(jobIds);
	}

	public void updateActionStatus(JobStatus jobStatus){
		throw new UnsupportedOperationException("ScheduleGroupManagerWithAction 不支持此操作");
	}

	public JobStatus getActionStatus(String jobId) {
		return groupManager.getActionStatus(jobId);
	}

	public void grantGroupOwner(String granter, String uid, String groupId)
			throws ZeusException {
		groupManager.grantGroupOwner(granter, uid, groupId);
	}

	public void grantJobOwner(String granter, String uid, String jobId)
			throws ZeusException {
		groupManager.grantJobOwner(granter, uid, jobId);
	}

	public List<ZeusGroupWithBLOBs> getChildrenGroup(String groupId) {
		return groupManager.getChildrenGroup(groupId);
	}

	public List<Tuple<ActionDescriptor, JobStatus>> getChildrenAction(String groupId) {
		return groupManager.getChildrenAction(groupId);
	}

	public GroupBean getDownstreamGroupBean(GroupBean parent) {
		return groupManager.getDownstreamGroupBean(parent);
	}

	public void moveJob(String uid, String jobId, String groupId)
			throws ZeusException {
		groupManager.moveJob(uid, jobId, groupId);
	}

	public void moveGroup(String uid, String groupId, String newParentGroupId)
			throws ZeusException {
		groupManager.moveGroup(uid, groupId, newParentGroupId);
	}


	public void saveOrUpdateAction(ZeusActionWithBLOBs actionPer) throws ZeusException {
		groupManager.saveOrUpdateAction(actionPer);
	}

	public List<ZeusActionWithBLOBs> getLastJobAction(String jobId) {
		return groupManager.getLastJobAction(jobId);
	}

	public void updateAction(ActionDescriptor actionPer) throws ZeusException {
		groupManager.updateAction(actionPer);
	}

	public List<Tuple<ActionDescriptor, JobStatus>> getActionList(String jobId) {
		return groupManager.getActionList(jobId);
	}
	
	public void removeAction(Long actionId) throws ZeusException {
		groupManager.removeAction(actionId);
	}

	public boolean IsExistedBelowRootGroup(String GroupName) {
		return groupManager.IsExistedBelowRootGroup(GroupName);
	}
}
