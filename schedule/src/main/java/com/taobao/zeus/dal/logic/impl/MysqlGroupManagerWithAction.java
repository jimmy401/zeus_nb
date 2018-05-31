package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.logic.PermissionManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.mapper.ZeusActionMapper;
import com.taobao.zeus.dal.mapper.ZeusGroupMapper;
import com.taobao.zeus.dal.mapper.ZeusJobMapper;
import com.taobao.zeus.dal.mapper.ZeusWorkerMapper;
import com.taobao.zeus.dal.model.*;
import com.taobao.zeus.dal.tool.*;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobDescriptor.JobRunType;
import com.taobao.zeus.model.JobDescriptor.JobScheduleType;
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

import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unchecked")
@Repository("mysqlGroupManagerWithAction")
public class MysqlGroupManagerWithAction implements GroupManagerWithAction {
    private static Logger log = LoggerFactory.getLogger(MysqlGroupManagerWithAction.class);

    @Autowired
    private PermissionManager permissionManager;

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
    private JobValidate jobValidate;

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
        zeusGroupMapper.updateByPrimaryKey(object);
    }

    @Override
    public void deleteAction(String user, String jobId) throws ZeusException {
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
        zeusActionMapper.deleteByPrimaryKey(Long.valueOf(jobId));
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
            List<Tuple<JobDescriptor, JobStatus>> jobs = getChildrenAction(parent
                    .getGroupDescriptor().getId());
            for (Tuple<JobDescriptor, JobStatus> tuple : jobs) {
                JobBean jobBean = new JobBean(tuple.getX(), tuple.getY());
                jobBean.setGroupBean(parent);
                parent.getJobBeans().put(tuple.getX().getId(), jobBean);
            }
        }

        return parent;
    }

    @Override
    public GroupBean getGlobeGroupBean() {
        return GroupManagerToolWithAction.buildGlobeGroupBean(this);
    }

    /**
     * 获取叶子组下所有的Job
     *
     * @param groupId
     * @return
     */
    @Override
    public List<Tuple<JobDescriptor, JobStatus>> getChildrenAction(String groupId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupId", groupId);
        List<ZeusActionWithBLOBs> list = zeusActionMapper.selectByParams(params);
        List<Tuple<JobDescriptor, JobStatus>> result = new ArrayList<Tuple<JobDescriptor, JobStatus>>();
        if (list != null) {
            for (ZeusActionWithBLOBs j : list) {
                result.add(PersistenceAndBeanConvertWithAction.convert(j));
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
                result.add(PersistenceAndBeanConvertWithAction.convert(p));
            }
        }
        return result;
    }

    @Override
    public GroupDescriptor getGroupDescriptor(String groupId) {
        ZeusGroupWithBLOBs persist = zeusGroupMapper.selectByPrimaryKey(Integer.valueOf(groupId));
        log.info("root group id : {}",persist.getId());
        if (persist != null) {
            return PersistenceAndBeanConvertWithAction.convert(persist);
        }
        return null;
    }

    @Override
    public Tuple<JobDescriptor, JobStatus> getActionDescriptor(String actionId) {
        ZeusActionWithBLOBs persist = getAction(actionId);
        if (persist == null) {
            return null;
        }
        Tuple<JobDescriptor, JobStatus> t = PersistenceAndBeanConvertWithAction
                .convert(persist);
        JobDescriptor jd = t.getX();
        // 如果是周期任务，并且依赖不为空，则需要封装周期任务的依赖
        if (jd.getScheduleType() == JobScheduleType.CyleJob
                && jd.getDependencies() != null) {
            ZeusAction jp = null;
            for (String jobID : jd.getDependencies()) {
                if (StringUtils.isNotEmpty(jobID)) {
                    jp = getAction(jobID);
                    if(jp!=null){
                        jd.getDepdCycleJob().put(jobID, jp.getCycle());
                    }
                }
            }

        }
        return t;
    }

    private ZeusActionWithBLOBs getAction(String actionId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("actionId", actionId);
        List<ZeusActionWithBLOBs> persist = zeusActionMapper.selectByActionId(params);
        if (persist == null || persist.size() == 0) {
            return null;
        }
        return persist.get(0);
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
        return GroupManagerToolWithAction.getUpstreamGroupBean(groupId, this);
    }

    @Override
    public JobBean getUpstreamJobBean(String jobId) {
        return GroupManagerToolWithAction.getUpstreamJobBean(jobId, this);
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

        ZeusGroupWithBLOBs persist = PersistenceAndBeanConvertWithAction.convert(group);

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
    public void updateAction(String user, JobDescriptor job) throws ZeusException {
        ZeusActionWithBLOBs orgPersist = zeusActionMapper.selectByPrimaryKey(Long.valueOf(job.getId()));
        updateAction(user, job, orgPersist.getOwner(), orgPersist.getGroupId()
                .toString());
    }

    public void updateAction(String user, JobDescriptor job, String owner,
                          String groupId) throws ZeusException {
        ZeusActionWithBLOBs orgPersist = zeusActionMapper.selectByPrimaryKey(Long.valueOf(job.getId()));
        if (job.getScheduleType() == JobScheduleType.Independent) {
            job.setDependencies(new ArrayList<String>());
        } else if (job.getScheduleType() == JobScheduleType.Dependent) {
            job.setCronExpression("");
        }
        job.setOwner(owner);
        job.setGroupId(groupId);
        // 以下属性不允许修改，强制采用老的数据
        ZeusActionWithBLOBs persist = PersistenceAndBeanConvertWithAction.convert(job);
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
        if (job.getScheduleType().equals(JobScheduleType.CyleJob)
                && job.getDependencies() != null
                && job.getDependencies().size() != 0) {
            List<JobDescriptor> list = this.getActionDescriptors(job
                    .getDependencies());
            jobValidate.checkCycleJob(job, list);
        }

        if (jobValidate.valide(job)) {
            zeusActionMapper.updateByPrimaryKeySelective(persist);
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

        ZeusGroupWithBLOBs persist = PersistenceAndBeanConvertWithAction.convert(group);
        Date now = new Date();
        persist.setGmtCreate(now);
        persist.setGmtModified(now);
        persist.setExisted(1);
        zeusGroupMapper.insertSelective(persist);

        Map<String ,Object> params = new HashMap<String ,Object>();
        params.put("owner",user);
        params.put("name",groupName);
        params.put("parent",parentGroup);
        params.put("directory",isDirectory);
        params.put("existed",1);
        params.put("gmtCreate",now);
        params.put("gmtModified",now);
        List<ZeusGroupWithBLOBs> result = zeusGroupMapper.selectByParams(params);
        return PersistenceAndBeanConvertWithAction.convert(result.get(0));
    }

    @Override
    public JobDescriptor createAction(String user, String jobName,
                                      String parentGroup, JobRunType jobType) throws ZeusException {
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
        ZeusActionWithBLOBs persist = PersistenceAndBeanConvertWithAction.convert(job);
        Date now = new Date();
        persist.setGmtCreate(now);
        persist.setGmtModified(now);

        zeusActionMapper.insertSelective(persist);
        Map<String ,Object> params = new HashMap<String ,Object>();
        params.put("owner",user);
        params.put("name",jobName);
        params.put("groupId",parentGroup);
        params.put("runType",jobType);
        List<ZeusActionWithBLOBs> result = zeusActionMapper.selectByParams(params);

        return PersistenceAndBeanConvertWithAction.convert(result.get(0)).getX();
    }

    @Override
    public Map<String, Tuple<JobDescriptor, JobStatus>> getActionDescriptor(
            final Collection<String> jobIds) {
        List<Long> ids = new ArrayList<Long>();
        for (String i : jobIds) {
            ids.add(Long.valueOf(i));
        }
        List<ZeusActionWithBLOBs> list = zeusActionMapper.findActionWithIds(ids);
        List<Tuple<JobDescriptor, JobStatus>> result = new ArrayList<Tuple<JobDescriptor, JobStatus>>();
        if (list != null && !list.isEmpty()) {
            for (ZeusActionWithBLOBs persist : list) {
                result.add(PersistenceAndBeanConvertWithAction
                        .convert(persist));
            }
        }

        Map<String, Tuple<JobDescriptor, JobStatus>> map = new HashMap<String, Tuple<JobDescriptor, JobStatus>>();
        for (Tuple<JobDescriptor, JobStatus> jd : result) {
            map.put(jd.getX().getId(), jd);
        }
        return map;
    }

    public List<JobDescriptor> getActionDescriptors(final Collection<String> jobIds) {
        List<Long> ids = new ArrayList<Long>();
        for (String i : jobIds) {
            if (StringUtils.isNotEmpty(i)) {
                ids.add(Long.valueOf(i));
            }
        }
        List<ZeusActionWithBLOBs> list = zeusActionMapper.findActionWithIds(ids);
        List<JobDescriptor> result = new ArrayList<JobDescriptor>();
        if (result != null && !result.isEmpty()) {
            for (ZeusActionWithBLOBs persist : list) {
                result.add(PersistenceAndBeanConvertWithAction.convert(
                        persist).getX());
            }
        }
        return result;
    }

    @Override
    public void updateActionStatus(JobStatus jobStatus) {
        ZeusActionWithBLOBs persistence = getAction(jobStatus.getJobId());
        persistence.setGmtModified(new Date());

        // 只修改状态 和 依赖 2个字段
        ZeusActionWithBLOBs temp = PersistenceAndBeanConvertWithAction.convert(jobStatus);
        persistence.setStatus(temp.getStatus());
        persistence.setReadyDependency(temp.getReadyDependency());
        persistence.setHistoryId(temp.getHistoryId());

        zeusActionMapper.updateByPrimaryKeySelective(persistence);
    }

    @Override
    public JobStatus getActionStatus(String jobId) {
        Tuple<JobDescriptor, JobStatus> tuple = getActionDescriptor(jobId);
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
        Tuple<JobDescriptor, JobStatus> job = getActionDescriptor(jobId);
        if (job != null) {
            job.getX().setOwner(uid);
            updateAction(granter, job.getX(), uid, job.getX().getGroupId());
        }
    }

    @Override
    public void moveJob(String uid, String jobId, String groupId)
            throws ZeusException {
        JobDescriptor jd = getActionDescriptor(jobId).getX();
        GroupDescriptor gd = getGroupDescriptor(groupId);
        if (gd.isDirectory()) {
            throw new ZeusException("非法操作");
        }
        updateAction(uid, jd, jd.getOwner(), groupId);
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
        for (ZeusWorker item:list) {
            results.add(item.getHost());
        }
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

    @Override
    public void saveOrUpdateAction(ZeusActionWithBLOBs actionPer) throws ZeusException{
        try{
            ZeusActionWithBLOBs action = zeusActionMapper.selectByPrimaryKey(actionPer.getId());
            if(action != null){
                if(action.getStatus() == null || !action.getStatus().equalsIgnoreCase("running")){
                    actionPer.setHistoryId(action.getHistoryId());
                    actionPer.setReadyDependency(action.getReadyDependency());

                    if(actionPer.getStatus()==null||
                            !actionPer.getStatus().equals(JobStatus.Status.FAILED.getId()))
                        actionPer.setStatus(action.getStatus());
                }else{
                    actionPer = action;
                }
            }else{
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                SimpleDateFormat df2 = new SimpleDateFormat("HH");
                String currentDateStr = df.format(new Date())+"0000";
                String currentHourStr = df2.format(new Date());
                if (Integer.parseInt(currentHourStr) > 8 && actionPer.getId() < Long.parseLong(currentDateStr)) {
                    actionPer.setStatus("failed");
                }
            }
            if(actionPer.getAuto() == 0){
                if(actionPer.getStatus() == null || actionPer.getStatus().equalsIgnoreCase("wait")){
                    actionPer.setStatus("failed");
                }
            }
            if(action != null){
                zeusActionMapper.updateByPrimaryKeySelective(actionPer);
            }else
            {
                zeusActionMapper.insertSelective(actionPer);
            }
        }catch(DataAccessException e){
            throw new ZeusException(e);
        }
    }

    @Override
    public List<ZeusActionWithBLOBs> getLastJobAction(String jobId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("jobId", jobId);
        List<ZeusActionWithBLOBs> list = zeusActionMapper.selectByJobId(params);
        return list;
    }


    @Override
    public List<Tuple<JobDescriptor, JobStatus>> getActionList(String jobId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("jobId", jobId);
        List<ZeusActionWithBLOBs> list = zeusActionMapper.selectByJobId(params);
        List<Tuple<JobDescriptor, JobStatus>> lst = new ArrayList<Tuple<JobDescriptor, JobStatus>>();
        for (ZeusActionWithBLOBs persist : list) {
            lst.add(PersistenceAndBeanConvertWithAction.convert(persist));
        }
        return lst;
    }

    @Override
    public void updateAction(JobDescriptor actionTor) throws ZeusException {
        try {
            ZeusActionWithBLOBs actionPer = PersistenceAndBeanConvertWithAction.convert(actionTor);
            ZeusActionWithBLOBs action = zeusActionMapper.selectByPrimaryKey(actionPer.getId());
            if (action != null) {
                if (action.getStatus() == null || !action.getStatus().equalsIgnoreCase("running")) {
                    actionPer.setHistoryId(action.getHistoryId());
                    actionPer.setReadyDependency(action.getReadyDependency());
                    actionPer.setStatus(action.getStatus());
                } else {
                    actionPer = action;
                }

                zeusActionMapper.updateByPrimaryKeySelective(actionPer);
            } else {
                zeusActionMapper.insert(actionPer);
            }
        } catch (DataAccessException e) {
            throw new ZeusException(e);
        }
    }

    @Override
    public void removeAction(Long actionId) throws ZeusException{
        try{
            ZeusActionWithBLOBs action = zeusActionMapper.selectByPrimaryKey(actionId);
            if(action != null){
                zeusActionMapper.deleteByPrimaryKey(action.getId());
            }
        }catch(DataAccessException e){
            throw new ZeusException(e);
        }
    }

    @Override
    public boolean IsExistedBelowRootGroup(String GroupName) {
        String rootId = getRootGroupId();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parent", Integer.valueOf(rootId));
        List<ZeusGroupWithBLOBs> temps = zeusGroupMapper.findByRoot(params);
        for (ZeusGroupWithBLOBs tmp : temps) {
            if (tmp.getName().equals(GroupName)) {
                return true;
            }
        }
        return false;
    }

}