package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.mapper.ZeusActionMapper;
import com.taobao.zeus.dal.mapper.ZeusGroupMapper;
import com.taobao.zeus.dal.mapper.ZeusJobMapper;
import com.taobao.zeus.dal.model.*;
import com.taobao.zeus.dal.tool.*;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobDescriptor.JobRunType;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.processer.Processer;
import com.taobao.zeus.schedule.mvc.DebugInfoLog;
import com.taobao.zeus.util.DateUtil;
import com.taobao.zeus.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.*;

/**
 * 性能优化，防止每次都递归去查询mysql
 * @author zhoufang
 *
 */
@Repository("readOnlyGroupManagerWithAction")
public class ReadOnlyGroupManagerWithAction {

	@Autowired
	ZeusActionMapper zeusActionMapper;

	@Autowired
	ZeusJobMapper zeusJobMapper;

	@Autowired
	ZeusGroupMapper zeusGroupMapper;

	@Autowired
	@Qualifier("mysqlGroupManagerWithAction")
    GroupManagerWithAction groupManagerWithAction;
	
	private static final Logger log = LoggerFactory.getLogger(ReadOnlyGroupManagerWithAction.class);
	
	private Judge jobjudge=new Judge();
	private Judge groupjudge=new Judge();
	
	private Judge ignoreContentJobJudge=new Judge();
	private Judge ignoreContentGroupJudge=new Judge();
	
	/**完整的globe GroupBean*/
	private GroupBean globe;
	
	private GroupBean ignoreGlobe;
	
	private static final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
	/**
	 * Jobs或者Groups是否有变化，忽略脚本内容的改变(保证树形结构不变即可)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean isJobsAndGroupsChangedIgnoreContent(){
		//init
		final Judge ignoreContentJobJudge=this.ignoreContentJobJudge;
		final Judge ignoreContentGroupJudge=this.ignoreContentGroupJudge;
		final GroupBean ignoreGlobe=this.ignoreGlobe;

		boolean jobChanged;
		Judge jobrealtime=null;
		ZeusJobStatistic statistic = zeusActionMapper.selectActionStatistic();
		if (statistic!=null){
			jobrealtime=new Judge();
			jobrealtime.count=statistic.getCnt();
			jobrealtime.maxId=statistic.getJobId()==null?0:statistic.getJobId();
			jobrealtime.lastModified=statistic.getGmtModified()==null?new Date(0):statistic.getGmtModified();
			jobrealtime.stamp=new Date();
		}

		List<JobDescriptor> changedJobs=new ArrayList<JobDescriptor>();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("gmtModified", DateUtil.date2String(ignoreContentJobJudge.lastModified));
		List<ZeusAction> list =zeusActionMapper.selectGreatThanGmtModified(params);

		for(ZeusAction o:list){
			JobDescriptor jd=new JobDescriptor();
			jd.setId(String.valueOf(o.getId()));
			jd.setGroupId(String.valueOf(o.getGroupId()));
			changedJobs.add(jd);
		}

		if(jobrealtime!=null && jobrealtime.count.equals(ignoreContentJobJudge.count) && jobrealtime.maxId.equals(ignoreContentJobJudge.maxId)
				&& isAllJobsNotChangeParent(ignoreGlobe, changedJobs)){
			ignoreContentJobJudge.stamp=new Date();
			ignoreContentJobJudge.lastModified=jobrealtime.lastModified;
			jobChanged= false;
		}else{
			this.ignoreContentJobJudge=jobrealtime;
			jobChanged= true;
		}

		//Group
		boolean groupChanged;
		Judge grouprealtime=null;
		ZeusGroupStatistic groupStatistic = zeusGroupMapper.selectGroupStatistic();
		if (groupStatistic!=null){
			grouprealtime=new Judge();
			grouprealtime.count=groupStatistic.getCnt();
			grouprealtime.maxId=groupStatistic.getId()==null?0:groupStatistic.getId();
			grouprealtime.lastModified=groupStatistic.getGmtModified()==null?new Date(0):groupStatistic.getGmtModified();
			grouprealtime.stamp=new Date();
		}

		Map<String,Object> groupParams = new HashMap<String,Object>();
		groupParams.put("gmtModified", DateUtil.date2String(ignoreContentGroupJudge.lastModified));
		List<ZeusGroupWithBLOBs> zeusGroups = zeusGroupMapper.selectGreatThanModified(groupParams);
		List<GroupDescriptor> changedGroups=new ArrayList<GroupDescriptor>();
		for(ZeusGroupWithBLOBs p:zeusGroups){
			changedGroups.add(PersistenceAndBeanConvertWithAction.convert(p));
		}

		if(grouprealtime!=null && grouprealtime.count.equals(ignoreContentGroupJudge.count) && grouprealtime.maxId.equals(ignoreContentGroupJudge.maxId)
				&& isAllGroupsNotChangeParent(ignoreGlobe, changedGroups)){
			ignoreContentGroupJudge.stamp=new Date();
			groupChanged= false;
		}else{
			this.ignoreContentGroupJudge=grouprealtime;
			groupChanged= true;
		}

        log.info("readonlyGroupManagerWithAction -> isJobsAndGroupsChangedIgnoreContent jobChanged: " + jobChanged
		+ " groupChanged :"+ groupChanged);
		return jobChanged || groupChanged;
	}
	/**
	 * 判断变动的Job中，是否全部不涉及parent节点的变化
	 * @param gb
	 * @param list
	 * @return
	 */
	private boolean isAllJobsNotChangeParent(GroupBean gb,List<JobDescriptor> list){
		Map<String, JobBean> allJobs=gb.getAllSubJobBeans();
		for(JobDescriptor jd:list){
			JobBean bean=allJobs.get(jd.getId());
			if(bean==null){
				DebugInfoLog.info("isAllJobsNotChangeParent job id="+ jd.getId()+" has changed");
				return false;
			}
			JobDescriptor old=bean.getJobDescriptor();
			if(!old.getGroupId().equals(jd.getGroupId())){
				DebugInfoLog.info("isAllJobsNotChangeParent job id="+ jd.getId()+" has changed");
				return false;
			}
		}
		return true;
	}
	private boolean isGroupsNotChangeExisted(Map<String, GroupBean> allGroups,List<GroupDescriptor> list){
		for(GroupDescriptor tmp:list){
			GroupBean bean=allGroups.get(tmp.getId());
			if (bean!=null && bean.isExisted()!=tmp.isExisted()) {
				return false;
			}
		}
		return true;
	}

	private boolean isAllGroupsNotChangeThese(GroupBean gb,List<GroupDescriptor> list){
		Map<String, GroupBean> allGroups=gb.getAllSubGroupBeans();
		for(GroupDescriptor gd:list){
			GroupBean bean=allGroups.get(gd.getId());
			if(gd.getId().equals(gb.getGroupDescriptor().getId())){
				break;
			}
			if(bean==null){
				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
				return false;
			}
			GroupDescriptor old=bean.getGroupDescriptor();
			if(!old.getParent().equals(gd.getParent())){
				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
				return false;
			}
		}
		return isGroupsNotChangeExisted(allGroups,list);
	}
	/**
	 * 判断变动的Group中，是否全部不涉及parent节点的变化
	 * @param gb
	 * @param list
	 * @return
	 */
	private boolean isAllGroupsNotChangeParent(GroupBean gb,List<GroupDescriptor> list){
		Map<String, GroupBean> allGroups=gb.getAllSubGroupBeans();
		for(GroupDescriptor gd:list){
			GroupBean bean=allGroups.get(gd.getId());
			if(gd.getId().equals(gb.getGroupDescriptor().getId())){
				break;
			}
			if(bean==null){
				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
				return false;
			}
			GroupDescriptor old=bean.getGroupDescriptor();
			if(!old.getParent().equals(gd.getParent())){
				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Jobs或者Groups是否有变化
	 * 判断标准：同时满足以下条件
	 * 1.max id 一致
	 * 2.count 数一致
	 * 3.last_modified 一致
	 * @return
	 */
	private boolean isJobsAndGroupsChanged(){
		//init
		final Judge jobjudge=this.jobjudge;
		final Judge groupjudge=this.groupjudge;
		
		boolean jobChanged;
		Judge jobrealtime=null;
		ZeusJobStatistic item= zeusActionMapper.selectActionStatistic();
		if(item!=null){
			jobrealtime=new Judge();
			jobrealtime.count=item.getCnt();
			jobrealtime.maxId=item.getJobId();
			jobrealtime.lastModified=item.getGmtModified();
			jobrealtime.stamp=new Date();
		}

		if(jobrealtime!=null && jobrealtime.count.equals(jobjudge.count) && jobrealtime.maxId.equals(jobjudge.maxId) && jobrealtime.lastModified.equals(jobjudge.lastModified)){
			jobjudge.stamp=new Date();
			jobChanged= false;
		}else{
			this.jobjudge=jobrealtime;
			jobChanged= true;
		}
		
		boolean groupChanged;
		Judge grouprealtime=null;
		ZeusGroupStatistic groupStatistic = zeusGroupMapper.selectGroupStatistic();
		if(groupStatistic!=null){
			grouprealtime=new Judge();
			grouprealtime.count=groupStatistic.getCnt();
			grouprealtime.maxId=groupStatistic.getId();
			grouprealtime.lastModified=groupStatistic.getGmtModified();
			grouprealtime.stamp=new Date();
		}

		if(grouprealtime!=null && grouprealtime.count.equals(groupjudge.count) && grouprealtime.maxId.equals(groupjudge.maxId) && grouprealtime.lastModified.equals(groupjudge.lastModified)){
			groupjudge.stamp=new Date();
			groupChanged= false;
		}else{
			this.groupjudge=grouprealtime;
			groupChanged= true;
		}
		return jobChanged || groupChanged;
	}

	public synchronized GroupBean getGlobeGroupBean() {
		if(globe!=null){
			if(!isJobsAndGroupsChanged()){
				return globe;
			}
		}
		globe=new ReadOnlyGroupManagerAssembly(groupManagerWithAction).getGlobeGroupBean();
		return globe;
	}

	/**
	 * 为Tree展示提供的方法，每次都返回Copy对象，可以对返回结果进行引用修改
	 * @return
	 */
	public synchronized GroupBean getGlobeGroupBeanForTreeDisplay(boolean copy){
		if(ignoreGlobe==null || isJobsAndGroupsChangedIgnoreContent()  ){
			ignoreGlobe=new ReadOnlyGroupManagerAssembly(groupManagerWithAction).getGlobeGroupBean();
		}
		if(copy){
			return GroupManagerToolWithAction.buildGlobeGroupBeanWithoutDepend(new CopyGroupManagerAssembly(ignoreGlobe));
		}else{
			return ignoreGlobe;
		}
	}
	/**
	 * 为Tree展示提供的方法，每次都返回Copy对象，可以对返回结果进行引用修改
	 * @return
	 */
	public synchronized GroupBean getGlobeGroupBeanForTreeDisplayNew(boolean copy){
		if(ignoreGlobe==null || isJobsAndGroupsChangedIgnoreContent()  ){
			ignoreGlobe=new ReadOnlyGroupManagerAssembly(groupManagerWithAction).getGlobeGroupBean();
		}
		if(copy){
			return GroupManagerToolWithAction.buildGlobeGroupBeanWithoutDepend(new CopyGroupManagerAssembly(ignoreGlobe));
		}else{
			return ignoreGlobe;
		}
	}

	
	public GroupBean getCopyGlobeGroupBean(){
		GroupBean gb=getGlobeGroupBean();
		return GroupManagerToolWithAction.buildGlobeGroupBean(new CopyGroupManagerAssembly(gb));
	}
	
	private class CopyGroupManagerAssembly extends ReadOnlyGroupManagerAssembly{
		private GroupBean globe;
		public CopyGroupManagerAssembly(GroupBean globe) {
			super(null);
			this.globe=globe;
		}
		@Override
		public String getRootGroupId() {
			return globe.getGroupDescriptor().getId();
		}
		@Override
		public List<GroupDescriptor> getChildrenGroup(String groupId) {
			List<GroupBean> list=null;
			if(globe.getGroupDescriptor().getId().equals(groupId)){
				list=globe.getChildrenGroupBeans();
			}else{
				list=globe.getAllSubGroupBeans().get(groupId).getChildrenGroupBeans();
			}
			List<GroupDescriptor> result=new ArrayList<GroupDescriptor>();
			if(list!=null){
				for(GroupBean gb:list){
					result.add(gb.getGroupDescriptor());
				}
			}
			return result;
		}
		@Override
		public GroupDescriptor getGroupDescriptor(String groupId) {
			if(globe.getGroupDescriptor().getId().equals(groupId)){
				return globe.getGroupDescriptor();
			}else{
				return globe.getAllSubGroupBeans().get(groupId).getGroupDescriptor();
			}
		}
		@Override
		public List<Tuple<JobDescriptor, JobStatus>> getChildrenAction(
				String groupId) {
			Map<String, JobBean> map=globe.getAllSubGroupBeans().get(groupId).getJobBeans();
			List<Tuple<JobDescriptor, JobStatus>> result=new ArrayList<Tuple<JobDescriptor,JobStatus>>();
			for(JobBean jb:map.values()){
				result.add(new Tuple<JobDescriptor, JobStatus>(jb.getJobDescriptor(), jb.getJobStatus()));
			}
			return result;
		}
	}
	
	private class ReadOnlyGroupManagerAssembly implements GroupManagerWithAction {
		private GroupManagerWithAction groupManager;
		public ReadOnlyGroupManagerAssembly(GroupManagerWithAction gm){
			this.groupManager=gm;
		}
		@Override
		public GroupDescriptor createGroup(String user, String groupName,
				String parentGroup, boolean isDirectory) throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public JobDescriptor createAction(String user, String jobName,
										  String parentGroup, JobRunType jobType) throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void deleteGroup(String user, String groupId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void deleteAction(String user, String jobId) throws ZeusException {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<GroupDescriptor> getChildrenGroup(String groupId) {
			List<GroupDescriptor> list= groupManager.getChildrenGroup(groupId);
			List<GroupDescriptor> result=new ArrayList<GroupDescriptor>();
			for(GroupDescriptor gd:list){
				result.add(new ReadOnlyGroupDescriptor(gd));
			}
			return result;
		}

		@Override
		public List<Tuple<JobDescriptor, JobStatus>> getChildrenAction(
				String groupId) {
			List<Tuple<JobDescriptor, JobStatus>> list=groupManager.getChildrenAction(groupId);
			List<Tuple<JobDescriptor, JobStatus>> result=new ArrayList<Tuple<JobDescriptor,JobStatus>>();
			for(Tuple<JobDescriptor, JobStatus> tuple:list){
				Tuple<JobDescriptor, JobStatus> t=new Tuple<JobDescriptor, JobStatus>(new ReadOnlyJobDescriptor(tuple.getX()),new ReadOnlyJobStatus(tuple.getY()));
				result.add(t);
			}
			return result;
		}

		@Override
		public GroupBean getDownstreamGroupBean(String groupId) {
			ReadOnlyGroupDescriptor readGd=null;
			GroupDescriptor group=getGroupDescriptor(groupId);
			if(group instanceof ReadOnlyGroupDescriptor){
				readGd=(ReadOnlyGroupDescriptor) group;
			}else{
				readGd=new ReadOnlyGroupDescriptor(group);
			}
			GroupBean result=new GroupBean(readGd);
			return getDownstreamGroupBean(result);
		}

		@Override
		public GroupBean getDownstreamGroupBean(GroupBean parent) {
			try {
				return getDownstreamGroupBean(parent, 99).get(10,TimeUnit.SECONDS);
			} catch (Exception e) {
				log.error("getDownstreamGroupBean failed", e);
				return null;
			}
		}
		
		private Future<GroupBean> getDownstreamGroupBean(final GroupBean parent, final int depth) throws Exception{
			Callable<GroupBean> callable = new Callable<GroupBean>(){

				@Override
				public GroupBean call() throws Exception {
					if(parent.isDirectory()){
						List<GroupDescriptor> children=getChildrenGroup(parent.getGroupDescriptor().getId());
						ArrayList<Future<GroupBean>> futures = new ArrayList<Future<GroupBean>>(children.size());
						for(GroupDescriptor child:children){
							ReadOnlyGroupDescriptor readGd=null;
							if(child instanceof ReadOnlyGroupDescriptor){
								readGd=(ReadOnlyGroupDescriptor) child;
							}else{
								readGd=new ReadOnlyGroupDescriptor(child);
							}
							GroupBean childBean=new GroupBean(readGd);
							if(pool.getActiveCount()<15) {
								futures.add(getDownstreamGroupBean(childBean, 99));
							}else{
								getDownstreamGroupBean(childBean, 0);
							}
							childBean.setParentGroupBean(parent);
							parent.getChildrenGroupBeans().add(childBean);
						}
						for(Future<GroupBean> f:futures){
							f.get(10,TimeUnit.SECONDS);
						}
					}else{
						List<Tuple<JobDescriptor, JobStatus>> jobs= getChildrenAction(parent.getGroupDescriptor().getId());
						for(Tuple<JobDescriptor, JobStatus> tuple:jobs){
							JobBean jobBean=new JobBean(tuple.getX(),tuple.getY());
							jobBean.setGroupBean(parent);
							parent.getJobBeans().put(tuple.getX().getId(), jobBean);
						}
					}
					return parent;
				}
			};
			if(depth>0) {
				return pool.submit(callable);
			}else{
				callable.call();
				return new Future<GroupBean>() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {return false;}
					@Override
					public boolean isCancelled() {return false;}
					@Override
					public boolean isDone() {return false;}
					@Override
					public GroupBean get() throws InterruptedException,
							ExecutionException {return null;}
					@Override
					public GroupBean get(long timeout, TimeUnit unit)
							throws InterruptedException, ExecutionException,
							TimeoutException {
						return parent;
					}
				};
			}
		}

		@Override
		public GroupBean getGlobeGroupBean() {
			return GroupManagerToolWithAction.buildGlobeGroupBean(this);
		}

		@Override
		public GroupDescriptor getGroupDescriptor(String groupId) {
			return new ReadOnlyGroupDescriptor(groupManager.getGroupDescriptor(groupId));
		}

		@Override
		public Tuple<JobDescriptor, JobStatus> getActionDescriptor(String jobId) {
			return groupManager.getActionDescriptor(jobId);
		}

		@Override
		public Map<String, Tuple<JobDescriptor, JobStatus>> getActionDescriptor(
				Collection<String> jobIds) {
			return groupManager.getActionDescriptor(jobIds);
		}

		@Override
		public JobStatus getActionStatus(String jobId) {
			return new ReadOnlyJobStatus(groupManager.getActionStatus(jobId));
		}

		@Override
		public String getRootGroupId() {
			return groupManager.getRootGroupId();
		}

		@Override
		public GroupBean getUpstreamGroupBean(String groupId) {
			return GroupManagerToolWithAction.getUpstreamGroupBean(groupId, this);
		}

		@Override
		public JobBean getUpstreamJobBean(String jobId) {
			return GroupManagerToolWithAction.getUpstreamJobBean(jobId, this);
		}
		@Override
		public void grantGroupOwner(String granter, String uid, String groupId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void grantJobOwner(String granter, String uid, String jobId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void moveGroup(String uid, String groupId,
				String newParentGroupId) throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void moveJob(String uid, String jobId, String groupId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void updateGroup(String user, GroupDescriptor group)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void updateAction(String user, JobDescriptor job)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void updateActionStatus(JobStatus jobStatus) {
			throw new UnsupportedOperationException();
		}
		@Override
		public List<String> getHosts() throws ZeusException {
			return Collections.emptyList();
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
			// TODO Auto-generated method stub
			
		}
		@Override
		public List<ZeusActionWithBLOBs> getLastJobAction(String jobId){
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void updateAction(JobDescriptor actionPer) throws ZeusException {
			// TODO Auto-generated method stub
			
		}
		@Override
		public List<Tuple<JobDescriptor, JobStatus>> getActionList(String jobId) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removeAction(Long actionId) throws ZeusException {
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean IsExistedBelowRootGroup(String GroupName) {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * 不可变的GroupDescriptor类
	 * @author zhoufang
	 *
	 */
	public class ReadOnlyGroupDescriptor extends GroupDescriptor{
		private static final long serialVersionUID = 1L;
		private GroupDescriptor gd;
		public ReadOnlyGroupDescriptor(GroupDescriptor gd){
			this.gd=gd;
		}
		@Override
		public String getDesc() {
			return gd.getDesc();
		}

		@Override
		public String getId() {
			return gd.getId();
		}

		@Override
		public String getName() {
			return gd.getName();
		}

		@Override
		public String getOwner() {
			return gd.getOwner();
		}

		@Override
		public boolean isExisted(){
			return gd.isExisted();
		}

		@Override
		public boolean isDirectory() {
			return gd.isDirectory();
		}

		@Override
		public void setDesc(String desc) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setOwner(String owner) {
			throw new UnsupportedOperationException();
		}


		@Override
		public String getParent() {
			return gd.getParent();
		}

		@Override
		public void setParent(String parent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Map<String, String>> getResources() {
			List<Map<String, String>> list=gd.getResources();
			List<Map<String, String>> result=new ArrayList<Map<String,String>>();
			for(Map<String, String> map:list){
				result.add(new HashMap<String, String>(map));
			}
			return result;
		}

		@Override
		public void setResources(List<Map<String, String>> resources) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setId(String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDirectory(boolean directory) {
			throw new UnsupportedOperationException();
		}


		@Override
		public Map<String, String> getProperties() {
			return new HashMap<String, String>(gd.getProperties());
		}

		@Override
		public void setProperties(Map<String, String> properties) {
			throw new UnsupportedOperationException();
		}
		
	}
	/**
	 * 不可变JobDescriptor类
	 * @author zhoufang
	 *
	 */
	public class ReadOnlyJobDescriptor extends JobDescriptor{
		private static final long serialVersionUID = 1L;
		private JobDescriptor jd;
		public ReadOnlyJobDescriptor(JobDescriptor jd){
			this.jd=jd;
		}
		@Override
		public List<Map<String, String>> getResources() {
			List<Map<String, String>> list=jd.getResources();
			List<Map<String, String>> result=new ArrayList<Map<String,String>>();
			for(Map<String, String> map:list){
				result.add(new HashMap<String, String>(map));
			}
			return result;
		}
		@Override
		public String getCronExpression() {
			return jd.getCronExpression();
		}

		@Override
		public List<String> getDependencies() {
			return new ArrayList<String>(jd.getDependencies());
		}

		@Override
		public String getDesc() {
			return jd.getDesc();
		}

		@Override
		public String getGroupId() {
			return jd.getGroupId();
		}

		@Override
		public String getId() {
			return jd.getId();
		}

		@Override
		public JobRunType getJobType() {
			return jd.getJobType();
		}

		@Override
		public String getName() {
			return jd.getName();
		}

		@Override
		public String getOwner() {
			return jd.getOwner();
		}


		@Override
		public JobScheduleType getScheduleType() {
			return jd.getScheduleType();
		}

		@Override
		public boolean hasDependencies() {
			return !jd.getDependencies().isEmpty();
		}

		@Override
		public void setCronExpression(String cronExpression) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDependencies(List<String> depends) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDesc(String desc) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setJobType(JobRunType type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setOwner(String owner) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setScheduleType(JobScheduleType type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setGroupId(String groupId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setId(String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setResources(List<Map<String, String>> resources) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getProperties() {
			return new HashMap<String, String>(jd.getProperties());
		}

		@Override
		public void setProperties(Map<String, String> properties) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Boolean getAuto() {
			return jd.getAuto();
		}

		@Override
		public void setAuto(Boolean auto) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getScript() {
			return jd.getScript();
		}

		@Override
		public void setScript(String script) {
			throw new UnsupportedOperationException();
		}
		@Override
		public List<Processer> getPreProcessers() {
			return new ArrayList<Processer>(jd.getPreProcessers());
		}

		@Override
		public void setPreProcessers(List<Processer> preProcessers) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Processer> getPostProcessers() {
			return new ArrayList<Processer>(jd.getPostProcessers());
		}

		@Override
		public void setPostProcessers(List<Processer> postProcessers) {
			throw new UnsupportedOperationException();
		}
	}
	/**
	 * 不可变JobStatus类
	 * @author zhoufang
	 *
	 */
	public class ReadOnlyJobStatus extends JobStatus{
		private static final long serialVersionUID = 1L;
		private JobStatus jobStatus;
		public ReadOnlyJobStatus(JobStatus js){
			jobStatus=js;
		}
		@Override
		public String getJobId(){
			return jobStatus.getJobId();
		}
		@Override
		public void setJobId(String jobId){
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Status getStatus(){
			return jobStatus.getStatus();
		}
		@Override
		public void setStatus(Status status){
			throw new UnsupportedOperationException();
		}
		@Override
		public Map<String, String> getReadyDependency() {
			return new HashMap<String, String>(jobStatus.getReadyDependency());
		}
		@Override
		public void setReadyDependency(Map<String, String> readyDependency){
			throw new UnsupportedOperationException();
		}
		@Override
		public String getHistoryId() {
			return jobStatus.getHistoryId();
		}
		@Override
		public void setHistoryId(String historyId) {
			throw new UnsupportedOperationException();
		}
	}

}
