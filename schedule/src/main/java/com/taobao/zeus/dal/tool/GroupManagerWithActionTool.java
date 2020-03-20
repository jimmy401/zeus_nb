package com.taobao.zeus.dal.tool;

import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.model.ZeusGroupWithBLOBs;
import com.taobao.zeus.model.ActionDescriptor;
import com.taobao.zeus.model.ActionDescriptor.JobScheduleType;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GroupManagerWithActionTool {

	private static Logger log = LoggerFactory.getLogger(GroupManagerWithActionTool.class);

	public static GroupBean getUpstreamGroupBean(String groupId,GroupManagerWithAction groupManager) {
		ZeusGroupWithBLOBs group=groupManager.getGroupDescriptor(groupId);
		GroupBean result=new GroupBean(group);
		if(group.getParent()!=null){
			GroupBean parent=groupManager.getUpstreamGroupBean(group.getParent().toString());
			result.setParentGroupBean(parent);
		}
		return result;
	}
	/**
	 * 构建一个带完整依赖关系的树形节点网络
	 * @param groupManager
	 * @return
	 */
	public static GroupBean buildGlobeGroupBean(GroupManagerWithAction groupManager) {
		GroupBean root=groupManager.getDownstreamGroupBean(groupManager.getRootGroupId());
		//构建依赖关系的网状结构
		//1.提取所有的GroupBean 和 JobBean
//		structureDependNet(root, root.getAllGroupBeanMap(), root.getAllJobBeanMap());
		//2.将JobBean中的依赖关系在内存模型中关联起来
		Map<String, JobBean> allJobBeans=root.getAllSubJobBeans();
		for(JobBean j1:allJobBeans.values()){
			if(j1.getActionDescriptor().getScheduleType()==JobScheduleType.Dependent){
				for(String depId:j1.getActionDescriptor().getDependencies()){
					try{
						JobBean depJob=allJobBeans.get(depId);
						j1.addDependee(depJob);
						depJob.addDepender(j1);
					}catch(Exception e){
						log.error("the jobid is " + j1.getActionDescriptor().getId() + ", the depId is " + depId, e);
					}
				}
			}
		}
		return root;
	}

	public static GroupBean getDownstreamGroupBean(String groupId,GroupManagerWithAction groupManager) {
		ZeusGroupWithBLOBs group=groupManager.getGroupDescriptor(groupId);
		GroupBean result=new GroupBean(group);
		return groupManager.getDownstreamGroupBean(result);
	}

	public static GroupBean getDownstreamGroupBean(GroupBean parent,GroupManagerWithAction groupManager) {
		if(parent.isDirectory()){
			List<ZeusGroupWithBLOBs> children=groupManager.getChildrenGroup(parent.getGroupDescriptor().getId().toString());
			for(ZeusGroupWithBLOBs child:children){
				GroupBean childBean=new GroupBean(child);
				groupManager.getDownstreamGroupBean(childBean);
				childBean.setParentGroupBean(parent);
				parent.getChildrenGroupBeans().add(childBean);
			}
		}else{
			List<Tuple<ActionDescriptor, JobStatus>> jobs=groupManager.getChildrenAction(parent.getGroupDescriptor().getId().toString());
			for(Tuple<ActionDescriptor, JobStatus> tuple:jobs){
				JobBean jobBean=new JobBean(tuple.getX(),tuple.getY());
				jobBean.setGroupBean(parent);
				parent.getJobBeans().put(tuple.getX().getId(), jobBean);
			}
		}

		return parent;
	}

	public static JobBean getUpstreamJobBean(String jobId,GroupManagerWithAction groupManager) {
		Tuple<ActionDescriptor, JobStatus> tuple=groupManager.getActionDescriptor(jobId);
		if(tuple!=null){
			JobBean result=new JobBean(tuple.getX(),tuple.getY());
			result.setGroupBean(groupManager.getUpstreamGroupBean(result.getActionDescriptor().getGroupId()));
			return result;
		}else{
			return null;
		}
	}
}
