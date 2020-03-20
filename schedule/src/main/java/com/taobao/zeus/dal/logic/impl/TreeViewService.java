package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.logic.GroupManagerWithJob;
import com.taobao.zeus.dal.mapper.ZeusGroupMapper;
import com.taobao.zeus.dal.mapper.ZeusJobMapper;
import com.taobao.zeus.dal.model.*;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.dal.tool.Judge;
import com.taobao.zeus.model.ActionDescriptor;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.schedule.mvc.DebugInfoLog;
import com.taobao.zeus.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.*;

@Repository("treeViewService")
public class TreeViewService {

    @Autowired
    ZeusJobMapper zeusJobMapper;

    @Autowired
    ZeusGroupMapper zeusGroupMapper;

    @Autowired
    @Qualifier("mysqlGroupManagerWithJob")
    GroupManagerWithJob groupManagerWithJob;

    private static final Logger log = LoggerFactory.getLogger(TreeViewService.class);

    private GroupBean ignoreGlobe;

    private GroupBean globe;

    private static final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);

    public GroupBean buildGlobeGroupBeanWithRelation(){
        if(globe!=null){
            if(!isJobsAndGroupsChanged()){
                return globe;
            }
        }
        globe = buildRelyRelation(buildGlobeGroupBean());
        return globe;
    }


    //传入一个GroupBean,把他所含的任务的依赖关系建立起来
    public GroupBean buildRelyRelation(GroupBean groupBean){
        Map<String, JobBean> allJobBeans= groupBean.getAllSubJobBeans();
        for(JobBean j1:allJobBeans.values()){
            if(j1.getActionDescriptor().getScheduleType()== ActionDescriptor.JobScheduleType.Dependent){
                for(String depId:j1.getActionDescriptor().getDependencies()){
                    try {
                        JobBean depJob=allJobBeans.get(depId);
                        j1.addDependee(depJob);
                        depJob.addDepender(j1);
                    } catch (Exception e) {
                        log.error("The jobid is " + j1.getActionDescriptor().getId() + ", the depId is " + depId);
                    }

                }
            }
        }
        return groupBean;
    }

    //构建一个树状结构，包含目录，组和节点
    public GroupBean buildGlobeGroupBean() {
        if (ignoreGlobe==null || isJobsAndGroupsChangedIgnoreContent())
        {
            ignoreGlobe = getDownstreamGroupBean(getRootGroupId());
        }
        return ignoreGlobe;
    }

    public String getRootGroupId() {
        return groupManagerWithJob.getRootGroupId();
    }

    public GroupBean getDownstreamGroupBean(String groupId) {
        ZeusGroupWithBLOBs group = getGroupDescriptor(groupId);
        GroupBean result = new GroupBean(group);
        return getDownstreamGroupBean(result);
    }

    public ZeusGroupWithBLOBs getGroupDescriptor(String groupId) {
        return groupManagerWithJob.getZeusGroupById(groupId);
    }

    public GroupBean getDownstreamGroupBean(GroupBean parent) {
        try {
            return getDownstreamGroupBean(parent, 99).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("getDownstreamGroupBean failed", e);
            return null;
        }
    }

    private Future<GroupBean> getDownstreamGroupBean(final GroupBean parent, final int depth) throws Exception {
        Callable<GroupBean> callable = new Callable<GroupBean>() {

            @Override
            public GroupBean call() throws Exception {
                if (parent.isDirectory()) {
                    List<ZeusGroupWithBLOBs> children = getChildrenGroup(parent.getGroupDescriptor().getId().toString());
                    ArrayList<Future<GroupBean>> futures = new ArrayList<Future<GroupBean>>(children.size());
                    for (ZeusGroupWithBLOBs child : children) {
                        GroupBean childBean = new GroupBean(child);
                        if (pool.getActiveCount() < 15) {
                            futures.add(getDownstreamGroupBean(childBean, 99));
                        } else {
                            getDownstreamGroupBean(childBean, 0);
                        }
                        childBean.setParentGroupBean(parent);
                        parent.getChildrenGroupBeans().add(childBean);
                    }
                    for (Future<GroupBean> f : futures) {
                        f.get(10, TimeUnit.SECONDS);
                    }
                } else {
                    List<Tuple<ActionDescriptor, JobStatus>> jobs = getChildrenJob(parent.getGroupDescriptor().getId().toString());
                    for (Tuple<ActionDescriptor, JobStatus> tuple : jobs) {
                        JobBean jobBean = new JobBean(tuple.getX(), tuple.getY());
                        jobBean.setGroupBean(parent);
                        parent.getJobBeans().put(tuple.getX().getId(), jobBean);
                    }
                }
                return parent;
            }
        };
        if (depth > 0) {
            return pool.submit(callable);
        } else {
            callable.call();
            return new Future<GroupBean>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return false;
                }

                @Override
                public GroupBean get(){
                    return null;
                }

                @Override
                public GroupBean get(long timeout, TimeUnit unit) {
                    return parent;
                }
            };
        }
    }

    public List<ZeusGroupWithBLOBs> getChildrenGroup(String groupId) {
        List<ZeusGroupWithBLOBs> list = groupManagerWithJob.getChildrenGroup(groupId);
        return list;
    }

    public List<Tuple<ActionDescriptor, JobStatus>> getChildrenJob(String groupId) {
        List<Tuple<ActionDescriptor, JobStatus>> list = groupManagerWithJob.getChildrenJob(groupId);
        List<Tuple<ActionDescriptor, JobStatus>> result = new ArrayList<Tuple<ActionDescriptor, JobStatus>>();
        for (Tuple<ActionDescriptor, JobStatus> tuple : list) {
            Tuple<ActionDescriptor, JobStatus> t = new Tuple<ActionDescriptor, JobStatus>(new ReadOnlyActionDescriptor(tuple.getX()), new ReadOnlyJobStatus(tuple.getY()));
            result.add(t);
        }
        return result;
    }

    private Judge jobjudge=new Judge();
    Judge jobrealtime = null;
    private Judge groupjudge=new Judge();
    Judge grouprealtime = null;

    /**
     * Jobs或者Groups是否有变化
     * 判断标准：同时满足以下条件
     * 1.max id 一致
     * 2.count 数一致
     * 3.last_modified 一致
     * @return
     */
    private boolean isJobsAndGroupsChanged(){
        boolean isJobsChanged = isJobsChanged();
        if(isJobsChanged){
            this.jobjudge=jobrealtime;
            return isJobsChanged;
        }

        boolean isGroupChanged = isGroupsChanged();
        if (isGroupChanged){
            this.groupjudge=grouprealtime;
            return isGroupChanged;
        }
        return isJobsChanged || isGroupChanged;
    }

//
    private boolean isJobsAndGroupsChangedIgnoreContent(){
        boolean isJobsChanged = isJobsChanged();
        boolean isJobsChangedParent = false;
        if(isJobsChanged){
            this.jobjudge=jobrealtime;
            return isJobsChanged;
        }else {
            isJobsChangedParent = isJobsChangedParent();
            if (isJobsChangedParent){
                this.jobjudge=jobrealtime;
                return isJobsChangedParent;
            }
        }

        boolean isGroupChanged = isGroupsChanged();
        boolean isGroupsChangedParent = false;
        if (isGroupChanged){
            this.groupjudge=grouprealtime;
            return isGroupChanged;
        }else{
            isGroupsChangedParent = isGroupsChangedParent();
            if (isGroupsChangedParent){
                this.groupjudge=grouprealtime;
                return isGroupsChangedParent;
            }
        }
        return isJobsChanged || isJobsChangedParent || isGroupChanged || isGroupsChangedParent;
    }

    private boolean isJobsChanged(){
        boolean jobChanged;
        ZeusJobStatistic item= zeusJobMapper.selectJobStatistic();
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
            jobChanged= true;
        }

        return jobChanged;
    }

private boolean isJobsChangedParent(){
    Map<String,Object> params = new HashMap<String,Object>();
    params.put("gmtModified", jobjudge.lastModified);
    List<ZeusJobWithBLOBs> changedJobs =zeusJobMapper.selectGreatThanGmtModified(params);
    return isAllJobsNotChangeParent(ignoreGlobe, changedJobs);
}

private boolean isGroupsChanged(){
    boolean groupChanged;
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
        groupChanged= true;
    }

    return groupChanged;
}

private boolean isGroupsChangedParent(){
    Map<String,Object> groupParams = new HashMap<String,Object>();
    groupParams.put("gmtModified", groupjudge.lastModified);
    List<ZeusGroupWithBLOBs> changedGroups = zeusGroupMapper.selectGreatThanModified(groupParams);
    return isAllGroupsNotChangeThese(ignoreGlobe, changedGroups);
}

    private boolean isAllJobsNotChangeParent(GroupBean gb,List<ZeusJobWithBLOBs> list){
        Map<String, JobBean> allJobs=gb.getAllSubJobBeans();
        for(ZeusJobWithBLOBs jd:list){
            JobBean bean=allJobs.get(jd.getId());
            if(bean==null){
                DebugInfoLog.info("isAllJobsNotChangeParent job id="+ jd.getId()+" has changed");
                return false;
            }
            ActionDescriptor old=bean.getActionDescriptor();
            if(!old.getGroupId().equals(jd.getGroupId())){
                DebugInfoLog.info("isAllJobsNotChangeParent job id="+ jd.getId()+" has changed");
                return false;
            }
        }
        return true;
    }

    private boolean isAllGroupsNotChangeThese(GroupBean gb,List<ZeusGroupWithBLOBs> list){
        Map<String, GroupBean> allGroups=gb.getAllSubGroupBeans();
        for(ZeusGroupWithBLOBs gd:list){
            GroupBean bean=allGroups.get(gd.getId());
            if(gd.getId().equals(gb.getGroupDescriptor().getId())){
                break;
            }
            if(bean==null){
                DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
                return false;
            }
            ZeusGroupWithBLOBs old=bean.getGroupDescriptor();
            if(!old.getParent().equals(gd.getParent())){
                DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
                return false;
            }
        }
        return isGroupsNotChangeExisted(allGroups,list);
    }

    private boolean isGroupsNotChangeExisted(Map<String, GroupBean> allGroups,List<ZeusGroupWithBLOBs> list){
        for(ZeusGroupWithBLOBs tmp:list){
            GroupBean bean=allGroups.get(tmp.getId());
            if (bean!=null && bean.isExisted()!=tmp.getbExisted()) {
                return false;
            }
        }
        return true;
    }
}
