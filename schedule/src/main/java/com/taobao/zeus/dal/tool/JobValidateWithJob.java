package com.taobao.zeus.dal.tool;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.impl.TreeViewService;
import com.taobao.zeus.model.ActionDescriptor;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.*;

@Repository
public class JobValidateWithJob {
	@Autowired
	private TreeViewService treeViewService;

	public boolean valide(ActionDescriptor job) throws ZeusException {
		if(job.getJobType()==null){
			throw new ZeusException("任务类型必须填写");
		}
		if(job.getGroupId()==null){
			throw new ZeusException("所属组必须填写");
		}
		if(job.getJobType()== ActionDescriptor.JobRunType.MapReduce){
			if(job.getName()==null || job.getName().trim().equals("")){
				throw new ZeusException("name字段不能为空");
			}
			if(job.getAuto()){
				if(job.getProperties().get("java.main.class")==null ||
						job.getProperties().get("java.main.class").trim().equals("")){
					throw new ZeusException("必须填写Java Main类");
				}
				if(job.getScheduleType()==null){
					throw new ZeusException("调度类型必须填写");
				}
				if(job.getScheduleType()== ActionDescriptor.JobScheduleType.Independent){
					if(job.getCronExpression()==null || job.getCronExpression().trim().equals("")){
						throw new ZeusException("独立任务的定时表达式必须填写");
					}
					job.setDependencies(new ArrayList<String>());
				}
				//如果是依赖任务
				if(job.getScheduleType()== ActionDescriptor.JobScheduleType.Dependent){
					//必须填写依赖项
					if(job.getDependencies()==null || job.getDependencies().isEmpty()){
						throw new ZeusException("依赖任务必须填写依赖项");
					}
					job.setCronExpression("");
				}

			}
		}else if(job.getJobType()== ActionDescriptor.JobRunType.Shell){
			if(job.getScript()==null){
				throw new ZeusException("Shell 脚本不得为空");
			}
		}else if(job.getJobType()== ActionDescriptor.JobRunType.Hive){
			if(job.getScript()==null){
				throw new ZeusException("Hive 脚本不得为空");
			}
		}
		
		if(job.getCronExpression()!=null && !job.getCronExpression().trim().equals("")){
			try {
				new CronTrigger("test", "test", job.getCronExpression());
			} catch (ParseException e) {
				throw new ZeusException("cronExpression表达式格式出错");
			}
		}
		
		//检查依赖的死循环问题
		GroupBean root= treeViewService.buildGlobeGroupBeanWithRelation();
		Map<String, JobBean> allJobBeans=root.getAllSubJobBeans();
		Set<JobBean> deps=new HashSet<JobBean>();
		if(job.getScheduleType()== ActionDescriptor.JobScheduleType.Dependent){
			for(String jobId:job.getDependencies()){
				if(allJobBeans.get(jobId)==null){
					throw new ZeusException("依赖任务："+jobId+" 不存在");
				}
				deps.add(allJobBeans.get(jobId));
			}
			check(job.getId(), deps);
		}
		return true;
	}
	//判断死循环问题
	private void check(String parentJobId,Set<JobBean> deps) throws ZeusException {
		for(JobBean job:deps){
			if(job.getActionDescriptor().getId().equals(parentJobId)){
				throw new ZeusException("存在死循环依赖，请检查JobId: " + parentJobId);
			}
			if(job.getActionDescriptor().getScheduleType()== ActionDescriptor.JobScheduleType.Dependent){
				check(parentJobId,job.getDependee());
			}
		}
	}
	/*
	 * 周期任务无法依赖不同周期，且小时任务无法依赖天的任务
	 * @author YangFei
	 */
	public void checkCycleJob(ActionDescriptor job, List<ActionDescriptor> jobs) throws ZeusException {
		if(jobs!=null&&jobs.size()!=0){
			ActionDescriptor tmp=jobs.get(0);
			for(ActionDescriptor j:jobs){
				if(StringUtils.isNotEmpty(tmp.getCycle())&&StringUtils.isNotEmpty(j.getCycle())&&!tmp.getCycle().equals(j.getCycle())){
					throw new ZeusException("周期任务不能依赖不同的周期，请检查!");
				}
				if(StringUtils.isNotEmpty(job.getCycle())&&StringUtils.isNotEmpty(j.getCycle())&&job.getCycle().equals("hour")&&!j.getCycle().equals("hour")){
					throw new ZeusException("小时任务无法依赖比它大的周期，请检查!");
				}
			}
		}
	}
}