package com.taobao.zeus.dal.tool;

import com.taobao.zeus.dal.logic.GroupManagerWithJob;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GroupManagerToolWithJob {
	private static Logger log = LoggerFactory.getLogger(GroupManagerToolWithJob.class);
	
	public static GroupBean getUpstreamGroupBean(String groupId,GroupManagerWithJob groupManagerWithJob) {
		GroupDescriptor group=groupManagerWithJob.getGroupDescriptor(groupId);
		GroupBean result=new GroupBean(group);
		if(group.getParent()!=null){
			GroupBean parent=groupManagerWithJob.getUpstreamGroupBean(group.getParent());
			result.setParentGroupBean(parent);
		}
		return result;
	}
	/**
	 * 构建一个带完整依赖关系的树形节点网络
	 * @param
	 * @return
	 */
	public static GroupBean buildGlobeGroupBean(GroupManagerWithJob groupManagerWithJob) {
		GroupBean root=groupManagerWithJob.getDownstreamGroupBean(groupManagerWithJob.getRootGroupId());
		//构建依赖关系的网状结构
		//1.提取所有的GroupBean 和 JobBeanOld
//		structureDependNet(root, root.getAllGroupBeanMap(), root.getAllJobBeanMap());
		//2.将JobBean中的依赖关系在内存模型中关联起来
		Map<String, JobBean> allJobBeans=root.getAllSubJobBeans();
		for(JobBean j1:allJobBeans.values()){
			if(j1.getJobDescriptor().getScheduleType()== JobDescriptor.JobScheduleType.Dependent){
				for(String depId:j1.getJobDescriptor().getDependencies()){
					try {
						JobBean depJob=allJobBeans.get(depId);
						j1.addDependee(depJob);
						depJob.addDepender(j1);
					} catch (Exception e) {
						log.error("The jobid is " + j1.getJobDescriptor().getId() + ", the depId is " + depId);
					}
					
				}
			}
		}
		return root;
	}
	/**
	 * 构建一个树形节点网络，不包含Job之间的依赖关系对象引用
	 * @param
	 * @return
	 */
	public static GroupBean buildGlobeGroupBeanWithoutDepend(GroupManagerWithJob groupManagerWithJob) {
		GroupBean root=groupManagerWithJob.getDownstreamGroupBean(groupManagerWithJob.getRootGroupId());
		return root;
	}
	public static GroupBean getDownstreamGroupBean(String groupId,GroupManagerWithJob groupManagerWithJob) {
		GroupDescriptor group=groupManagerWithJob.getGroupDescriptor(groupId);
		GroupBean result=new GroupBean(group);
		return groupManagerWithJob.getDownstreamGroupBean(result);
	}
	
	public static GroupBean getDownstreamGroupBean(GroupBean parent,GroupManagerWithJob groupManagerWithJob) {
		if(parent.isDirectory()){
			List<GroupDescriptor> children=groupManagerWithJob.getChildrenGroup(parent.getGroupDescriptor().getId());
			for(GroupDescriptor child:children){
				GroupBean childBean=new GroupBean(child);
				groupManagerWithJob.getDownstreamGroupBean(childBean);
				childBean.setParentGroupBean(parent);
				parent.getChildrenGroupBeans().add(childBean);
			}
		}else{
			List<Tuple<JobDescriptor, JobStatus>> jobs=groupManagerWithJob.getChildrenJob(parent.getGroupDescriptor().getId());
			for(Tuple<JobDescriptor, JobStatus> tuple:jobs){
				JobBean JobBeanOld=new JobBean(tuple.getX(),tuple.getY());
				JobBeanOld.setGroupBean(parent);
				parent.getJobBeans().put(tuple.getX().getId(), JobBeanOld);
			}
		}
		
		return parent;
	}
	
	public static JobBean getUpstreamJobBean(String jobId,GroupManagerWithJob groupManagerWithJob) {
		Tuple<JobDescriptor, JobStatus> tuple=groupManagerWithJob.getJobDescriptor(jobId);
		if(tuple!=null){
			JobBean result=new JobBean(tuple.getX(),tuple.getY());
			result.setGroupBean(groupManagerWithJob.getUpstreamGroupBean(result.getJobDescriptor().getGroupId()));
			return result;
		}else{
			return null;
		}
	}
}
