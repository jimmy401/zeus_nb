package com.taobao.zeus.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusGroupWithBLOBs;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.model.ActionDescriptor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.model.ActionDescriptor.JobRunType;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.processer.JobProcesser;
import com.taobao.zeus.model.processer.Processer;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.dal.logic.PermissionManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.util.Tuple;
import org.springframework.stereotype.Repository;

/**
 * 权限验证，需要的操作权限验证不通过，将抛出异常
 * @author zhoufang
 *
 */
@Repository
public class PermissionGroupManagerWithAction{
	private Logger log=LogManager.getLogger(PermissionGroupManagerWithAction.class);

	@Autowired
	@Qualifier("scheduleGroupManagerWithAction")
	ScheduleGroupManagerWithAction scheduleGroupManagerWithAction;

	@Autowired
	@Qualifier("mysqlPermissionManager")
	private PermissionManager permissionManager;
	@Autowired
	@Qualifier("mysqlUserManager")
	private UserManager userManager;
   //new class method begin
	public Tuple<ActionDescriptor,JobStatus> getActionDescriptor(String jobId) {
		return scheduleGroupManagerWithAction.getActionDescriptor(jobId);
	}
	public GroupBean getDownstreamGroupBean(String groupId) {
		return scheduleGroupManagerWithAction.getDownstreamGroupBean(groupId);
	}

	public List<Long> getJobACtion(String jobId) {
		return permissionManager.getJobACtion(jobId);
	}
	//new class method end

	private Boolean isGroupOwner(String uid,GroupBean gb){
		List<String> owners=new ArrayList<String>();
		while(gb!=null){
			if(!owners.contains(gb.getGroupDescriptor().getOwner())){
				owners.add(gb.getGroupDescriptor().getOwner());
			}
			gb=gb.getParentGroupBean();
		}
		if(owners.contains(uid)){
			return true;
		}
		return false;
	}
	private Boolean isGroupOwner(String uid,String groupId){
		return isGroupOwner(uid, scheduleGroupManagerWithAction.getUpstreamGroupBean(groupId));
	}
	private Boolean isJobOwner(String uid,String jobId){
		JobBean jb=scheduleGroupManagerWithAction.getUpstreamJobBean(jobId);
		if(jb.getActionDescriptor().getOwner().equalsIgnoreCase(uid)){
			return true;
		}
		return isGroupOwner(uid, jb.getGroupBean());
	}
	
	public Boolean hasGroupPermission(String uid,String groupId){
		if(isGroupOwner(uid, groupId)){
			return true;
		}
		return permissionManager.hasGroupPermission(uid, groupId);
	}
	public Boolean hasJobPermission(String uid,String jobId){
		if(isJobOwner(uid, jobId)){
			return true;
		}
		return permissionManager.hasJobPermission(uid, jobId);
	}
	public ZeusGroupWithBLOBs createGroup(String user, String groupName,String parentGroup, boolean isDirectory) throws ZeusException {
		if(hasGroupPermission(user, parentGroup)){
			return scheduleGroupManagerWithAction.createGroup(user, groupName, parentGroup, isDirectory);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	public ActionDescriptor createAction(String user, String jobName,String parentGroup, JobRunType jobType) throws ZeusException {
		if(hasGroupPermission(user, parentGroup)){
			return scheduleGroupManagerWithAction.createAction(user, jobName, parentGroup, jobType);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	public void deleteGroup(String user, String groupId) throws ZeusException {
		if(hasGroupPermission(user, groupId)){
			ZeusGroupWithBLOBs gd=scheduleGroupManagerWithAction.getGroupDescriptor(groupId);
			if(gd!=null && gd.getOwner().equals(user)){
				scheduleGroupManagerWithAction.deleteGroup(user, groupId);
			}
		}else{
			throw new ZeusException("您无权操作");
		}
		
	}

	public void deleteAction(String user, String jobId) throws ZeusException {
		if(hasJobPermission(user, jobId)){
			Tuple<ActionDescriptor, JobStatus> job=scheduleGroupManagerWithAction.getActionDescriptor(jobId);
			if(job!=null){
				scheduleGroupManagerWithAction.deleteAction(user, jobId);
			}
		}else{
			throw new ZeusException("没有删除的权限");
		}
	}

	public GroupBean getGlobeGroupBean() {
		return scheduleGroupManagerWithAction.getGlobeGroupBean();
	}

	public ZeusGroupWithBLOBs getGroupDescriptor(String groupId) {
		return scheduleGroupManagerWithAction.getGroupDescriptor(groupId);
	}

	public String getRootGroupId() {
		return scheduleGroupManagerWithAction.getRootGroupId();
	}

	public GroupBean getUpstreamGroupBean(String groupId) {
		return scheduleGroupManagerWithAction.getUpstreamGroupBean(groupId);
	}

	public JobBean getUpstreamJobBean(String jobId) {
		return scheduleGroupManagerWithAction.getUpstreamJobBean(jobId);
	}

	public void updateGroup(String user, ZeusGroupWithBLOBs group)
			throws ZeusException {
		if(hasGroupPermission(user, group.getId().toString())){
			ZeusGroupWithBLOBs gd=scheduleGroupManagerWithAction.getGroupDescriptor(group.getId().toString());
			if(gd!=null){
				scheduleGroupManagerWithAction.updateGroup(user, group);
			}
		}else{
			throw new ZeusException("没有更新的权限");
		}
		
	}

	public void updateAction(String user, ActionDescriptor job) throws ZeusException {
		if(hasJobPermission(user, job.getId())){
			Tuple<ActionDescriptor, JobStatus> old=scheduleGroupManagerWithAction.getActionDescriptor(job.getId());
			if(old!=null ){
				List<JobProcesser> hasadd=new ArrayList<JobProcesser>();
				for(Processer p:old.getX().getPreProcessers()){
					if(p instanceof JobProcesser){
						hasadd.add((JobProcesser)p);
					}
				}
				for(Processer p:old.getX().getPostProcessers()){
					if(p instanceof JobProcesser){
						hasadd.add((JobProcesser)p);
					}
				}
				List<JobProcesser> thistime=new ArrayList<JobProcesser>();
				for(Processer p:job.getPreProcessers()){
					if(p instanceof JobProcesser){
						thistime.add((JobProcesser)p);
					}
				}
				for(Processer p:job.getPostProcessers()){
					if(p instanceof JobProcesser){
						thistime.add((JobProcesser)p);
					}
				}
				for(JobProcesser jp:thistime){
					if(jp.getJobId().equals(job.getId())){
						throw new ZeusException("不得将自身设置为自身的处理器");
					}
					boolean exist=false;
					for(JobProcesser jp2:hasadd){
						if(jp2.getId().equalsIgnoreCase(jp.getId())){
							exist=true;
							break;
						}
					}
					if(!exist && !hasJobPermission(user, jp.getJobId())){
						throw new ZeusException("您没有权限将Job："+jp.getJobId() +" 添加到处理单元中");
					}
				}
				scheduleGroupManagerWithAction.updateAction(user, job);
			}
		}else{
			throw new ZeusException("没有更新的权限");
		}
		
	}

	public Map<String, Tuple<ActionDescriptor,JobStatus>> getActionDescriptor(Collection<String> jobIds) {
		return scheduleGroupManagerWithAction.getActionDescriptor(jobIds);
	}
	public void updateActionStatus(JobStatus jobStatus) {
		throw new UnsupportedOperationException("PermissionGroupManager 不支持此操作");
	}
	public JobStatus getActionStatus(String jobId) {
		return scheduleGroupManagerWithAction.getActionStatus(jobId);
	}
	public void grantGroupOwner(String granter, String uid, String groupId) throws ZeusException{
		ZeusUser nextUser = userManager.findByUidFilter(uid);
		//if (nextUser.getUserType() != 0) {
		//	throw new ZeusException("请转给组管理员！");
		//}
		if (nextUser.getIsEffective() != 1) {
			throw new ZeusException("请转给有效用户");
		}
		GroupBean gb=scheduleGroupManagerWithAction.getUpstreamGroupBean(groupId);
		List<String> owners=new ArrayList<String>();
		while(gb!=null){
			if(!owners.contains(gb.getGroupDescriptor().getOwner())){
				owners.add(gb.getGroupDescriptor().getOwner());
			}
			gb=gb.getParentGroupBean();
		}
		if(owners.contains(granter)){
			scheduleGroupManagerWithAction.grantGroupOwner(granter, uid, groupId);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	public void grantJobOwner(String granter, String uid, String jobId) throws ZeusException{
		ZeusUser nextUser = userManager.findByUidFilter(uid);
		//if (nextUser.getUserType() != 0) {
		//	throw new ZeusException("请转给组管理员！");
		//}
		if (nextUser.getIsEffective() != 1) {
			throw new ZeusException("请转给有效用户");
		}
		JobBean jb=scheduleGroupManagerWithAction.getUpstreamJobBean(jobId);
		List<String> owners=new ArrayList<String>();
		owners.add(jb.getActionDescriptor().getOwner());
		GroupBean gb=jb.getGroupBean();
		while(gb!=null){
			if(!owners.contains(gb.getGroupDescriptor().getOwner())){
				owners.add(gb.getGroupDescriptor().getOwner());
			}
			gb=gb.getParentGroupBean();
		}
		if(owners.contains(granter)){
			scheduleGroupManagerWithAction.grantJobOwner(granter, uid, jobId);
		}else{
			throw new ZeusException("您无权操作");
		}
		
	}
	public void addGroupAdmin(String granter,String user, String groupId) throws ZeusException {
		if(isGroupOwner(granter, groupId)){
			permissionManager.addGroupAdmin(user, groupId);
		}else{
			throw new ZeusException("您无权操作");
		}
	}
	public void addJobAdmin(String granter,String user, String jobId) throws ZeusException {
		if(isJobOwner(granter, jobId)){
			permissionManager.addJobAdmin(user, jobId);
		}else{
			throw new ZeusException("您无权操作");
		}
	}
	public void removeGroupAdmin(String granter,String user, String groupId) throws ZeusException {
		if(isGroupOwner(granter, groupId)){
			permissionManager.removeGroupAdmin(user, groupId);
		}else{
			throw new ZeusException("您无权操作");
		}
	}
	public void removeJobAdmin(String granter,String user, String jobId) throws ZeusException {
		if(isJobOwner(granter, jobId)){
			permissionManager.removeJobAdmin(user, jobId);
		}else{
			throw new ZeusException("您无权操作");
		}
	}
	public List<ZeusUser> getGroupAdmins(String groupId) {
		return userManager.findListByUid(permissionManager.getGroupAdmins(groupId));
	}
	public List<ZeusUser> getJobAdmins(String jobId) {
		return userManager.findListByUid(permissionManager.getJobAdmins(jobId));
	}

	public List<ZeusGroupWithBLOBs> getChildrenGroup(String groupId) {
		return scheduleGroupManagerWithAction.getChildrenGroup(groupId);
	}
	public List<Tuple<ActionDescriptor, JobStatus>> getChildrenAction(String groupId) {
		return scheduleGroupManagerWithAction.getChildrenAction(groupId);
	}
	public GroupBean getDownstreamGroupBean(GroupBean parent) {
		return scheduleGroupManagerWithAction.getDownstreamGroupBean(parent);
	}
	public void moveJob(String uid, String jobId, String groupId)
			throws ZeusException {
		if(!permissionManager.hasGroupPermission(uid, groupId) || !permissionManager.hasJobPermission(uid, jobId)){
			throw new ZeusException("您无权操作");
		}
		scheduleGroupManagerWithAction.moveJob(uid, jobId, groupId);
	}
	public void moveGroup(String uid, String groupId, String newParentGroupId)
			throws ZeusException {
		if(!permissionManager.hasGroupPermission(uid, groupId) ||
				!permissionManager.hasGroupPermission(uid, newParentGroupId)){
			throw new ZeusException("您无权操作");
		}
		scheduleGroupManagerWithAction.moveGroup(uid, groupId, newParentGroupId);
	}

	public void saveOrUpdateAction(ZeusActionWithBLOBs actionPer) throws ZeusException {
		scheduleGroupManagerWithAction.saveOrUpdateAction(actionPer);
		
	}
	public List<ZeusActionWithBLOBs> getLastJobAction(String jobId) {
		return scheduleGroupManagerWithAction.getLastJobAction(jobId);
	}
	public void updateAction(ActionDescriptor actionPer) throws ZeusException {
		scheduleGroupManagerWithAction.updateAction(actionPer);
	}
	public List<Tuple<ActionDescriptor, JobStatus>> getActionList(String jobId) {
		return scheduleGroupManagerWithAction.getActionList(jobId);
	}
	public void removeAction(Long actionId) throws ZeusException {
		scheduleGroupManagerWithAction.removeAction(actionId);
		
	}
	public boolean IsExistedBelowRootGroup(String GroupName) {
		return scheduleGroupManagerWithAction.IsExistedBelowRootGroup(GroupName);
	}
}
