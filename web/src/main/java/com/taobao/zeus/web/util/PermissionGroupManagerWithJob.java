package com.taobao.zeus.web.util;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.GroupManagerWithJob;
import com.taobao.zeus.dal.logic.PermissionManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.model.ZeusJobWithBLOBs;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.dal.model.ZeusWorker;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.processer.JobProcesser;
import com.taobao.zeus.model.processer.Processer;
import com.taobao.zeus.util.Tuple;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 权限验证，需要的操作权限验证不通过，将抛出异常
 * @author zhoufang
 *
 */
@Repository
public class PermissionGroupManagerWithJob implements GroupManagerWithJob {
	private Logger log=LogManager.getLogger(PermissionGroupManagerWithJob.class);

	@Autowired
	@Qualifier("scheduleGroupManagerWithJob")
	GroupManagerWithJob groupManagerWithJob;

	@Autowired
	@Qualifier("mysqlPermissionManager")
	private PermissionManager permissionManager;
	@Autowired
	@Qualifier("mysqlUserManager")
	private UserManager userManager;
	
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
		return isGroupOwner(uid, groupManagerWithJob.getUpstreamGroupBean(groupId));
	}
	private Boolean isJobOwner(String uid,String jobId){
		JobBean jb= groupManagerWithJob.getUpstreamJobBean(jobId);
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
			return groupManagerWithJob.createGroup(user, groupName, parentGroup, isDirectory);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	@Override
	public JobDescriptor createJob(String user, String jobName,
								   String parentGroup, JobDescriptor.JobRunType jobType) throws ZeusException {
		if(hasGroupPermission(user, parentGroup)){
			return groupManagerWithJob.createJob(user, jobName, parentGroup, jobType);
		}else{
			throw new ZeusException("您无权操作");
		}
	}

	@Override
	public void deleteGroup(String user, String groupId) throws ZeusException {
		if(hasGroupPermission(user, groupId)){
			GroupDescriptor gd= groupManagerWithJob.getGroupDescriptor(groupId);
			if(gd!=null && gd.getOwner().equals(user)){
				groupManagerWithJob.deleteGroup(user, groupId);
			}
		}else{
			throw new ZeusException("您无权操作");
		}
		
	}

	@Override
	public void deleteJob(String user, String jobId) throws ZeusException {
		if(hasJobPermission(user, jobId)){
			Tuple<JobDescriptor, JobStatus> job= groupManagerWithJob.getJobDescriptor(jobId);
			if(job!=null){
				groupManagerWithJob.deleteJob(user, jobId);
			}
		}else{
			throw new ZeusException("没有删除的权限");
		}
	}

	@Override
	public GroupBean getDownstreamGroupBean(String groupId) {
		return groupManagerWithJob.getDownstreamGroupBean(groupId);
	}

	@Override
	public GroupBean getGlobeGroupBean() {
		return groupManagerWithJob.getGlobeGroupBean();
	}

	@Override
	public GroupDescriptor getGroupDescriptor(String groupId) {
		return groupManagerWithJob.getGroupDescriptor(groupId);
	}

	@Override
	public Tuple<JobDescriptor,JobStatus> getJobDescriptor(String jobId) {
		return groupManagerWithJob.getJobDescriptor(jobId);
	}

	@Override
	public String getRootGroupId() {
		return groupManagerWithJob.getRootGroupId();
	}

	@Override
	public GroupBean getUpstreamGroupBean(String groupId) {
		return groupManagerWithJob.getUpstreamGroupBean(groupId);
	}

	@Override
	public JobBean getUpstreamJobBean(String jobId) {
		return groupManagerWithJob.getUpstreamJobBean(jobId);
	}

	@Override
	public void updateGroup(String user, GroupDescriptor group)
			throws ZeusException {
		if(hasGroupPermission(user, group.getId())){
			GroupDescriptor gd= groupManagerWithJob.getGroupDescriptor(group.getId());
			if(gd!=null){
				groupManagerWithJob.updateGroup(user, group);
			}
		}else{
			throw new ZeusException("没有更新的权限");
		}
		
	}

	@Override
	public void updateJob(String user, JobDescriptor job) throws ZeusException {
		if(hasJobPermission(user, job.getId())){
			Tuple<JobDescriptor, JobStatus> old= groupManagerWithJob.getJobDescriptor(job.getId());
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
				groupManagerWithJob.updateJob(user, job);
			}
		}else{
			throw new ZeusException("没有更新的权限");
		}
		
	}

	@Override
	public Map<String, Tuple<JobDescriptor,JobStatus>> getJobDescriptor(Collection<String> jobIds) {
		return groupManagerWithJob.getJobDescriptor(jobIds);
	}
	@Override
	public void updateJobStatus(JobStatus jobStatus) {
		throw new UnsupportedOperationException("PermissionGroupManager 不支持此操作");
	}
	@Override
	public JobStatus getJobStatus(String jobId) {
		return groupManagerWithJob.getJobStatus(jobId);
	}
	@Override
	public void grantGroupOwner(String granter, String uid, String groupId) throws ZeusException {
		ZeusUser nextUser = userManager.findByUidFilter(uid);
		//if (nextUser.getUserType() != 0) {
		//	throw new ZeusException("请转给组管理员！");
		//}
		if (nextUser.getIsEffective() != 1) {
			throw new ZeusException("请转给有效用户");
		}
		GroupBean gb= groupManagerWithJob.getUpstreamGroupBean(groupId);
		List<String> owners=new ArrayList<String>();
		while(gb!=null){
			if(!owners.contains(gb.getGroupDescriptor().getOwner())){
				owners.add(gb.getGroupDescriptor().getOwner());
			}
			gb=gb.getParentGroupBean();
		}
		if(owners.contains(granter)){
			groupManagerWithJob.grantGroupOwner(granter, uid, groupId);
		}else{
			throw new ZeusException("您无权操作");
		}
	}
	@Override
	public void grantJobOwner(String granter, String uid, String jobId) throws ZeusException {
		ZeusUser nextUser = userManager.findByUidFilter(uid);
		//if (nextUser.getUserType() != 0) {
		//	throw new ZeusException("请转给组管理员！");
		//}
		if (nextUser.getIsEffective() != 1) {
			throw new ZeusException("请转给有效用户");
		}
		JobBean jb= groupManagerWithJob.getUpstreamJobBean(jobId);
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
			groupManagerWithJob.grantJobOwner(granter, uid, jobId);
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
	public List<Long> getJobACtion(String jobId) {
		return permissionManager.getJobACtion(jobId);
	}
	@Override
	public List<GroupDescriptor> getChildrenGroup(String groupId) {
		return groupManagerWithJob.getChildrenGroup(groupId);
	}
	@Override
	public List<Tuple<JobDescriptor, JobStatus>> getChildrenJob(String groupId) {
		return groupManagerWithJob.getChildrenJob(groupId);
	}
	@Override
	public GroupBean getDownstreamGroupBean(GroupBean parent) {
		return groupManagerWithJob.getDownstreamGroupBean(parent);
	}
	@Override
	public void moveJob(String uid, String jobId, String groupId)
			throws ZeusException {
		if(!permissionManager.hasGroupPermission(uid, groupId) || !permissionManager.hasJobPermission(uid, jobId)){
			throw new ZeusException("您无权操作");
		}
		groupManagerWithJob.moveJob(uid, jobId, groupId);
	}
	@Override
	public void moveGroup(String uid, String groupId, String newParentGroupId)
			throws ZeusException {
		if(!permissionManager.hasGroupPermission(uid, groupId) ||
				!permissionManager.hasGroupPermission(uid, newParentGroupId)){
			throw new ZeusException("您无权操作");
		}
		groupManagerWithJob.moveGroup(uid, groupId, newParentGroupId);
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
	public List<ZeusJobWithBLOBs> getAllJobs() {
		return null;
	}
	@Override
	public List<String> getAllDependencied(String jobID) {
		return groupManagerWithJob.getAllDependencied(jobID);
	}
	@Override
	public List<String> getAllDependencies(String jobID) {
		return groupManagerWithJob.getAllDependencies(jobID);
	}
	@Override
	public void updateActionList(JobDescriptor job) {
		groupManagerWithJob.updateActionList(job);
	}
	
}
