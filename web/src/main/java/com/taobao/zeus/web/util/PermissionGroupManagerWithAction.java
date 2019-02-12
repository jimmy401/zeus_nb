package com.taobao.zeus.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.dal.model.ZeusWorker;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobDescriptor.JobRunType;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.processer.JobProcesser;
import com.taobao.zeus.model.processer.Processer;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.logic.GroupManagerWithAction;
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
public class PermissionGroupManagerWithAction implements GroupManagerWithAction {
	private Logger log=LogManager.getLogger(PermissionGroupManagerWithAction.class);

	@Autowired
	@Qualifier("scheduleGroupManagerWithAction")
    GroupManagerWithAction groupManager;

	@Autowired
	@Qualifier("mysqlPermissionManager")
	private PermissionManager permissionManager;
	@Autowired
	@Qualifier("mysqlUserManager")
	private UserManager userManager;
   //new class method begin
	@Override
	public Tuple<JobDescriptor,JobStatus> getActionDescriptor(String jobId) {
		return groupManager.getActionDescriptor(jobId);
	}
	@Override
	public GroupBean getDownstreamGroupBean(String groupId) {
		return groupManager.getDownstreamGroupBean(groupId);
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
		return isGroupOwner(uid, groupManager.getUpstreamGroupBean(groupId));
	}
	private Boolean isJobOwner(String uid,String jobId){
		JobBean jb=groupManager.getUpstreamJobBean(jobId);
		if(jb.getJobDescriptor().getOwner().equalsIgnoreCase(uid)){
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
	@Override
	public GroupDescriptor createGroup(String user, String groupName,
			String parentGroup, boolean isDirectory) throws ZeusException {
		if(hasGroupPermission(user, parentGroup)){
			return groupManager.createGroup(user, groupName, parentGroup, isDirectory);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	@Override
	public JobDescriptor createAction(String user, String jobName,
									  String parentGroup, JobRunType jobType) throws ZeusException {
		if(hasGroupPermission(user, parentGroup)){
			return groupManager.createAction(user, jobName, parentGroup, jobType);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	@Override
	public void deleteGroup(String user, String groupId) throws ZeusException {
		if(hasGroupPermission(user, groupId)){
			GroupDescriptor gd=groupManager.getGroupDescriptor(groupId);
			if(gd!=null && gd.getOwner().equals(user)){
				groupManager.deleteGroup(user, groupId);
			}
		}else{
			throw new ZeusException("您无权操作");
		}
		
	}

	@Override
	public void deleteAction(String user, String jobId) throws ZeusException {
		if(hasJobPermission(user, jobId)){
			Tuple<JobDescriptor, JobStatus> job=groupManager.getActionDescriptor(jobId);
			if(job!=null){
				groupManager.deleteAction(user, jobId);
			}
		}else{
			throw new ZeusException("没有删除的权限");
		}
	}

	@Override
	public GroupBean getGlobeGroupBean() {
		return groupManager.getGlobeGroupBean();
	}

	@Override
	public GroupDescriptor getGroupDescriptor(String groupId) {
		return groupManager.getGroupDescriptor(groupId);
	}

	@Override
	public String getRootGroupId() {
		return groupManager.getRootGroupId();
	}

	@Override
	public GroupBean getUpstreamGroupBean(String groupId) {
		return groupManager.getUpstreamGroupBean(groupId);
	}

	@Override
	public JobBean getUpstreamJobBean(String jobId) {
		return groupManager.getUpstreamJobBean(jobId);
	}

	@Override
	public void updateGroup(String user, GroupDescriptor group)
			throws ZeusException {
		if(hasGroupPermission(user, group.getId())){
			GroupDescriptor gd=groupManager.getGroupDescriptor(group.getId());
			if(gd!=null){
				groupManager.updateGroup(user, group);
			}
		}else{
			throw new ZeusException("没有更新的权限");
		}
		
	}

	@Override
	public void updateAction(String user, JobDescriptor job) throws ZeusException {
		if(hasJobPermission(user, job.getId())){
			Tuple<JobDescriptor, JobStatus> old=groupManager.getActionDescriptor(job.getId());
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
				groupManager.updateAction(user, job);
			}
		}else{
			throw new ZeusException("没有更新的权限");
		}
		
	}

	@Override
	public Map<String, Tuple<JobDescriptor,JobStatus>> getActionDescriptor(Collection<String> jobIds) {
		return groupManager.getActionDescriptor(jobIds);
	}
	@Override
	public void updateActionStatus(JobStatus jobStatus) {
		throw new UnsupportedOperationException("PermissionGroupManager 不支持此操作");
	}
	@Override
	public JobStatus getActionStatus(String jobId) {
		return groupManager.getActionStatus(jobId);
	}
	@Override
	public void grantGroupOwner(String granter, String uid, String groupId) throws ZeusException{
		ZeusUser nextUser = userManager.findByUidFilter(uid);
		//if (nextUser.getUserType() != 0) {
		//	throw new ZeusException("请转给组管理员！");
		//}
		if (nextUser.getIsEffective() != 1) {
			throw new ZeusException("请转给有效用户");
		}
		GroupBean gb=groupManager.getUpstreamGroupBean(groupId);
		List<String> owners=new ArrayList<String>();
		while(gb!=null){
			if(!owners.contains(gb.getGroupDescriptor().getOwner())){
				owners.add(gb.getGroupDescriptor().getOwner());
			}
			gb=gb.getParentGroupBean();
		}
		if(owners.contains(granter)){
			groupManager.grantGroupOwner(granter, uid, groupId);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	@Override
	public void grantJobOwner(String granter, String uid, String jobId) throws ZeusException{
		ZeusUser nextUser = userManager.findByUidFilter(uid);
		//if (nextUser.getUserType() != 0) {
		//	throw new ZeusException("请转给组管理员！");
		//}
		if (nextUser.getIsEffective() != 1) {
			throw new ZeusException("请转给有效用户");
		}
		JobBean jb=groupManager.getUpstreamJobBean(jobId);
		List<String> owners=new ArrayList<String>();
		owners.add(jb.getJobDescriptor().getOwner());
		GroupBean gb=jb.getGroupBean();
		while(gb!=null){
			if(!owners.contains(gb.getGroupDescriptor().getOwner())){
				owners.add(gb.getGroupDescriptor().getOwner());
			}
			gb=gb.getParentGroupBean();
		}
		if(owners.contains(granter)){
			groupManager.grantJobOwner(granter, uid, jobId);
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

	@Override
	public List<GroupDescriptor> getChildrenGroup(String groupId) {
		return groupManager.getChildrenGroup(groupId);
	}
	@Override
	public List<Tuple<JobDescriptor, JobStatus>> getChildrenAction(String groupId) {
		return groupManager.getChildrenAction(groupId);
	}
	@Override
	public GroupBean getDownstreamGroupBean(GroupBean parent) {
		return groupManager.getDownstreamGroupBean(parent);
	}
	@Override
	public void moveJob(String uid, String jobId, String groupId)
			throws ZeusException {
		if(!permissionManager.hasGroupPermission(uid, groupId) || !permissionManager.hasJobPermission(uid, jobId)){
			throw new ZeusException("您无权操作");
		}
		groupManager.moveJob(uid, jobId, groupId);
	}
	@Override
	public void moveGroup(String uid, String groupId, String newParentGroupId)
			throws ZeusException {
		if(!permissionManager.hasGroupPermission(uid, groupId) ||
				!permissionManager.hasGroupPermission(uid, newParentGroupId)){
			throw new ZeusException("您无权操作");
		}
		groupManager.moveGroup(uid, groupId, newParentGroupId);
	}
	@Override
	public List<String> getHosts() throws ZeusException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void replaceWorker(ZeusWorker worker) throws ZeusException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeWorker(String host) throws ZeusException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveOrUpdateAction(ZeusActionWithBLOBs actionPer) throws ZeusException {
		groupManager.saveOrUpdateAction(actionPer);
		
	}
	@Override
	public List<ZeusActionWithBLOBs> getLastJobAction(String jobId) {
		return groupManager.getLastJobAction(jobId);
	}
	@Override
	public void updateAction(JobDescriptor actionPer) throws ZeusException {
		groupManager.updateAction(actionPer);
	}
	@Override
	public List<Tuple<JobDescriptor, JobStatus>> getActionList(String jobId) {
		return groupManager.getActionList(jobId);
	}
	@Override
	public void removeAction(Long actionId) throws ZeusException {
		groupManager.removeAction(actionId);
		
	}
	@Override
	public boolean IsExistedBelowRootGroup(String GroupName) {
		return groupManager.IsExistedBelowRootGroup(GroupName);
	}
}
