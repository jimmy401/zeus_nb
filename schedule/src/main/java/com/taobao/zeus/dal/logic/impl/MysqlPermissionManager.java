package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.logic.GroupManagerWithJob;
import com.taobao.zeus.dal.logic.PermissionManager;
import com.taobao.zeus.dal.mapper.ZeusActionMapper;
import com.taobao.zeus.dal.mapper.ZeusPermissionMapper;
import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusPermission;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.dal.tool.Super;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@SuppressWarnings("unchecked")
@Repository("mysqlPermissionManager")
public class MysqlPermissionManager implements PermissionManager {
	@Autowired
	@Qualifier("mysqlGroupManagerWithAction")
	private GroupManagerWithAction groupManagerWithAction;

	@Autowired
	@Qualifier("mysqlGroupManagerWithJob")
	private GroupManagerWithJob groupManagerWithJob;

	@Autowired
	ZeusPermissionMapper zeusPermissionMapper;

	@Autowired
	ZeusActionMapper zeusActionMapper;
	
	@Override
	public Boolean hasGroupPermission(final String user, final String groupId) {
		if(Super.getSupers().contains(user)){
			//超级管理员
			return true;
		}
		Set<String> groups=new HashSet<String>();
		GroupBean gb=groupManagerWithJob.getUpstreamGroupBean(groupId);
		if(user.equals(gb.getGroupDescriptor().getOwner())){
			//组所有人
			return true;
		}
		while(gb!=null){
			groups.add(gb.getGroupDescriptor().getId());
			gb=gb.getParentGroupBean();
		}
		Set<String> users=new HashSet<String>();
		for(String g:groups){
			users.addAll(getGroupAdmins(g));
		}
		return users.contains(user)?true:false;
	}
	
	public List<String> getGroupAdmins(final String groupId){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", ZeusPermission.GROUP_TYPE);
		params.put("targetId",  Long.valueOf(groupId));
		List<ZeusPermission> list = zeusPermissionMapper.selectByTypes(params);
		List<String> uids=new ArrayList<String>();
		for (ZeusPermission item: list) {
			uids.add(item.getUid());
		}
		return uids;
	}
	public List<String> getJobAdmins(final String jobId){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", ZeusPermission.JOB_TYPE);
		params.put("targetId",  Long.valueOf(jobId));
		List<ZeusPermission> list = zeusPermissionMapper.selectByTypes(params);
		List<String> uids=new ArrayList<String>();
		for (ZeusPermission item: list) {
			uids.add(item.getUid());
		}
		return uids;
	}
	public List<Long> getJobACtion(final String jobId){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("jobId", jobId);
		List<ZeusActionWithBLOBs> list = zeusActionMapper.selectByJobId(params);

		List<Long> uids=new ArrayList<Long>();
		for (ZeusActionWithBLOBs item: list) {
			uids.add(item.getId());
		}
		return uids;

	}
	private ZeusPermission getGroupPermission(final String user,final String groupId){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", ZeusPermission.GROUP_TYPE);
		params.put("uid", user);
		params.put("targetId", Long.valueOf(groupId));
		List<ZeusPermission> list = zeusPermissionMapper.selectByUid(params);

		if(list!=null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	private ZeusPermission getJobPermission(final String user,final String jobId){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", ZeusPermission.JOB_TYPE);
		params.put("uid", user);
		params.put("targetId", Long.valueOf(jobId));
		List<ZeusPermission> list = zeusPermissionMapper.selectByUid(params);

		if(list!=null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	@Override
	public void addGroupAdmin(String user,String groupId) {
		boolean has=getGroupPermission(user, groupId)==null?false:true;
		if(!has){
			ZeusPermission pp=new ZeusPermission();
			pp.setType(ZeusPermission.GROUP_TYPE);
			pp.setUid(user);
			pp.setTargetId(Long.valueOf(groupId));
			pp.setGmtModified(new Date());
			zeusPermissionMapper.insertSelective(pp);
		}
	}
	@Override
	public void addJobAdmin(String user, String jobId) {
		boolean has=getJobPermission(user, jobId)==null?false:true;
		if(!has){
			ZeusPermission pp=new ZeusPermission();
			pp.setType(ZeusPermission.JOB_TYPE);
			pp.setUid(user);
			pp.setTargetId(Long.valueOf(jobId));
			pp.setGmtModified(new Date());
			zeusPermissionMapper.insertSelective(pp);
		}
	}
	@Override
	public Boolean hasJobPermission(String user, String jobId) {
		if(Super.getSupers().contains(user)){
			//超级管理员
			return true;
		}
		Set<String> groups=new HashSet<String>();
		JobBean jobBean=groupManagerWithJob.getUpstreamJobBean(jobId);
		if(user.equals(jobBean.getJobDescriptor().getOwner())){
			//任务所有人
			return true;
		}
		GroupBean gb=jobBean.getGroupBean();
		while(gb!=null){
			groups.add(gb.getGroupDescriptor().getId());
			gb=gb.getParentGroupBean();
		}
		Set<String> users=new HashSet<String>();
		users.addAll(getJobAdmins(jobId));
		for(String g:groups){
			users.addAll(getGroupAdmins(g));
		}
		return users.contains(user)?true:hasGroupPermission(user, groupManagerWithJob.getJobDescriptor(jobId).getX().getGroupId());
	}
	
	@Override
	public Boolean hasActionPermission(String user, String jobId) {
		if(Super.getSupers().contains(user)){
			//超级管理员
			return true;
		}
		Set<String> groups=new HashSet<String>();
		JobBean jobBean=groupManagerWithAction.getUpstreamJobBean(jobId);
		if(user.equals(jobBean.getJobDescriptor().getOwner())){
			//任务所有人
			return true;
		}
		GroupBean gb=jobBean.getGroupBean();
		while(gb!=null){
			groups.add(gb.getGroupDescriptor().getId());
			gb=gb.getParentGroupBean();
		}
		Set<String> users=new HashSet<String>();
		users.addAll(getJobAdmins(jobBean.getJobDescriptor().getJobId()));
		for(String g:groups){
			users.addAll(getGroupAdmins(g));
		}
		return users.contains(user)?true:hasGroupPermission(user, groupManagerWithAction.getActionDescriptor(jobId).getX().getGroupId());
	}
	
	@Override
	public void removeGroupAdmin(String user, String groupId) {
		ZeusPermission pp=getGroupPermission(user, groupId);
		if(pp!=null){
			zeusPermissionMapper.deleteByPrimaryKey(pp.getId());
		}
	}
	@Override
	public void removeJobAdmin(String user, String jobId) {
		ZeusPermission pp=getJobPermission(user, jobId);
		if(pp!=null){
			zeusPermissionMapper.deleteByPrimaryKey(pp.getId());
		}
	}

}