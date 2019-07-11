package com.taobao.zeus.web.controller;

import com.alibaba.fastjson.JSON;
import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.*;
import com.taobao.zeus.dal.logic.impl.MysqlLogManager;
import com.taobao.zeus.dal.logic.impl.ReadOnlyGroupManagerWithJob;
import com.taobao.zeus.dal.mapper.ZeusJobMapper;
import com.taobao.zeus.dal.model.*;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.dal.tool.ProcesserUtil;
import com.taobao.zeus.model.*;
import com.taobao.zeus.model.processer.Processer;
import com.taobao.zeus.socket.protocol.Protocol;
import com.taobao.zeus.socket.worker.ClientWorker;
import com.taobao.zeus.util.ContentUtil;
import com.taobao.zeus.util.DateUtil;
import com.taobao.zeus.util.Environment;
import com.taobao.zeus.util.Tuple;
import com.taobao.zeus.web.common.CurrentUser;
import com.taobao.zeus.web.common.JobConfig;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.GridContent;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.platform.client.module.jobdisplay.job.JobHistoryModel;
import com.taobao.zeus.web.platform.client.module.jobmanager.JobModel;
import com.taobao.zeus.web.platform.client.module.jobmanager.JobModelAction;
import com.taobao.zeus.web.platform.client.util.GwtException;
import com.taobao.zeus.web.platform.client.util.ZUser;
import com.taobao.zeus.web.platform.client.util.ZUserContactTuple;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.util.PermissionGroupManagerWithAction;
import com.taobao.zeus.web.util.PermissionGroupManagerWithJob;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.taobao.zeus.dal.model.ZeusUser.ADMIN;

@RestController
@RequestMapping("/job")
public class JobController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);

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

    @Autowired
    private ClientWorker worker;

    @Autowired
    @Qualifier("mysqlLogManager")
    private MysqlLogManager mysqlLogManager;

    @RequestMapping(value = "/get_upstream_job", method = RequestMethod.GET)
    public CommonResponse<JobModel> getUpstreamJob(@RequestParam(value = "jobId") String jobId) {
        try {
            JobModel jobModel = getUpstreamJobUtil(jobId);
            return this.buildResponse(jobModel);
        } catch (Exception e) {
            log.error("load job upstream data failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }

    @RequestMapping(value = "/get_job_by_id", method = RequestMethod.GET)
    public CommonResponse<ZeusJobWithBLOBs> getJobById(@RequestParam(value = "jobId") String jobId) {
        try {
            ZeusJobWithBLOBs ret = zeusJobMapper.selectByPrimaryKey(Long.valueOf(jobId));
            return this.buildResponse(ret);
        } catch (Exception e) {
            log.error("get job by id failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }

    @RequestMapping(value = "/get_job_rely", method = RequestMethod.POST)
    public List<ZeusJobShort> getJobRelyId(@RequestParam(value = "jobId") String jobId) {
        List<ZeusJobShort> ret = new ArrayList<>();
        try {
            ZeusJobWithBLOBs jobInfo = zeusJobMapper.selectByPrimaryKey(Long.valueOf(jobId));
            String dependencies = jobInfo.getDependencies();
            if (dependencies != null && !dependencies.equalsIgnoreCase("")) {
                String[] jobRelys = dependencies.split(",");
                List<Long> jobRelysIds = new ArrayList<>();
                for (String item : jobRelys) {
                    jobRelysIds.add(Long.valueOf(item));
                }

                ret = zeusJobMapper.findJobShortWithIds(jobRelysIds);
            }

        } catch (Exception e) {
            log.error("get job by id failed.", e);
        }
        return ret;
    }

    @RequestMapping(value = "/get_job_like_name", method = RequestMethod.POST)
    public List<ZeusJobShort> getJobLikeName(@RequestParam(value = "jobName") String jobName) {
        List<ZeusJobShort> ret = new ArrayList<>();
        try {
            ret = zeusJobMapper.selectLikeName(jobName);
            return ret;
        } catch (Exception e) {
            log.error("get job like name failed.", e);
            return ret;
        }
    }

    @RequestMapping(value = "/update_job", method = RequestMethod.POST)
    public CommonResponse<Void> updateJob(@RequestParam(value = "jobId") String jobId,
                                          @RequestParam(value = "name") String name,
                                          @RequestParam(value = "owner") String owner,
                                          @RequestParam(value = "groupId") String groupId,
                                          @RequestParam(value = "runType") String runType,
                                          @RequestParam(value = "scheduleType") String scheduleTypenew,
                                          @RequestParam(value = "failRetryTimes") String failRetryTimes,
                                          @RequestParam(value = "cron") String cron,
                                          @RequestParam(value = "dependencies") String dependencies,
                                          @RequestParam(value = "retrySpan") String retrySpan,
                                          @RequestParam(value = "hostGroupId") String hostGroupId,
                                          @RequestParam(value = "priority") String priority,
                                          @RequestParam(value = "scriptVisible") String scriptVisible,
                                          @RequestParam(value = "desc") String desc,
                                          @RequestParam(value = "runTimeSpan") String runTimeSpan,
                                          @RequestParam(value = "config") String config,
                                          @RequestParam(value = "script") String script,
                                          @RequestParam(value = "resource") String resource,
                                          @RequestParam(value = "cycle") String cycle,
                                          @RequestParam(value = "auto") boolean auto,
                                          @RequestParam(value = "timezone") String timezone,
                                          @RequestParam(value = "offset") String offset) {
        try {
            if (!owner.equalsIgnoreCase(ADMIN.getUid())) {
                if (ContentUtil.containInvalidContent(script)) {
                    throw new RuntimeException("没有数据仓库DDL权限！");
                } else if (ContentUtil.containRmCnt(script) != ContentUtil.contentValidRmCnt(script, Environment.getZeusSafeDeleteDir())) {
                    throw new RuntimeException("不能使用rm删除非许可文件路径！");
                }
            }

            JobDescriptor jd = new JobDescriptor();
            jd.setCronExpression(cron);
            List<String> dependList = new ArrayList<>();
            if (dependencies != null && !dependencies.trim().equalsIgnoreCase("")) {
                for (String item:dependencies.split(",")){
                    if (!dependList.contains(item)){
                        dependList.add(item);
                    }
                }
            }
            jd.setDependencies(dependList);
            jd.setDesc(desc);
            jd.setGroupId(groupId);
            jd.setId(jobId);

            JobDescriptor.JobRunType type = null;
            if (runType.equals(JobConfig.MapReduce)) {
                type = JobDescriptor.JobRunType.MapReduce;
            } else if (runType.equals(JobConfig.SHELL)) {
                type = JobDescriptor.JobRunType.Shell;
            } else if (runType.equals(JobConfig.HIVE)) {
                type = JobDescriptor.JobRunType.Hive;
            }
            jd.setJobType(type);

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
            jd.setOwner(owner);
            List<FileResource> resources = new ArrayList<>();
            if (resource != null && !resource.trim().equalsIgnoreCase("")) {
                String[] parts = resource.split("\n");
                for (String part : parts) {
                    if (!part.trim().equalsIgnoreCase("")) {
                        FileResource item = JSON.parseObject(part, FileResource.class);
                        resources.add(item);
                    }
                }
            }
            jd.setResources(resources);
            Map<String, String> properties = new HashMap<>();
            properties.put(JobConfig.ROLL_TIMES, failRetryTimes);
            properties.put(JobConfig.ROLL_INTERVAL, retrySpan);
            properties.put(JobConfig.PRIORITY_LEVEL, priority);
            jd.setProperties(properties);
            jd.setScheduleType(scheduleType);
            jd.setScript(script);
            jd.setAuto(auto);

            jd.setTimezone(timezone);
            jd.setOffRaw(offset);
            jd.setCycle(cycle);

            if (hostGroupId == null) {
                jd.setHostGroupId(Environment.getDefaultWorkerGroupId());
                log.error("job id: " + jd.getId() + " is not setHostGroupId and using the default");
            } else {
                jd.setHostGroupId(hostGroupId);
            }
            ZeusJobWithBLOBs zeusJob = zeusJobMapper.selectByPrimaryKey(Long.valueOf(jd.getId()));

            permissionGroupManagerWithJob.updateJob(CurrentUser.getUser().getUid(), jd);

            String user = LoginUser.getUser().getUid();
            LogDescriptor log = new LogDescriptor();
            log.setCreateTime(new Date());
            log.setUserName(user);
            log.setLogType("update_job");
            log.setUrl(jd.getName());
            log.setOldScript(zeusJob.getScript());
            log.setNewScript(jd.getScript());
            log.setStatus(0);

            mysqlLogManager.addLog(log);

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

    @RequestMapping(value = "/get_imp_contacts_by_jobId", method = RequestMethod.POST)
    public List<ZUser> getImpContactList(@RequestParam(value = "jobId") String jobId) {
        List<ZUser> importantContactList = new ArrayList<>();
        try {
            List<ZeusFollow> jobFollowers = followManager.findJobFollowers(jobId);
            importantContactList = getImportantContactList(jobFollowers);
        }catch (Exception e){
            log.error("get_imp_contacts_by_jobId failed.", e);
        }

        return importantContactList;
    }

    @RequestMapping(value = "/get_not_contacts_by_jobId", method = RequestMethod.POST)
    public List<ZUser> getNotImpContactList(@RequestParam(value = "jobId") String jobId) {
        List<ZUser> notImportantContactList = new ArrayList<>();
        try {
            List<ZeusFollow> jobFollowers = followManager.findJobFollowers(jobId);
            notImportantContactList = getNotImportantContactList(jobFollowers);
        }catch (Exception e){
            log.error("get_not_contacts_by_jobId failed.", e);
        }
        return notImportantContactList;
    }

    public List<ZUser> getImportantContactList(List<ZeusFollow> jobFollowers) {
        return getContactList(jobFollowers, true);
    }

    public List<ZUser> getNotImportantContactList(List<ZeusFollow> jobFollowers) {
        return getContactList(jobFollowers, false);
    }

    @RequestMapping(value = "/get_host_group_name_by_id", method = RequestMethod.GET)
    public CommonResponse<String> getHostGroupNameById(@RequestParam(value = "id") String id) {
        String result = null;
        if (id != null) {
            ZeusHostGroup persist = hostGroupManager.getHostGroupName(id);
            result = persist.getName();
        }
        return this.buildResponse(ReturnCode.SUCCESS, result);
    }

    @RequestMapping(value = "/get_host_group_by_id", method = RequestMethod.POST)
    public GridContent getHostGroupById() {
        GridContent gridcontent = new GridContent();
        try {
            List<ZeusHostGroup> hostGroups = hostGroupManager.getAllHostGroup();
            gridcontent.rows = hostGroups;
            gridcontent.total = 10;
        } catch (Exception e) {
            log.error("job/get_host_group_by_id fail", e);
        }

        return gridcontent;
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

        jobModel.setAdmin(permissionGroupManagerWithJob.hasJobPermission(CurrentUser
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

    @RequestMapping(value = "/get_job_history_list", method = RequestMethod.POST)
    public GridContent jobHistoryPaging(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "rows", required = false) Integer rows) {
        GridContent gridcontent = new GridContent();
        try {
            int start = (page - 1) * rows;
            int limit = rows;
            List<JobHistory> list = jobHistoryManager.pagingList(jobId, start, limit);
            int total = jobHistoryManager.pagingTotal(jobId);

            gridcontent.rows = list;
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

    @RequestMapping(value = "/cancel", method = RequestMethod.GET)
    public CommonResponse<Void> cancel(@RequestParam(value = "id", defaultValue = "") String id) {
        JobHistory history = jobHistoryManager.findJobHistory(id);
        if (!permissionManager.hasActionPermission(CurrentUser.getUser().getUid(), history.getActionId())) {
            return this.buildResponse(ReturnCode.INVALID_ERROR);
        }
        Protocol.ExecuteKind kind = null;
        if (history.getTriggerType() == JobStatus.TriggerType.MANUAL) {
            kind = Protocol.ExecuteKind.ManualKind;
        } else {
            kind = Protocol.ExecuteKind.ScheduleKind;
        }
        try {
            worker.cancelJobFromWeb(kind, id, CurrentUser.getUser().getUid());
            return this.buildResponse(ReturnCode.SUCCESS);
        } catch (Exception e) {
            log.error("cancel action instance error", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/get_job_action", method = RequestMethod.GET)
    public List<Long> getJobACtion(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        List<Long> result = permissionGroupManagerWithAction.getJobACtion(jobId);
        return result;
    }

    @RequestMapping(value = "/handle_run", method = RequestMethod.GET)
    public CommonResponse<Void> handleRun(@RequestParam(value = "actionId", defaultValue = "") String actionId,
                                          @RequestParam(value = "type", defaultValue = "") int type) {
        JobStatus.TriggerType triggerType = null;
        JobDescriptor jobDescriptor = null;
        Protocol.ExecuteKind kind = null;
        if (type == 1) {
            triggerType = JobStatus.TriggerType.MANUAL;
            kind = Protocol.ExecuteKind.ManualKind;
        } else if (type == 2) {
            triggerType = JobStatus.TriggerType.MANUAL_RECOVER;
            kind = Protocol.ExecuteKind.ScheduleKind;
        }
        if (!permissionManager.hasActionPermission(
                CurrentUser.getUser().getUid(), actionId)) {
            return this.buildResponse(ReturnCode.INVALID_ERROR);
        }
        /*
        ZeusLog zeusActionLog = mysqlLogManager.selectLogByActionId(actionId);
        if (zeusActionLog != null) {
            Date now = new Date();
            Calendar calNow = Calendar.getInstance();
            calNow.setTime(now);

            Calendar calLast = Calendar.getInstance();
            calLast.setTime(zeusActionLog.getCreatetime());
            calLast.add(Calendar.MINUTE, 5);
            //上次触发时间没有超过5分钟，拒绝执行。
            if (calLast.after(calNow)) {
                GwtException e = new GwtException("你操作太频繁，请5分钟后再执行！并确认上次操作已经完成或真正被终止！");
                log.error(e);
                throw e;
            }
        }*/
        try {
            Tuple<JobDescriptor, JobStatus> job = permissionGroupManagerWithAction.getActionDescriptor(actionId);
            jobDescriptor = job.getX();
            JobHistory history = new JobHistory();
            history.setActionId(actionId);
            history.setJobId(jobDescriptor.getJobId());
            history.setTriggerType(triggerType);
            history.setOperator(CurrentUser.getUser().getUid());
            //history.setOperator(jobDescriptor.getOwner());
            history.setIllustrate("触发人：" + CurrentUser.getUser().getUid());
            history.setStatus(JobStatus.Status.RUNNING);
            history.setStatisEndTime(jobDescriptor.getStatisEndTime());
            history.setTimezone(jobDescriptor.getTimezone());
//		history.setExecuteHost(jobDescriptor.getHost());
            history.setHostGroupId(jobDescriptor.getHostGroupId());
            jobHistoryManager.addJobHistory(history);


            String user = CurrentUser.getUser().getUid();
            LogDescriptor zeusLog = new LogDescriptor();
            zeusLog.setCreateTime(new Date());
            zeusLog.setUserName(user);
            zeusLog.setLogType("manual_trigger_job");
            zeusLog.setRpc(actionId);

            mysqlLogManager.addLog(zeusLog);
            worker.executeJobFromWeb(kind, history.getId());
        } catch (Exception e) {
            log.error("job/handle_run error ", e);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/open_or_close", method = RequestMethod.GET)
    public CommonResponse<String> openOrClose(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                                            @RequestParam(value = "auto", defaultValue = "") Boolean auto) {
        Tuple<JobDescriptor, JobStatus> job = permissionGroupManagerWithJob.getJobDescriptor(jobId);
        JobDescriptor jd = job.getX();
        // 如果是周期任务，在开启自动调度时，需要计算下一次任务执行时间
        // 2 代表周期调度

        if (auto&& jd.getScheduleType() == JobDescriptor.JobScheduleType.CyleJob) {
            String tz = jd.getTimezone();
            // 小时任务，计算下一个小时的开始时间
            if (jd.getCycle().equals("hour")) {
                long startTimestamp = 0;
                try {
                    startTimestamp = DateUtil.string2Timestamp(
                            DateUtil.getDelayEndTime(0, tz), tz)
                            + Integer.parseInt(jd.getOffRaw())
                            * 60
                            * 1000
                            + 1000;
                } catch (ParseException e) {
                    startTimestamp = new Date().getTime() + 60 * 60 * 1000
                            + 1000;
                    log.error("parse time str to timestamp failed,", e);
                }
                String startStr = DateUtil.getTimeStrByTimestamp(
                        startTimestamp, DateUtil.getDefaultTZStr());
                jd.setStartTimestamp(startTimestamp);
                jd.setStartTime(startStr);
                jd.setStatisStartTime(DateUtil.getDelayStartTime(0, tz));
                jd.setStatisEndTime(DateUtil.getDelayEndTime(0, tz));
            }
            // 天任务，计算天的开始时间和结束时间
            if (jd.getCycle().equals("day")) {
                long startTimestamp = 0;
                try {
                    startTimestamp = DateUtil.string2Timestamp(
                            DateUtil.getDayEndTime(0, tz), null)
                            + Integer.parseInt(jd.getOffRaw()) * 60 * 1000;
                } catch (ParseException e) {
                    startTimestamp = new Date().getTime() + 24 * 60 * 60 * 1000;
                    log.error("parse time str to timestamp failed,", e);
                }
                jd.setStatisStartTime(DateUtil.getDayStartTime(0, tz));
                jd.setStatisEndTime(DateUtil.getDayEndTime(0, tz));
                jd.setStartTimestamp(startTimestamp);
                jd.setStartTime(DateUtil.getTimeStrByTimestamp(startTimestamp,
                        DateUtil.getDefaultTZStr()));
            }

        }

        List<String> canNotCloseList = new ArrayList<String>();
        List<String> canNotOpenList = new ArrayList<String>();
        if (!auto.equals(jd.getAuto())) {
            if (!auto) {
                // 下游存在一个开，就不能关闭
                boolean canChange = true;
                List<String> depdidlst = permissionGroupManagerWithJob.getAllDependencied(jobId);
                if (depdidlst != null && depdidlst.size() != 0) {
                    Map<String, Tuple<JobDescriptor, JobStatus>> depdlst = permissionGroupManagerWithJob.getJobDescriptor(depdidlst);
                    for (Map.Entry<String, Tuple<JobDescriptor, JobStatus>> entry : depdlst.entrySet()) {
                        if (entry.getValue().getX().getAuto()) {
                            canNotCloseList.add(entry.getValue().getX().getId());
                            canChange = false;
                        }
                    }
                    if (canChange) {
                        ChangeAuto(auto, jd);
                    }
                } else {
                    ChangeAuto(auto, jd);// 该节点为尾节点
                }

            } else {
                // 上游全为开才能开,并且开启最近一周的状态
                boolean canChange = true;
                List<String> depidlst = permissionGroupManagerWithJob.getAllDependencies(jobId);
                if (depidlst != null && depidlst.size() != 0) {
                    Map<String, Tuple<JobDescriptor, JobStatus>> deplst = permissionGroupManagerWithJob.getJobDescriptor(depidlst);
                    for (Map.Entry<String, Tuple<JobDescriptor, JobStatus>> entry : deplst.entrySet()) {
                        if (!entry.getValue().getX().getAuto()) {
                            canNotOpenList.add(entry.getValue().getX().getId());
                            canChange = false;
                        }
                    }
                    if (canChange) {
                        ChangeAuto(auto, jd);
                    }
                } else {
                    ChangeAuto(auto, jd);// 该节点为首节点
                }

            }
        }
        String canNotClose= "";
        for (String item : canNotCloseList){
            if (canNotClose.equalsIgnoreCase("")){
                canNotClose = item;
            }else{
                canNotClose += ",";
                canNotClose += item;
            }
        }

        String canNotOpen= "";
        for (String item : canNotOpenList){
            if (canNotOpen.equalsIgnoreCase("")){
                canNotOpen = item;
            }else{
                canNotOpen += ",";
                canNotOpen += item;
            }
        }

        if (!canNotClose.equalsIgnoreCase("")){
            return this.buildResponse(ReturnCode.TAIL,canNotClose);
        }

        if (!canNotOpen.equalsIgnoreCase("")){
            return this.buildResponse(ReturnCode.HEADER,canNotOpen);
        }

        return this.buildResponse(ReturnCode.SUCCESS,"ok");
    }

    private void ChangeAuto(Boolean auto, JobDescriptor jd){
        jd.setAuto(auto);
        try {
            permissionGroupManagerWithJob.updateJob(CurrentUser.getUser().getUid(),jd);
        } catch (Exception e) {
            log.error("openOrClose job  update job failed,",e);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResponse<String> delete(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        try {
            permissionGroupManagerWithJob.deleteJob(CurrentUser.getUser().getUid(),jobId);

            String user = CurrentUser.getUser().getUid();
            LogDescriptor log = new LogDescriptor();
            log.setCreateTime(new Date());
            log.setUserName(user);
            log.setLogType("delete_job");
            log.setIp(jobId);

            mysqlLogManager.addLog(log);

        } catch (ZeusException e) {
            log.error("job/delete failed!", e);
            return this.buildResponse(ReturnCode.FAILED,e.getMessage());
        }
        return this.buildResponse(ReturnCode.SUCCESS,"ok");
    }

    @RequestMapping(value = "/get_job_admins", method = RequestMethod.POST)
    public List<ZUser> getJobAdmins(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        List<ZeusUser> users = permissionGroupManagerWithJob.getJobAdmins(jobId);
        List<ZUser> result = new ArrayList<ZUser>();
        for (ZeusUser zu : users) {
            ZUser z = new ZUser();
            z.setName(zu.getName());
            z.setUid(zu.getUid());
            result.add(z);
        }
        return result;
    }

    @RequestMapping(value = "/get_job_owner", method = RequestMethod.GET)
    public List<ZUser> getJobOwner(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        List<ZeusUser> users = permissionGroupManagerWithJob.getJobAdmins(jobId);
        List<ZUser> result = new ArrayList<ZUser>();
        for (ZeusUser zu : users) {
            ZUser z = new ZUser();
            z.setName(zu.getName());
            z.setUid(zu.getUid());
            result.add(z);
        }
        return result;
    }

    @RequestMapping(value = "/transfer_owner", method = RequestMethod.GET)
    public CommonResponse<Void> transferOwner(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                              @RequestParam(value = "uid", defaultValue = "") String uid )  {
        try {
            permissionGroupManagerWithJob.grantJobOwner(CurrentUser.getUser().getUid(), uid, jobId);
        } catch (ZeusException e) {
            log.error("job/transfer_owner failed!", e);
            return this.buildResponse(ReturnCode.FAILED);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/add_job_admin", method = RequestMethod.GET)
    public CommonResponse<Void> addJobAdmin(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                            @RequestParam(value = "uid", defaultValue = "") String uid ) {
        try {
            permissionGroupManagerWithJob.addJobAdmin(CurrentUser.getUser().getUid(),uid, jobId);
        } catch (ZeusException e) {
            log.error("job/add_job_admin failed!", e);
            return this.buildResponse(ReturnCode.FAILED);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/remove_job_admin", method = RequestMethod.GET)
    public CommonResponse<Void> removeJobAdmin(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                               @RequestParam(value = "uid", defaultValue = "") String uid ) {
        try {
            permissionGroupManagerWithJob.removeJobAdmin(CurrentUser.getUser().getUid(), uid, jobId);
        } catch (ZeusException e) {
            log.error("job/remove_job_admin failed!", e);
            return this.buildResponse(ReturnCode.FAILED);
        }

        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/follow_status", method = RequestMethod.GET)
    public CommonResponse<Boolean> followStatus(@RequestParam(value = "jobId", defaultValue = "") String targetId) {
        Boolean follow = false;
        try{
            List<ZeusFollow> rets = followManagerWithJob.findJobFollowers(targetId);
            for (ZeusFollow item :rets) {
                if (item.getUid().equalsIgnoreCase(CurrentUser.getUser().getUid())){
                    follow = true;
                    break;
                }
            }
        }catch (Exception e){
            log.error("job/follow_status failed!", e);
            return this.buildResponse(ReturnCode.FAILED,follow);
        }
        return this.buildResponse(ReturnCode.SUCCESS,follow);
    }

    @RequestMapping(value = "/follow", method = RequestMethod.GET)
    public CommonResponse<Void> follow(@RequestParam(value = "type", defaultValue = "") Integer type,
                       @RequestParam(value = "jobId", defaultValue = "") String targetId) {
        try{
            followManagerWithJob.addFollow(CurrentUser.getUser().getUid(), type, targetId);
        }catch (Exception e){
            log.error("job/follow failed!", e);
            return this.buildResponse(ReturnCode.FAILED);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.GET)
    public CommonResponse<Void> unFollow(@RequestParam(value = "type", defaultValue = "") Integer type,
                         @RequestParam(value = "jobId", defaultValue = "") String targetId) {
        try{
        followManagerWithJob.deleteFollow(CurrentUser.getUser().getUid(), type, targetId);
        }catch (Exception e){
            log.error("job/unfollow failed!", e);
            return this.buildResponse(ReturnCode.FAILED);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/grant_important", method = RequestMethod.GET)
    public CommonResponse<Void> grantImportantContact(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                                                      @RequestParam(value = "uid", defaultValue = "") String uid) {
        if (permissionGroupManagerWithJob.hasJobPermission(CurrentUser.getUser().getUid(), jobId)) {
            followManager.grantImportantContact(jobId, uid);
        } else {
            return this.buildResponse(ReturnCode.FAILED);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/revoke_important", method = RequestMethod.GET)
    public CommonResponse<Void> revokeImportantContact(@RequestParam(value = "jobId", defaultValue = "") String jobId,
                                                       @RequestParam(value = "uid", defaultValue = "") String uid) {
        if (permissionGroupManagerWithJob.hasJobPermission(CurrentUser.getUser().getUid(), jobId)) {
            followManager.revokeImportantContact(jobId, uid);
        } else {
            return this.buildResponse(ReturnCode.FAILED);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }
}
