package com.taobao.zeus.web.controller;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.*;
import com.taobao.zeus.dal.logic.impl.ReadOnlyGroupManagerWithJob;
import com.taobao.zeus.dal.mapper.ZeusJobMapper;
import com.taobao.zeus.dal.model.ZeusHostGroup;
import com.taobao.zeus.dal.model.ZeusJobWithBLOBs;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.dal.tool.ProcesserUtil;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobHistory;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.ZeusFollow;
import com.taobao.zeus.model.processer.Processer;
import com.taobao.zeus.util.DateUtil;
import com.taobao.zeus.util.Environment;
import com.taobao.zeus.util.Tuple;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.GridContent;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.platform.client.module.jobdisplay.job.JobHistoryModel;
import com.taobao.zeus.web.platform.client.module.jobmanager.JobModel;
import com.taobao.zeus.web.platform.client.module.jobmanager.JobModelAction;
import com.taobao.zeus.web.platform.client.util.ZUser;
import com.taobao.zeus.web.platform.client.util.ZUserContactTuple;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.util.PermissionGroupManagerWithAction;
import com.taobao.zeus.web.util.PermissionGroupManagerWithJob;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/job")
public class JobController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private ReadOnlyGroupManagerWithJob readOnlyGroupManagerWithJob;
    @Autowired
    private FollowManagerWithJob followManagerWithJob;
    @Autowired
    private JobHistoryManager jobHistoryManager;

    @Autowired
    private PermissionGroupManagerWithJob permissionGroupManagerWithJob;
    @Autowired
    private PermissionGroupManagerWithAction permissionGroupManagerWithAction;
    @Autowired
    private FollowManagerWithJob followManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private HostGroupManager hostGroupManager;

    @Autowired
    private ZeusJobMapper zeusJobMapper;

    @RequestMapping(value = "/get_upstream_job", method = RequestMethod.GET)
    public CommonResponse<JobModel> getUpstreamJob(@RequestParam(value = "jobId") String jobId) {
        try {
            JobModel jobModel = getUpstreamJobUtil(jobId);
            return this.buildResponse(jobModel);
        } catch (Exception e) {
            log.error("load my job upstream data failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }

    @RequestMapping(value = "/get_job_by_id", method = RequestMethod.GET)
    public CommonResponse<ZeusJobWithBLOBs> getJobById(@RequestParam(value = "jobId") String jobId) {
        try {
            ZeusJobWithBLOBs ret = zeusJobMapper.selectByPrimaryKey(Long.valueOf(jobId));
            return this.buildResponse(ret);
        } catch (Exception e) {
            log.error("get my job by id failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }

    @RequestMapping(value = "/update_schedule_job", method = RequestMethod.POST)
    public CommonResponse<Void> updateJob(@RequestParam(value = "jobId") String jobId,
                                          @RequestParam(value = "name") String name,
                                          @RequestParam(value = "scheduleType") String scheduleTypenew,
                                          @RequestParam(value = "failRetryTimes") String failRetryTimes,
                                          @RequestParam(value = "cron") String cron,
                                          @RequestParam(value = "retrySpan") String retrySpan,
                                          @RequestParam(value = "hostGroupId") String hostGroupId,
                                          @RequestParam(value = "priority") String priority,
                                          @RequestParam(value = "scriptVisible") String scriptVisible,
                                          @RequestParam(value = "desc") String desc,
                                          @RequestParam(value = "runTimeSpan") String runTimeSpan,
                                          @RequestParam(value = "config") String config,
                                          @RequestParam(value = "script") String script,
                                          @RequestParam(value = "resource") String resource) {
        try {
            JobDescriptor jd = new JobDescriptor();
            jd.setCronExpression(cron);
            jd.setDesc(desc);
            jd.setId(jobId);
            JobDescriptor.JobScheduleType scheduleType = null;

            if (JobModel.DEPEND_JOB.equals(scheduleTypenew)) {
                scheduleType = JobDescriptor.JobScheduleType.Dependent;
            }
            if (JobModel.INDEPEN_JOB.equals(scheduleTypenew)) {
                scheduleType = JobDescriptor.JobScheduleType.Independent;
            }
            if (JobModel.CYCLE_JOB.equals(scheduleTypenew)) {
                scheduleType = JobDescriptor.JobScheduleType.CyleJob;
            }
            jd.setName(name);
            jd.setScheduleType(scheduleType);
            jd.setScript(script);

            if (hostGroupId == null) {
                jd.setHostGroupId(Environment.getDefaultWorkerGroupId());
                log.error("job id: " + jd.getId() + " is not setHostGroupId and using the default");
            } else {
                jd.setHostGroupId(hostGroupId);
            }

            permissionGroupManagerWithJob.updateJob(LoginUser.getUser().getUid(), jd);
            return this.buildResponse(ReturnCode.SUCCESS);
        } catch (ZeusException e) {
            log.error("update schedule job failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/update_depend_job", method = RequestMethod.POST)
    public CommonResponse<Void> updateDependJob(@RequestParam(value = "jobId") String jobId,
                                          @RequestParam(value = "name") String name,
                                          @RequestParam(value = "scheduleType") String scheduleTypenew,
                                          @RequestParam(value = "failRetryTimes") String failRetryTimes,
                                          @RequestParam(value = "dependJobs") String dependJobs,
                                          @RequestParam(value = "retrySpan") String retrySpan,
                                          @RequestParam(value = "dependCycle") String dependCycle,
                                          @RequestParam(value = "priority") String priority,
                                          @RequestParam(value = "hostGroupId") String hostGroupId,
                                          @RequestParam(value = "scriptVisible") String scriptVisible,
                                          @RequestParam(value = "desc") String desc,
                                          @RequestParam(value = "runTimeSpan") String runTimeSpan,
                                          @RequestParam(value = "config") String config,
                                          @RequestParam(value = "script") String script,
                                          @RequestParam(value = "resource") String resource) {
        try {
            JobDescriptor jd = new JobDescriptor();
            jd.setDependencies(Arrays.asList(dependJobs.split(",")));
            jd.setDesc(desc);
            jd.setId(jobId);

            JobDescriptor.JobScheduleType scheduleType = null;
            if (JobModel.DEPEND_JOB.equals(scheduleTypenew)) {
                scheduleType = JobDescriptor.JobScheduleType.Dependent;
            }
            if (JobModel.INDEPEN_JOB.equals(scheduleTypenew)) {
                scheduleType = JobDescriptor.JobScheduleType.Independent;
            }
            if (JobModel.CYCLE_JOB.equals(scheduleTypenew)) {
                scheduleType = JobDescriptor.JobScheduleType.CyleJob;
            }
            jd.setName(name);
            jd.setScheduleType(scheduleType);
            jd.setScript(script);
            jd.setCycle(dependCycle);
            if (hostGroupId == null) {
                jd.setHostGroupId(Environment.getDefaultWorkerGroupId());
                log.error("job id: " + jd.getId() + " is not setHostGroupId and using the default");
            }else {
                jd.setHostGroupId(hostGroupId);
            }

            permissionGroupManagerWithJob.updateJob(LoginUser.getUser().getUid(), jd);
            return this.buildResponse(ReturnCode.SUCCESS);
        } catch (ZeusException e) {
            log.error("update schedule job failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR);
        }
    }

    private List<Processer> parseProcessers(List<String> ps) {
        List<Processer> list = new ArrayList<Processer>();
        for (String s : ps) {
            Processer p = ProcesserUtil.parse(JSONObject.fromObject(s));
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    public List<String> getImportantContactUid(String jobId) {
        List<ZeusFollow> jobFollowers = followManager.findJobFollowers(jobId);
        List<ZUser> contactList = getContactList(jobFollowers, true);
        List<String> uidList = new ArrayList<String>();
        for (ZUser u : contactList) {
            uidList.add(u.getUid());
        }
        return uidList;
    }

    public List<ZUser> getContactList(List<ZeusFollow> jobFollowers, boolean isImportant) {

        List<String> uids = new ArrayList<String>();
        List<ZUser> results = new ArrayList<ZUser>();
        for (ZeusFollow zf : jobFollowers) {
            if (zf.isImportant() == isImportant) {
                uids.add(zf.getUid());
            }
        }
        List<ZeusUser> users = userManager.findListByUid(uids);
        for (ZeusUser user : users) {
            ZUser tmp = new ZUser();
            tmp.setName(user.getName());
            tmp.setUid(user.getUid());
            results.add(tmp);
        }
        return results;
    }

    public List<ZUserContactTuple> getAllContactList(String jobId) {
        List<ZeusFollow> jobFollowers = followManager.findJobFollowers(jobId);
        List<ZUser> importantContactList = getImportantContactList(jobFollowers);
        List<ZUser> notImportantContactList = getNotImportantContactList(jobFollowers);
        List<ZUserContactTuple> results = new ArrayList<ZUserContactTuple>();
        for (ZUser u : importantContactList) {
            ZUserContactTuple tmp = new ZUserContactTuple(u, true);
            results.add(tmp);
        }
        for (ZUser u : notImportantContactList) {
            ZUserContactTuple tmp = new ZUserContactTuple(u, false);
            results.add(tmp);
        }
        return results;
    }

    public List<ZUser> getImportantContactList(List<ZeusFollow> jobFollowers) {
        return getContactList(jobFollowers, true);
    }

    public List<ZUser> getNotImportantContactList(List<ZeusFollow> jobFollowers) {
        return getContactList(jobFollowers, false);
    }

    @RequestMapping(value = "/get_host_group_name_by_id", method = RequestMethod.GET)
    public String getHostGroupNameById(@RequestParam(value = "id") String id) {
        String result = null;
        if (id != null) {
            ZeusHostGroup persist = hostGroupManager.getHostGroupName(id);
            result = persist.getName();
        }
        return result;
    }

    @RequestMapping(value = "/get_job_status_by_groupId", method = RequestMethod.GET)
    public GridContent getSubJobStatus(
            @RequestParam(value = "groupId", defaultValue = "", required = false) String groupId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "rows", required = false) Integer rows,
            @RequestParam(value = "startDate", required = false) Date startDate,
            @RequestParam(value = "endDate", required = false) Date endDate) {
        GridContent gridcontent = new GridContent();
        try {
            int start = (page - 1) * rows;
            int limit = rows;
            GroupBean gb = permissionGroupManagerWithAction.getDownstreamGroupBean(groupId);
            Map<String, JobBean> map = gb.getAllSubJobBeans();
            List<Tuple<JobDescriptor, JobStatus>> allJobs = new ArrayList<Tuple<JobDescriptor, JobStatus>>();
            if (startDate != null && endDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                Integer startInt = Integer.parseInt(dateFormat.format(startDate));
                Integer endInt = Integer.parseInt(dateFormat.format(endDate)) + 1;
                for (String key : map.keySet()) {
                    Integer subkeyInt = Integer.parseInt(key.substring(0, 8));
                    if (subkeyInt < endInt && subkeyInt >= startInt) {
                        Tuple<JobDescriptor, JobStatus> tuple = new Tuple<JobDescriptor, JobStatus>(
                                map.get(key).getJobDescriptor(), map.get(key)
                                .getJobStatus());
                        allJobs.add(tuple);
                    }
                }
            }
            // 按名次排序
            Collections.sort(allJobs,
                    new Comparator<Tuple<JobDescriptor, JobStatus>>() {
                        @Override
                        public int compare(Tuple<JobDescriptor, JobStatus> o1,
                                           Tuple<JobDescriptor, JobStatus> o2) {
                            return o1.getX().getName()
                                    .compareToIgnoreCase(o2.getX().getName());
                        }
                    });

            int total = allJobs.size();
            if (start >= allJobs.size()) {
                start = 0;
            }
            allJobs = allJobs.subList(start,
                    Math.min(start + limit, allJobs.size()));

            List<String> jobIds = new ArrayList<String>();
            for (Tuple<JobDescriptor, JobStatus> tuple : allJobs) {
                jobIds.add(tuple.getX().getId());
                if (tuple.getX().getDependencies() != null) {
                    for (String deps : tuple.getX().getDependencies()) {
                        if (!jobIds.contains(deps)) {
                            jobIds.add(deps);
                        }
                    }
                }
            }
            Map<String, JobHistory> jobHisMap = jobHistoryManager
                    .findLastHistoryByList(jobIds);
            List<JobModelAction> result = new ArrayList<JobModelAction>();
            for (Tuple<JobDescriptor, JobStatus> job : allJobs) {
                JobStatus status = job.getY();
                JobDescriptor jd = job.getX();
                JobModelAction model = new JobModelAction();
                model.setId(status.getJobId());
                Map<String, String> dep = new HashMap<String, String>();
                for (String jobId : job.getX().getDependencies()) {
                    if (jobId != null && !"".equals(jobId)) {
                        // dep.put(jobId, null);
                        if (jobHisMap.get(jobId) != null && jobHisMap.get(jobId).getEndTime() != null) {
                            // System.out.println(new
                            // SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(jobHisMap.get(jobId).getEndTime()));
                            dep.put(jobId,
                                    String.valueOf(jobHisMap.get(jobId)
                                            .getEndTime().getTime()));
                        } else {
                            dep.put(jobId, null);
                        }
                    }
                }
                dep.putAll(status.getReadyDependency());
                model.setReadyDependencies(dep);
                model.setStatus(status.getStatus() == null ? null : status
                        .getStatus().getId());
                model.setName(jd.getName());
                model.setAuto(jd.getAuto());
                model.setToJobId(jd.getJobId());
                JobHistory his = jobHisMap.get(jd.getId());
                if (his != null && his.getStartTime() != null) {
                    SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
                    model.setLastStatus(format.format(his.getStartTime())
                            + " "
                            + (his.getStatus() == null ? "" : his.getStatus()
                            .getId()));
                }
                result.add(model);
            }
            gridcontent.rows = result;
            gridcontent.total = total;
        } catch (Exception e) {
            log.error("get group whole view failed.", e);
        }
        return gridcontent;
    }

    @RequestMapping(value = "/create_job", method = RequestMethod.GET)
    public CommonResponse<JobModel> createJob(@RequestParam(value = "jobName", defaultValue = "") String jobName,
                                              @RequestParam(value = "parentGroupId", defaultValue = "") String parentGroupId,
                                              @RequestParam(value = "jobType", defaultValue = "") String jobType) {
        JobDescriptor.JobRunType type = null;
        JobModel model = new JobModel();
        if (JobModel.MapReduce.equals(jobType)) {
            type = JobDescriptor.JobRunType.MapReduce;
        } else if (JobModel.SHELL.equals(jobType)) {
            type = JobDescriptor.JobRunType.Shell;
        } else if (JobModel.HIVE.equals(jobType)) {
            type = JobDescriptor.JobRunType.Hive;
        }
        try {
            JobDescriptor jd = permissionGroupManagerWithJob.createJob(LoginUser
                    .getUser().getUid(), jobName, parentGroupId, type);
            model = getUpstreamJobUtil(jd.getId());
            model.setDefaultTZ(DateUtil.getDefaultTZStr());
            return this.buildResponse(model);
        } catch (Exception e) {
            log.error("create job failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }

    public JobModel getUpstreamJobUtil(String jobId) {
        JobBean jobBean = permissionGroupManagerWithJob
                .getUpstreamJobBean(jobId);
        JobModel jobModel = new JobModel();

        jobModel.setCronExpression(jobBean.getJobDescriptor()
                .getCronExpression());
        jobModel.setDependencies(jobBean.getJobDescriptor().getDependencies());
        jobModel.setDesc(jobBean.getJobDescriptor().getDesc());
        jobModel.setGroupId(jobBean.getJobDescriptor().getGroupId());
        jobModel.setId(jobBean.getJobDescriptor().getId());
        String jobRunType = null;
        if (jobBean.getJobDescriptor().getJobType() == JobDescriptor.JobRunType.MapReduce) {
            jobRunType = JobModel.MapReduce;
        } else if (jobBean.getJobDescriptor().getJobType() == JobDescriptor.JobRunType.Shell) {
            jobRunType = JobModel.SHELL;
        } else if (jobBean.getJobDescriptor().getJobType() == JobDescriptor.JobRunType.Hive) {
            jobRunType = JobModel.HIVE;
        }
        jobModel.setJobRunType(jobRunType);
        String jobScheduleType = null;
        if (jobBean.getJobDescriptor().getScheduleType() == JobDescriptor.JobScheduleType.Dependent) {
            jobScheduleType = JobModel.DEPEND_JOB;
        }
        if (jobBean.getJobDescriptor().getScheduleType() == JobDescriptor.JobScheduleType.Independent) {
            jobScheduleType = JobModel.INDEPEN_JOB;
        }
        if (jobBean.getJobDescriptor().getScheduleType() == JobDescriptor.JobScheduleType.CyleJob) {
            jobScheduleType = JobModel.CYCLE_JOB;
        }
        jobModel.setJobScheduleType(jobScheduleType);
        jobModel.setLocalProperties(jobBean.getJobDescriptor().getProperties());
        jobModel.setName(jobBean.getJobDescriptor().getName());
        jobModel.setOwner(jobBean.getJobDescriptor().getOwner());
        String ownerName = userManager.findByUid(jobModel.getOwner()).getName();
        if (ownerName == null || "".equals(ownerName.trim())
                || "null".equals(ownerName)) {
            ownerName = jobModel.getOwner();
        }
        jobModel.setOwnerName(ownerName);
        jobModel.setLocalResources(jobBean.getJobDescriptor().getResources());
        jobModel.setAllProperties(jobBean.getHierarchyProperties()
                .getAllProperties());
        jobModel.setAllResources(jobBean.getHierarchyResources());

        jobModel.setAuto(jobBean.getJobDescriptor().getAuto());
        jobModel.setScript(jobBean.getJobDescriptor().getScript());

        List<String> preList = new ArrayList<String>();
        if (!jobBean.getJobDescriptor().getPreProcessers().isEmpty()) {
            for (Processer p : jobBean.getJobDescriptor().getPreProcessers()) {
                JSONObject o = new JSONObject();
                o.put("id", p.getId());
                o.put("config", p.getConfig());
                preList.add(o.toString());
            }
        }
        jobModel.setPreProcessers(preList);

        List<String> postList = new ArrayList<String>();
        if (!jobBean.getJobDescriptor().getPostProcessers().isEmpty()) {
            for (Processer p : jobBean.getJobDescriptor().getPostProcessers()) {
                JSONObject o = new JSONObject();
                o.put("id", p.getId());
                o.put("config", p.getConfig());
                postList.add(o.toString());
            }
        }
        jobModel.setPostProcessers(postList);

        jobModel.setAdmin(permissionGroupManagerWithJob.hasJobPermission(LoginUser
                .getUser().getUid(), jobId));

        List<ZeusFollow> follows = followManager.findJobFollowers(jobId);
        if (follows != null) {
            List<String> followNames = new ArrayList<String>();
            for (ZeusFollow zf : follows) {
                String name = userManager.findByUid(zf.getUid()).getName();
                if (name == null || "".equals(name.trim())) {
                    name = zf.getUid();
                }
                followNames.add(name);
            }
            jobModel.setFollows(followNames);
        }
        jobModel.setImportantContacts(getImportantContactUid(jobId));
        List<String> ladmins = permissionManager.getJobAdmins(jobId);
        List<String> admins = new ArrayList<String>();
        for (String s : ladmins) {
            String name = userManager.findByUid(s).getName();
            if (name == null || "".equals(name.trim()) || "null".equals(name)) {
                name = s;
            }
            admins.add(name);
        }
        jobModel.setAdmins(admins);


        List<String> owners = new ArrayList<String>();
        owners.add(jobBean.getJobDescriptor().getOwner());
        GroupBean parent = jobBean.getGroupBean();
        while (parent != null) {
            if (!owners.contains(parent.getGroupDescriptor().getOwner())) {
                owners.add(parent.getGroupDescriptor().getOwner());
            }
            parent = parent.getParentGroupBean();
        }
        jobModel.setOwners(owners);

        // 所有secret. 开头的配置项都进行权限控制
        for (String key : jobModel.getAllProperties().keySet()) {
            boolean isLocal = jobModel.getLocalProperties().get(key) == null ? false
                    : true;
            if (key.startsWith("secret.")) {
                if (!isLocal) {
                    jobModel.getAllProperties().put(key, "*");
                } else {
                    if (!jobModel.getAdmin()
                            && !jobModel.getOwner().equals(
                            LoginUser.getUser().getUid())) {
                        jobModel.getLocalProperties().put(key, "*");
                    }
                }
            }
        }
        // 本地配置项中的hadoop.hadoop.job.ugi 只有管理员和owner才能查看，继承配置项不能查看
        String SecretKey = "core-site.hadoop.job.ugi";
        if (jobModel.getLocalProperties().containsKey(SecretKey)) {
            String value = jobModel.getLocalProperties().get(SecretKey);
            if (value.lastIndexOf("#") == -1) {
                value = "*";
            } else {
                value = value.substring(0, value.lastIndexOf("#"));
                value += "#*";
            }
            if (!jobModel.getAdmin()
                    && !jobModel.getOwner()
                    .equals(LoginUser.getUser().getUid())) {
                jobModel.getLocalProperties().put(SecretKey, value);
            }
            jobModel.getAllProperties().put(SecretKey, value);
        } else if (jobModel.getAllProperties().containsKey(SecretKey)) {
            String value = jobModel.getAllProperties().get(SecretKey);
            if (value.lastIndexOf("#") == -1) {
                value = "*";
            } else {
                value = value.substring(0, value.lastIndexOf("#"));
                value += "#*";
            }
            jobModel.getAllProperties().put(SecretKey, value);
        }
        // 如果zeus.secret.script=true 并且没有权限，对script进行加密处理
        if ("true".equalsIgnoreCase(jobModel.getAllProperties().get(
                "zeus.secret.script"))) {
            if (!jobModel.getAdmin()
                    && !jobModel.getOwner()
                    .equals(LoginUser.getUser().getUid())) {
                jobModel.setScript("脚本已加密,如需查看请联系相关负责人分配权限");
            }
        }
        if (jobBean.getJobDescriptor().getTimezone() == null
                || "".equals(jobBean.getJobDescriptor().getTimezone())) {
            jobModel.setDefaultTZ(DateUtil.getDefaultTZStr());
        } else {
            jobModel.setDefaultTZ(jobBean.getJobDescriptor().getTimezone());
        }
        jobModel.setOffRaw(jobBean.getJobDescriptor().getOffRaw());
        jobModel.setJobCycle(jobBean.getJobDescriptor().getCycle());
        jobModel.setHost(jobBean.getJobDescriptor().getHost());
        jobModel.setHostGroupId(jobBean.getJobDescriptor().getHostGroupId());
        return jobModel;
    }

    @RequestMapping(value = "/get_job_history_list", method = RequestMethod.GET)
    public GridContent jobHistoryPaging(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "rows", required = false) Integer rows) {
        GridContent gridcontent = new GridContent();
        try {


            int start = (page - 1) * rows;
            int limit = rows;
            List<JobHistory> list = jobHistoryManager.pagingList(jobId,
                    start, limit);
            int total = jobHistoryManager.pagingTotal(jobId);

            List<JobHistoryModel> data = new ArrayList<JobHistoryModel>();
            for (JobHistory his : list) {
                JobHistoryModel d = new JobHistoryModel();
                d.setId(his.getId());
                d.setActionId(his.getActionId());
                d.setJobId(his.getJobId());
                d.setStartTime(his.getStartTime());
                d.setEndTime(his.getEndTime());
                d.setExecuteHost(his.getExecuteHost());
                d.setOperator(his.getOperator());
                d.setStatus(his.getStatus() == null ? null : his.getStatus()
                        .toString());
                String type = "";
                if (his.getTriggerType() != null) {
                    if (his.getTriggerType() == JobStatus.TriggerType.MANUAL) {
                        type = "手动调度";
                    } else if (his.getTriggerType() == JobStatus.TriggerType.MANUAL_RECOVER) {
                        type = "手动恢复";
                    } else if (his.getTriggerType() == JobStatus.TriggerType.SCHEDULE) {
                        type = "自动调度";
                    }
                }
                d.setTriggerType(type);
                d.setIllustrate(his.getIllustrate());
                d.setStatisEndTime(his.getStatisEndTime());
                d.setTimeZone(his.getTimezone());
                d.setCycle(his.getCycle());
                data.add(d);
            }
            gridcontent.rows = data;
            gridcontent.total = total;
        } catch (Exception e) {
            log.error("get job run log failed.", e);
        }
        return gridcontent;
    }

    @RequestMapping(value = "/get_job_history_by_id", method = RequestMethod.GET)
    public CommonResponse<JobHistoryModel> getJobHistory(@RequestParam(value = "id", defaultValue = "") String id) {
        try {
            JobHistory his = jobHistoryManager.findJobHistory(id);
            JobHistoryModel d = new JobHistoryModel();
            d.setId(his.getId());
            d.setActionId(his.getActionId());
            d.setJobId(his.getJobId());
            d.setStartTime(his.getStartTime());
            d.setEndTime(his.getEndTime());
            d.setExecuteHost(his.getExecuteHost());
            d.setStatus(his.getStatus() == null ? null : his.getStatus().toString());
            String type = "";
            if (his.getTriggerType() != null) {
                if (his.getTriggerType() == JobStatus.TriggerType.MANUAL) {
                    type = "手动调度";
                } else if (his.getTriggerType() == JobStatus.TriggerType.MANUAL_RECOVER) {
                    type = "手动恢复";
                } else if (his.getTriggerType() == JobStatus.TriggerType.SCHEDULE) {
                    type = "自动调度";
                }
            }
            d.setTriggerType(type);
            d.setIllustrate(his.getIllustrate());
            d.setLog(his.getLog().getContent());
            d.setStatisEndTime(his.getStatisEndTime());
            d.setTimeZone(his.getTimezone());
            d.setCycle(his.getCycle());

            return this.buildResponse(d);
        } catch (Exception e) {
            log.error("get job run log by id failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }
    @RequestMapping(value = "/get_job_action", method = RequestMethod.GET)
    public List<Long> getJobACtion(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        List<Long> result = permissionGroupManagerWithAction.getJobACtion(jobId);
        return result;
    }
}
