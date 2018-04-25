package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.GroupManagerWithJob;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.mapper.ZeusActionMapper;
import com.taobao.zeus.dal.mapper.ZeusGroupMapper;
import com.taobao.zeus.dal.mapper.ZeusJobMapper;
import com.taobao.zeus.dal.mapper.ZeusWorkerMapper;
import com.taobao.zeus.dal.model.*;
import com.taobao.zeus.dal.tool.*;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.processer.DownloadProcesser;
import com.taobao.zeus.model.processer.Processer;
import com.taobao.zeus.util.Tuple;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;

@SuppressWarnings("unchecked")
@Repository("mysqlGroupManagerWithJob")
public class MysqlGroupManagerWithJob implements GroupManagerWithJob {
	private static Logger log = LoggerFactory.getLogger(MysqlGroupManagerWithJob.class);
	@Autowired
	ZeusGroupMapper zeusGroupMapper;

	@Autowired
	ZeusActionMapper zeusActionMapper;

	@Autowired
	ZeusWorkerMapper zeusWorkerMapper;

	@Autowired
	ZeusJobMapper zeusJobMapper;

	@Autowired
	UserManager userManager;


	@Autowired
	private JobValidateWithJob jobValidateWithJob;
	@Override
	public void deleteGroup(String user, String groupId) throws ZeusException {
		GroupBean group = getDownstreamGroupBean(groupId);
		if (group.isDirectory()) {
//			if (!group.getChildrenGroupBeans().isEmpty()) {
//				throw new ZeusException("该组下不为空，无法删除");
//			}
			boolean candelete = true;
			for (GroupBean child : group.getChildrenGroupBeans()) {
				if (child.isExisted()) {
					candelete = false;
					break;
				}
			}
			if (!candelete) {
				throw new ZeusException("该组下不为空，无法删除");
			}
		} else {
			if (!group.getJobBeans().isEmpty()) {
				throw new ZeusException("该组下不为空，无法删除");
			}
		}
		ZeusGroupWithBLOBs object = zeusGroupMapper.selectByPrimaryKey(Integer.valueOf(groupId));
		object.setExisted(0);
		object.setGmtModified(new Date());
		zeusGroupMapper.updateByPrimaryKeySelective(object);
	}

	@Override
	public void deleteJob(String user, String jobId) throws ZeusException {
		GroupBean root = getGlobeGroupBean();
		JobBean job = root.getAllSubJobBeans().get(jobId);
		if (!job.getDepender().isEmpty()) {
			List<String> deps = new ArrayList<String>();
			for (JobBean jb : job.getDepender()) {
				deps.add(jb.getJobDescriptor().getId());
			}
			throw new ZeusException("该Job正在被其他Job" + deps.toString()
					+ "依赖，无法删除");
		}
		zeusJobMapper.deleteByPrimaryKey(Long.valueOf(jobId));
	}

	@Override
	public GroupBean getDownstreamGroupBean(String groupId) {
		GroupDescriptor group = getGroupDescriptor(groupId);
		GroupBean result = new GroupBean(group);
		return getDownstreamGroupBean(result);
	}

	@Override
	public GroupBean getDownstreamGroupBean(GroupBean parent) {
		if (parent.isDirectory()) {
			List<GroupDescriptor> children = getChildrenGroup(parent
					.getGroupDescriptor().getId());
			for (GroupDescriptor child : children) {
				GroupBean childBean = new GroupBean(child);
				getDownstreamGroupBean(childBean);
				childBean.setParentGroupBean(parent);
				parent.getChildrenGroupBeans().add(childBean);
			}
		} else {
			List<Tuple<JobDescriptor, JobStatus>> jobs = getChildrenJob(parent
					.getGroupDescriptor().getId());
			for (Tuple<JobDescriptor, JobStatus> tuple : jobs) {
				JobBean JobBeanOld = new JobBean(tuple.getX(), tuple.getY());
				JobBeanOld.setGroupBean(parent);
				parent.getJobBeans().put(tuple.getX().getId(), JobBeanOld);
			}
		}

		return parent;
	}

	@Override
	public GroupBean getGlobeGroupBean() {
		return GroupManagerToolWithJob.buildGlobeGroupBean(this);
	}

	/**
	 * 获取叶子组下所有的Job
	 * 
	 * @param groupId
	 * @return
	 */
	@Override
	public List<Tuple<JobDescriptor, JobStatus>> getChildrenJob(String groupId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("groupId", groupId);
		List<ZeusJobWithBLOBs> list = zeusJobMapper.selectByParams(params);
		List<Tuple<JobDescriptor, JobStatus>> result = new ArrayList<Tuple<JobDescriptor, JobStatus>>();
		if (list != null) {
			for (ZeusJobWithBLOBs j : list) {
				result.add(PersistenceAndBeanConvertWithJob.convert(j));
			}
		}
		return result;
	}

	/**
	 * 获取组的下级组列表
	 * 
	 * @param groupId
	 * @return
	 */
	@Override
	public List<GroupDescriptor> getChildrenGroup(String groupId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent", groupId);
		List<ZeusGroupWithBLOBs> list = zeusGroupMapper.findByParent(params);
		List<GroupDescriptor> result = new ArrayList<GroupDescriptor>();
		if (list != null) {
			for (ZeusGroupWithBLOBs p : list) {
				result.add(PersistenceAndBeanConvertWithJob.convert(p));
			}
		}
		return result;
	}

	@Override
	public GroupDescriptor getGroupDescriptor(String groupId) {
		ZeusGroupWithBLOBs persist = zeusGroupMapper.selectByPrimaryKey(Integer.valueOf(groupId));
		if (persist != null) {
			return PersistenceAndBeanConvertWithJob.convert(persist);
		}
		return null;
	}

	@Override
	public Tuple<JobDescriptor, JobStatus> getJobDescriptor(String jobId) {
		ZeusJobWithBLOBs persist = getJobPersistence(jobId);
		if (persist == null) {
			return null;
		}
		Tuple<JobDescriptor, JobStatus> t = PersistenceAndBeanConvertWithJob
				.convert(persist);
		JobDescriptor jd = t.getX();
		// 如果是周期任务，并且依赖不为空，则需要封装周期任务的依赖
		if (jd.getScheduleType() == JobDescriptor.JobScheduleType.CyleJob
				&& jd.getDependencies() != null) {
			ZeusJobWithBLOBs jp = null;
			for (String jobID : jd.getDependencies()) {
				if (StringUtils.isNotEmpty(jobID)) {
					jp = getJobPersistence(jobID);
					if(jp!=null){
						jd.getDepdCycleJob().put(jobID, jp.getCycle());
					}
				}
			}

		}
		return t;
	}

	private ZeusJobWithBLOBs getJobPersistence(String jobId) {
		ZeusJobWithBLOBs persist = zeusJobMapper.selectByPrimaryKey(Long.valueOf(jobId));
		if (persist == null) {
			return null;
		}
		return persist;
	}

	@Override
	public String getRootGroupId() {
		ZeusGroupWithBLOBs item = zeusGroupMapper.selectFirstOne();
		if (item == null) {
			ZeusGroupWithBLOBs persist = new ZeusGroupWithBLOBs();
			persist.setName("众神之神");
			persist.setOwner(ZeusUser.ADMIN.getUid());
			persist.setDirectory(0);
			zeusGroupMapper.insertSelective(persist);
			ZeusGroupWithBLOBs newItem = zeusGroupMapper.selectFirstOne();
			if (newItem.getId() == null) {
				return null;
			}
			return String.valueOf(newItem.getId());
		}
		return String.valueOf(item.getId());
	}

	@Override
	public GroupBean getUpstreamGroupBean(String groupId) {
		return GroupManagerToolWithJob.getUpstreamGroupBean(groupId, this);
	}

	@Override
	public JobBean getUpstreamJobBean(String jobId) {
		return GroupManagerToolWithJob.getUpstreamJobBean(jobId, this);
	}

	@Override
	public void updateGroup(String user, GroupDescriptor group)
			throws ZeusException {
		ZeusGroupWithBLOBs old = zeusGroupMapper.selectByPrimaryKey(Integer.valueOf(group.getId()));
		updateGroup(user, group, old.getOwner(), old.getParent() == null ? null
				: old.getParent().toString());
	}

	public void updateGroup(String user, GroupDescriptor group, String owner,
                            String parent) throws ZeusException {

		ZeusGroupWithBLOBs old = zeusGroupMapper.selectByPrimaryKey(Integer.valueOf(group.getId()));

		ZeusGroupWithBLOBs persist = PersistenceAndBeanConvertWithJob.convert(group);

		persist.setOwner(owner);
		if (parent != null) {
			persist.setParent(Integer.valueOf(parent));
		}

		// 以下属性不允许修改，强制采用老的数据
		persist.setDirectory(old.getDirectory());
		persist.setGmtCreate(old.getGmtCreate());
		persist.setGmtModified(new Date());
		persist.setExisted(old.getExisted());

		zeusGroupMapper.updateByPrimaryKeySelective(persist);
	}

	@Override
	public void updateJob(String user, JobDescriptor job) throws ZeusException {
		ZeusJobWithBLOBs orgPersist = zeusJobMapper.selectByPrimaryKey(Long.valueOf(job.getId()));
		updateJob(user, job, orgPersist.getOwner(), orgPersist.getGroupId()
				.toString());
	}

	public void updateJob(String user, JobDescriptor job, String owner,
			String groupId) throws ZeusException {
		ZeusJobWithBLOBs orgPersist = zeusJobMapper.selectByPrimaryKey(Long.valueOf(job.getId()));
		if (job.getScheduleType() == JobDescriptor.JobScheduleType.Independent) {
			job.setDependencies(new ArrayList<String>());
		} else if (job.getScheduleType() == JobDescriptor.JobScheduleType.Dependent) {
			job.setCronExpression("");
		}
		job.setOwner(owner);
		job.setGroupId(groupId);
		// 以下属性不允许修改，强制采用老的数据
		ZeusJobWithBLOBs persist = PersistenceAndBeanConvertWithJob.convert(job);
		persist.setGmtCreate(orgPersist.getGmtCreate());
		persist.setGmtModified(new Date());
		persist.setRunType(orgPersist.getRunType());
		persist.setStatus(orgPersist.getStatus());
		persist.setReadyDependency(orgPersist.getReadyDependency());
		persist.setHost(job.getHost());
		persist.setHostGroupId(Integer.valueOf(job.getHostGroupId()));
		// 如果是用户从界面上更新，开始时间、统计周期等均为空，用原来的值
		if (job.getStartTime() == null || "".equals(job.getStartTime())) {
			persist.setStartTime(orgPersist.getStartTime());
		}
		if (job.getStartTimestamp() == 0) {
			persist.setStartTimestamp(orgPersist.getStartTimestamp());
		}
		if (job.getStatisStartTime() == null
				|| "".equals(job.getStatisStartTime())) {
			persist.setStatisStartTime(orgPersist.getStatisStartTime());
		}
		if (job.getStatisEndTime() == null || "".equals(job.getStatisEndTime())) {
			persist.setStatisEndTime(orgPersist.getStatisEndTime());
		}

		// 如果是周期任务，则许检查依赖周期是否正确
		if (JobDescriptor.JobScheduleType.CyleJob.equals(job.getScheduleType())
				&& job.getDependencies() != null
				&& job.getDependencies().size() != 0) {
			List<JobDescriptor> list = this.getJobDescriptors(job
					.getDependencies());
			jobValidateWithJob.checkCycleJob(job, list);
		}

		if (jobValidateWithJob.valide(job)) {
			zeusJobMapper.updateByPrimaryKeySelective(persist);
		}

	}


	@Override
	public GroupDescriptor createGroup(String user, String groupName,
                                       String parentGroup, boolean isDirectory) throws ZeusException {
		if (parentGroup == null) {
			throw new ZeusException("parent group may not be null");
		}
		GroupDescriptor group = new GroupDescriptor();
		group.setOwner(user);
		group.setName(groupName);
		group.setParent(parentGroup);
		group.setDirectory(isDirectory);

		GroupValidate.valide(group);

		ZeusGroupWithBLOBs persist = PersistenceAndBeanConvertWithJob.convert(group);
		Date now = new Date();
		persist.setGmtCreate(now);
		persist.setGmtModified(now);
		persist.setExisted(1);
		zeusGroupMapper.insertSelective(persist);
		Map<String ,Object> params = new HashMap<String ,Object>();
		params.put("owner",user);
		params.put("name",groupName);
		params.put("parent",parentGroup);
		params.put("directory",isDirectory?0:1);
		params.put("existed",1);
		List<ZeusGroupWithBLOBs> result = zeusGroupMapper.selectByParams(params);
		return PersistenceAndBeanConvertWithJob.convert(result.get(0));
	}

	@Override
	public JobDescriptor createJob(String user, String jobName,
			String parentGroup, JobDescriptor.JobRunType jobType) throws ZeusException {
		GroupDescriptor parent = getGroupDescriptor(parentGroup);
		if (parent.isDirectory()) {
			throw new ZeusException("目录组下不得创建Job");
		}
		JobDescriptor job = new JobDescriptor();
		job.setOwner(user);
		job.setName(jobName);
		job.setGroupId(parentGroup);
		job.setJobType(jobType);
		job.setPreProcessers(Arrays.asList((Processer) new DownloadProcesser()));
		ZeusJobWithBLOBs persist = PersistenceAndBeanConvertWithJob.convert(job);
		persist.setGmtCreate(new Date());
		persist.setGmtModified(new Date());
		zeusJobMapper.insertSelective(persist);
		Map<String ,Object> params = new HashMap<String ,Object>();
		params.put("owner",user);
		params.put("name",jobName);
		params.put("groupId",parentGroup);
		params.put("runType",jobType);
		List<ZeusJobWithBLOBs> result = zeusJobMapper.selectByParams(params);
		return PersistenceAndBeanConvertWithJob.convert(result.get(0)).getX();
	}

	@Override
	public Map<String, Tuple<JobDescriptor, JobStatus>> getJobDescriptor(
			final Collection<String> jobIds) {
		List<Long> ids = new ArrayList<Long>();
		for (String i : jobIds) {
			ids.add(Long.valueOf(i));
		}
		List<ZeusJobWithBLOBs> list = zeusJobMapper.findJobWithIds(ids);
		List<Tuple<JobDescriptor, JobStatus>> result = new ArrayList<Tuple<JobDescriptor, JobStatus>>();
		if (list != null && !list.isEmpty()) {
			for (ZeusJobWithBLOBs persist : list) {
				result.add(PersistenceAndBeanConvertWithJob
						.convert(persist));
			}
		}

		Map<String, Tuple<JobDescriptor, JobStatus>> map = new HashMap<String, Tuple<JobDescriptor, JobStatus>>();
		for (Tuple<JobDescriptor, JobStatus> jd : result) {
			map.put(jd.getX().getId(), jd);
		}
		return map;
	}

	public List<JobDescriptor> getJobDescriptors(final Collection<String> jobIds) {
		List<Long> ids = new ArrayList<Long>();
		for (String i : jobIds) {
			if (StringUtils.isNotEmpty(i)) {
				ids.add(Long.valueOf(i));
			}
		}
		List<ZeusJobWithBLOBs> list = zeusJobMapper.findJobWithIds(ids);
		List<JobDescriptor> result = new ArrayList<JobDescriptor>();
		if (result != null && !result.isEmpty()) {
			for (ZeusJobWithBLOBs persist : list) {
				result.add(PersistenceAndBeanConvertWithJob.convert(
						persist).getX());
			}
		}
		return result;
	}

	@Override
	public void updateJobStatus(JobStatus jobStatus) {
		ZeusJobWithBLOBs persistence = getJobPersistence(jobStatus.getJobId());
		persistence.setGmtModified(new Date());

		// 只修改状态 和 依赖 2个字段
		ZeusJobWithBLOBs temp = PersistenceAndBeanConvertWithJob.convert(jobStatus);
		persistence.setStatus(temp.getStatus());
		persistence.setReadyDependency(temp.getReadyDependency());
		persistence.setHistoryId(temp.getHistoryId());

		zeusJobMapper.updateByPrimaryKeySelective(persistence);
	}

	@Override
	public JobStatus getJobStatus(String jobId) {
		Tuple<JobDescriptor, JobStatus> tuple = getJobDescriptor(jobId);
		if (tuple == null) {
			return null;
		}
		return tuple.getY();
	}

	@Override
	public void grantGroupOwner(String granter, String uid, String groupId)
			throws ZeusException {
		GroupDescriptor gd = getGroupDescriptor(groupId);
		if (gd != null) {
			updateGroup(granter, gd, uid, gd.getParent());
		}
	}

	@Override
	public void grantJobOwner(String granter, String uid, String jobId)
			throws ZeusException {
		Tuple<JobDescriptor, JobStatus> job = getJobDescriptor(jobId);
		if (job != null) {
			job.getX().setOwner(uid);
			updateJob(granter, job.getX(), uid, job.getX().getGroupId());
		}
	}

	@Override
	public void moveJob(String uid, String jobId, String groupId)
			throws ZeusException {
		JobDescriptor jd = getJobDescriptor(jobId).getX();
		GroupDescriptor gd = getGroupDescriptor(groupId);
		if (gd.isDirectory()) {
			throw new ZeusException("非法操作");
		}
		updateJob(uid, jd, jd.getOwner(), groupId);
	}

	@Override
	public void moveGroup(String uid, String groupId, String newParentGroupId)
			throws ZeusException {
		GroupDescriptor gd = getGroupDescriptor(groupId);
		GroupDescriptor parent = getGroupDescriptor(newParentGroupId);
		if (!parent.isDirectory()) {
			throw new ZeusException("非法操作");
		}
		updateGroup(uid, gd, gd.getOwner(), newParentGroupId);
	}

	@Override
	public List<String> getHosts() throws ZeusException {
		List<ZeusWorker> list = zeusWorkerMapper.selectAll();
		final List<String> results = new ArrayList<String>();
		list.stream().forEach(o -> results.add(o.getHost()));
		return results;
	}

	@Override
	public void replaceWorker(ZeusWorker worker) throws ZeusException {
		try {
			ZeusWorker item = zeusWorkerMapper.selectByPrimaryKey(worker.getHost());
			if (item!=null){
				zeusWorkerMapper.updateByPrimaryKeySelective(worker);
			}else
			{
				zeusWorkerMapper.insertSelective(worker);
			}
		} catch (DataAccessException e) {
			throw new ZeusException(e);
		}

	}

	@Override
	public void removeWorker(String host) throws ZeusException {
		try {
			zeusWorkerMapper.deleteByPrimaryKey(host);
		} catch (DataAccessException e) {
			throw new ZeusException(e);
		}

	}
	
	/**
	 * 获取All Jobs
	 * 
	 * @param
	 * @return
	 */
	@Override
	public List<ZeusJobWithBLOBs> getAllJobs() {
		List<ZeusJobWithBLOBs> list = zeusJobMapper.selectAll();
		return list;
	}
	
	@Override
	public List<String> getAllDependencied(String jobID) {
		List<ZeusJobWithBLOBs> jobs = getAllJobs();
		if( jobs == null || jobs.size() == 0) return null;
		Map<String, List<String>> allJobDependencied = new HashMap<String, List<String>>();
		for(ZeusJobWithBLOBs job : jobs){
			JobDescriptor jobd = PersistenceAndBeanConvertWithJob.convert(job).getX();
			if( jobd != null && jobd.hasDependencies()){
				List<String> deps = jobd.getDependencies();
				for(String dep : deps){
					List<String> depds = allJobDependencied.get(dep);
					if(depds == null){
						depds = new ArrayList<String>();
					}
					depds.add(job.getId().toString());
					allJobDependencied.put(dep, depds);
				}
			}
		}
		
		List<String> dependencied = new ArrayList<String>();
		Set<String> visited = new HashSet<String>();
		Queue<String> idQueue = new LinkedList<String>();
		idQueue.offer(jobID);
		visited.add(jobID);
		while (!idQueue.isEmpty()) {
			String id = idQueue.poll();
			List<String> depdList = allJobDependencied.get(id);
			if(depdList !=null && depdList.size() != 0){
				for (String depd : depdList) {
					if (!visited.contains(depd)) {
						visited.add(depd);
						idQueue.offer(depd);
						dependencied.add(depd);
					}
				}
			}
		}
		return dependencied;
	}

	@Override
	public List<String> getAllDependencies(String jobID) {
		JobDescriptor job = getJobDescriptor(jobID).getX();
		if(job == null || !job.hasDependencies()) return null;
		List<String> dependencies = new ArrayList<String>();
		Set<String> visited = new HashSet<String>();
		Queue<String> idQueue = new LinkedList<String>();
		idQueue.offer(jobID);
		visited.add(jobID);
		while (!idQueue.isEmpty()) {
			String id = idQueue.poll();
			JobDescriptor jb = getJobDescriptor(id).getX();
			if (jb != null && jb.hasDependencies()) {
				List<String> deps = jb.getDependencies();
				if (deps != null && deps.size() != 0) {
					for (String dep : deps) {
						if (!visited.contains(dep)) {
							visited.add(dep);
							idQueue.offer(dep);
							dependencies.add(dep);
						}
					}
				}
			}
		}
		return dependencies;
	}
	
	@Override
	public void updateActionList(JobDescriptor job) {
		ZeusJobWithBLOBs persist = PersistenceAndBeanConvertWithJob.convert(job);
		Long jobId = persist.getId();
/*		String script = persist.getScript();*/
		String resources = persist.getResources();
		String configs = persist.getConfigs();
		String host = persist.getHost();
		Integer workGroupId = persist.getHostGroupId();
		Integer auto = persist.getAuto();
		log.info("begin updateActionList.");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("jobId", jobId);
		List<ZeusActionWithBLOBs> actionList =zeusActionMapper.selectByJobId(params);
		log.info("finish query.");
		if (actionList != null && actionList.size() > 0 ){
			for(ZeusActionWithBLOBs actionPer : actionList){
//				if(!"running".equalsIgnoreCase(actionPer.getStatus())){
/*					actionPer.setScript(script);*/
					actionPer.setResources(resources);
					actionPer.setConfigs(configs);
					actionPer.setHost(host);
					actionPer.setGmtModified(new Date());
					actionPer.setHostGroupId(workGroupId);
					actionPer.setAuto(auto);
				zeusActionMapper.updateByPrimaryKeySelective(actionPer);
//				}
			}
			log.info("finish update " + actionList.size() + ".");
		}
	}
}