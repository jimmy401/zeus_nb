package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.logic.FollowManagerWithJob;
import com.taobao.zeus.dal.logic.GroupManagerWithJob;
import com.taobao.zeus.dal.mapper.ZeusFollowUpMapper;
import com.taobao.zeus.dal.model.ZeusFollowUp;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.dal.tool.PersistenceAndBeanConvertWithAction;
import com.taobao.zeus.model.ZeusFollow;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@SuppressWarnings("unchecked")
@Repository("mysqlFollowManagerWithJob")
public class MysqlFollowManagerWithJob implements FollowManagerWithJob {
	@Autowired
	ZeusFollowUpMapper zeusFollowUpMapper;

	@Autowired
	@Qualifier("mysqlGroupManagerWithJob")
	private GroupManagerWithJob groupManagerWithJob;

	@Override
	public List<ZeusFollow> findAllTypeFollows(final String uid) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		List<ZeusFollowUp> list=zeusFollowUpMapper.selectByParams(params);

		List<ZeusFollow> result=new ArrayList<ZeusFollow>();
		if(list!=null){
			for(ZeusFollowUp persist:list){
				result.add(PersistenceAndBeanConvertWithAction.convert(persist));
			}
		}
		return result;
	}

	@Override
	public List<ZeusFollow> findFollowedGroups(final String uid) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("type", ZeusFollow.GroupType);
		List<ZeusFollowUp> list=zeusFollowUpMapper.selectByParams(params);

		List<ZeusFollow> result=new ArrayList<ZeusFollow>();
		if(list!=null){
			for(ZeusFollowUp persist:list){
				result.add(PersistenceAndBeanConvertWithAction.convert(persist));
			}
		}
		return result;
	}

	@Override
	public List<ZeusFollow> findFollowedJobs(final String uid) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", ZeusFollow.JobType);
		params.put("uid", uid);
		List<ZeusFollowUp> list = zeusFollowUpMapper.selectByParams(params);

		List<ZeusFollow> result=new ArrayList<ZeusFollow>();
		if(list!=null){
			for(ZeusFollowUp persist:list){
				result.add(PersistenceAndBeanConvertWithAction.convert(persist));
			}
		}
		return result;
	}

	@Override
	public List<ZeusFollow> findJobFollowers(final String jobId) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", ZeusFollow.JobType);
		params.put("targetId", Long.valueOf(jobId));
		List<ZeusFollowUp> list = zeusFollowUpMapper.selectByParams(params);

		List<ZeusFollow> result=new ArrayList<ZeusFollow>();
		if(list!=null){
			for(ZeusFollowUp persist:list){
				result.add(PersistenceAndBeanConvertWithAction.convert(persist));
			}
		}
		return result;
	}

	@Override
	public List<ZeusFollow> findGroupFollowers(final List<String> groupIds) {
		String targetIdString = StringUtils.join(groupIds.toArray(),",");
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", ZeusFollow.GroupType);
		params.put("targetIdString", targetIdString);
		List<ZeusFollowUp> list = zeusFollowUpMapper.selectByTargetIds(params);

		List<ZeusFollow> result=new ArrayList<ZeusFollow>();
		if(list!=null){
			for(ZeusFollowUp persist:list){
				result.add(PersistenceAndBeanConvertWithAction.convert(persist));
			}
		}
		return result;
	}

	@Override
	public ZeusFollow addFollow(final String uid, final Integer type, final String targetId) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("type",type);
		params.put("targetId", Long.valueOf(targetId));
		List<ZeusFollowUp> list = zeusFollowUpMapper.selectByParams(params);

		if(list!=null && !list.isEmpty()){
			ZeusFollow zf= PersistenceAndBeanConvertWithAction.convert(list.get(0));
			return zf;
		}
		ZeusFollowUp persist=new ZeusFollowUp();
		persist.setGmtCreate(new Date());
		persist.setGmtModified(new Date());
		persist.setTargetId(Long.valueOf(targetId));
		persist.setType(type);
		persist.setUid(uid);
		persist.setImportant(0);
		zeusFollowUpMapper.insertSelective(persist);

		persist = zeusFollowUpMapper.selectByParams(params).get(0);

		return PersistenceAndBeanConvertWithAction.convert(persist);
	}

	@Override
	public void deleteFollow(final String uid, final Integer type, final String targetId) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("type",type);
		params.put("targetId", Long.valueOf(targetId));
		List<ZeusFollowUp> list = zeusFollowUpMapper.selectByParams(params);

		if(list!=null && !list.isEmpty()){
			for(ZeusFollowUp persist:list){
				zeusFollowUpMapper.deleteByPrimaryKey(persist.getId());
			}
		}
	}

	@Override
	public List<String> findActualJobFollowers(String jobId) {
		List<ZeusFollow> jobFollows=findJobFollowers(jobId);
		JobBean jobBean=groupManagerWithJob.getUpstreamJobBean(jobId);

		List<String> groupIds=new ArrayList<String>();
		GroupBean gb=jobBean.getGroupBean();
		while(gb!=null){
			groupIds.add(gb.getGroupDescriptor().getId());
			gb=gb.getParentGroupBean();
		}
		List<ZeusFollow> groupFollows=findGroupFollowers(groupIds);

		List<String> follows=new ArrayList<String>();
		//任务创建人自动纳入消息通知人员名单
		follows.add(jobBean.getJobDescriptor().getOwner());
		for(ZeusFollow zf:jobFollows){
			if(!follows.contains(zf.getUid())){
				follows.add(zf.getUid());
			}
		}
		for(ZeusFollow zf:groupFollows){
			if(!follows.contains(zf.getUid())){
				follows.add(zf.getUid());
			}
		}
		return follows;
	}

	@Override
	public List<ZeusFollow> findAllFollowers(String jobId) {
		List<ZeusFollow> jobFollows=findJobFollowers(jobId);
		JobBean jobBean=groupManagerWithJob.getUpstreamJobBean(jobId);

		List<String> groupIds=new ArrayList<String>();
		GroupBean gb=jobBean.getGroupBean();
		while(gb!=null){
			groupIds.add(gb.getGroupDescriptor().getId());
			gb=gb.getParentGroupBean();
		}
		List<ZeusFollow> groupFollows=findGroupFollowers(groupIds);

		List<ZeusFollow> result = new ArrayList<ZeusFollow>();
		result.addAll(jobFollows);
		result.addAll(groupFollows);
		return result;
	}

	public void updateImportantContact(final String targetId,final String uid, int isImportant) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("type",ZeusFollow.JobType );
		params.put("targetId", Long.valueOf(targetId));
		List<ZeusFollowUp> list = zeusFollowUpMapper.selectByParams(params);

		if(list!=null && !list.isEmpty()){
			ZeusFollowUp persist=list.get(0);
			persist.setImportant(isImportant);
			persist.setGmtModified(new Date());
			zeusFollowUpMapper.updateByPrimaryKeySelective(persist);
		}
	}

	@Override
	public void grantImportantContact(String targetId, String uid) {
		updateImportantContact(targetId, uid, 1);
	}

	@Override
	public void revokeImportantContact(String targetId, String uid) {
		updateImportantContact(targetId, uid, 0);
	}	
}
