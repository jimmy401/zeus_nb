package com.taobao.zeus.web.controller;

import com.taobao.zeus.dal.logic.FollowManagerWithJob;
import com.taobao.zeus.dal.logic.JobHistoryManager;
import com.taobao.zeus.dal.logic.PermissionManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.logic.impl.ReadOnlyGroupManagerWithJob;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;
import com.taobao.zeus.model.JobHistory;
import com.taobao.zeus.model.ZeusFollow;
import com.taobao.zeus.util.DateUtil;
import com.taobao.zeus.web.common.CurrentUser;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.platform.client.module.jobmanager.GroupJobTreeModel;
import com.taobao.zeus.web.platform.client.module.jobmanager.JobRelationInfo;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.util.PermissionGroupManagerWithJob;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/tree")
public class TreeController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TreeController.class);

    @Autowired
    private ReadOnlyGroupManagerWithJob readOnlyGroupManagerWithJob;
    @Autowired
    private FollowManagerWithJob followManagerWithJob;
    @Autowired
    private JobHistoryManager jobHistoryManager;

    @Autowired
    private PermissionGroupManagerWithJob permissionGroupManagerWithJob;
    @Autowired
    private FollowManagerWithJob followManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private PermissionManager permissionManager;

    @RequestMapping(value = "/my_tree_data", method = RequestMethod.GET)
    public CommonResponse<List<GroupJobTreeModel>> getMyTreeData(HttpServletResponse response) throws Exception {
        try {
            String uid = LoginUser.getUser().getUid();
            GroupBean rootGroup = readOnlyGroupManagerWithJob.getGlobeGroupBeanForTreeDisplay(true);
            Map<String, JobBean> allJobs = rootGroup.getAllSubJobBeans();
            for (String key : allJobs.keySet()) {
                JobBean bean = allJobs.get(key);
                //不是owner，删除
                if (!bean.getJobDescriptor().getOwner().equals(uid)) {
                    bean.getGroupBean().getJobBeans().remove(key);
                }
            }

            Map<String, GroupBean> allGroups = rootGroup.getAllSubGroupBeans();
            List<GroupBean> leafGroups = new ArrayList<GroupBean>();
            for (GroupBean bean : allGroups.values()) {
                if (!bean.isDirectory() || bean.getChildrenGroupBeans().isEmpty()) {
                    leafGroups.add(bean);
                }
            }
            for (GroupBean bean : leafGroups) {
                recursionRemove(bean, uid);
            }

            GroupJobTreeModel item = getTreeData(rootGroup);
            List<GroupJobTreeModel> ret = new ArrayList<>();
            ret.add(item);
            return this.buildResponse(ret);
        } catch (Exception e) {
            log.error("load my jobs tree data failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }

    }

    @RequestMapping(value = "/all_tree_data", method = RequestMethod.GET)
    public CommonResponse<List<GroupJobTreeModel>> getTreeData() {
        try {
            GroupBean globe = readOnlyGroupManagerWithJob.getGlobeGroupBeanForTreeDisplay(false);
            GroupJobTreeModel item = getTreeData(globe);
            List<GroupJobTreeModel> ret = new ArrayList<>();
            ret.add(item);
            return this.buildResponse(ret);
        } catch (Exception e) {
            log.error("load all jobs tree data failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }

    private void recursionRemove(GroupBean bean, String uid) {
        if (!bean.isDirectory()) {
            if (!bean.getGroupDescriptor().getOwner().equals(uid) && bean.getJobBeans().isEmpty()) {
                GroupBean parent = bean.getParentGroupBean();
                if (parent != null) {
                    parent.getChildrenGroupBeans().remove(bean);
                    recursionRemove(parent, uid);
                }
            }
        } else {
            if (!bean.getGroupDescriptor().getOwner().equals(uid) && bean.getChildrenGroupBeans().isEmpty()) {
                GroupBean parent = bean.getParentGroupBean();
                if (parent != null) {
                    parent.getChildrenGroupBeans().remove(bean);
                    recursionRemove(parent, uid);
                }
            }
        }
    }

    private GroupJobTreeModel getTreeData(GroupBean rootGroup) {
        String uid = CurrentUser.getUser().getUid();
        List<ZeusFollow> list = followManagerWithJob.findAllTypeFollows(uid);
        Map<String, Boolean> groupFollow = new HashMap<String, Boolean>();
        Map<String, Boolean> jobFollow = new HashMap<String, Boolean>();
        for (ZeusFollow f : list) {
            if (ZeusFollow.GroupType.equals(f.getType())) {
                groupFollow.put(f.getTargetId(), true);
            } else if (ZeusFollow.JobType.equals(f.getType())) {
                jobFollow.put(f.getTargetId(), true);
            }
        }

        GroupJobTreeModel root = new GroupJobTreeModel();
        root.setName(rootGroup.getGroupDescriptor().getName());
        root.setId(rootGroup.getGroupDescriptor().getId());
        root.setGroup(true);
        root.setDirectory(true);
        root.setJob(false);
        root.setOwner(rootGroup.getGroupDescriptor().getOwner());
        Boolean isFollow = groupFollow.get(rootGroup.getGroupDescriptor().getId());
        root.setFollow(isFollow == null ? false : isFollow);

        setGroup(root, rootGroup.getChildrenGroupBeans(), groupFollow, jobFollow);
        return root;
    }

    private void setGroup(GroupJobTreeModel parent, List<GroupBean> children, Map<String, Boolean> groupFollow, Map<String, Boolean> jobFollow) {
        Collections.sort(children, new Comparator<GroupBean>() {
            public int compare(GroupBean o1, GroupBean o2) {
                return o1.getGroupDescriptor().getName().compareToIgnoreCase(o2.getGroupDescriptor().getName());
            }
        });
        for (GroupBean g : children) {
            if (g.isExisted()) {
                GroupJobTreeModel group = new GroupJobTreeModel();
                group.setName(g.getGroupDescriptor().getName());
                group.setId(g.getGroupDescriptor().getId());
                group.setGroup(true);
                group.setJob(false);
                group.setOwner(g.getGroupDescriptor().getOwner());
                group.setDirectory(g.isDirectory());
                Boolean follow = groupFollow.get(g.getGroupDescriptor().getId());
                group.setFollow(follow == null ? false : (follow ? true : false));
                parent.getChildren().add(group);
                if (g.isDirectory()) {
                    setGroup(group, g.getChildrenGroupBeans(), groupFollow, jobFollow);
                } else {
                    List<JobBean> list = new ArrayList<JobBean>();
                    for (JobBean jb : g.getJobBeans().values()) {
                        list.add(jb);
                    }
                    Collections.sort(list, new Comparator<JobBean>() {
                        public int compare(JobBean o1, JobBean o2) {
                            return o1.getJobDescriptor().getName().compareTo(o2.getJobDescriptor().getName());
                        }
                    });
                    for (JobBean jb : list) {
                        GroupJobTreeModel job = new GroupJobTreeModel();
                        job.setId(jb.getJobDescriptor().getId());
                        job.setGroup(false);
                        job.setDirectory(false);
                        job.setName(jb.getJobDescriptor().getName());
                        job.setJob(true);
                        Boolean jFollow = jobFollow.get(job.getId());
                        job.setFollow(jFollow == null ? false : (jFollow ? true : false));
                        job.setScheduleType(jb.getJobDescriptor().getScheduleType().getType());
                        group.getChildren().add(job);
                    }
                }
            }
        }
    }

    @RequestMapping(value = "/get_dependee_tree", method = RequestMethod.GET)
    public CommonResponse<JobRelationInfo> getDependeeTreeJson(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        try {
            JobRelationInfo jobRelationInfo = getJsonData(getDependeeTree(jobId));
            return this.buildResponse(jobRelationInfo,ReturnCode.SUCCESS);
        } catch (Exception e) {
            log.error("get_dependee_tree", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private JobRelationInfo getJsonData(GroupJobTreeModel result) throws JSONException {
        JobRelationInfo jobRelationInfo = new JobRelationInfo();
        jobRelationInfo.setId(result.getId() + "-" + new Random().nextInt(1000));
        jobRelationInfo.setName(result.getName());

        Map<String, JobHistory> map = jobHistoryManager.findLastHistoryByList(Arrays.asList(result.getId()));
        JobHistory his = map.get(result.getId());
        if (his == null) {
            jobRelationInfo.setJobId(result.getId());
            jobRelationInfo.setHistoryId("");
            jobRelationInfo.setLastStatus("");
            jobRelationInfo.setLastRuntime("");
        } else {
            jobRelationInfo.setJobId(result.getId());
            jobRelationInfo.setHistoryId(his.getId());
            jobRelationInfo.setLastStatus(his.getStatus() == null ? "" : his.getStatus().toString());
            jobRelationInfo.setLastRuntime(his.getStartTime() == null ? null : DateUtil.date2String(his.getStartTime()));
        }
        if (result.getChildren().size() > 0) {
            List<JobRelationInfo> children = new ArrayList<>();
            jobRelationInfo.setChildren(children);
            jobRelationInfo.getChildren().addAll(getChildren(result));
        } else {
            List<JobRelationInfo> children = new ArrayList<>();
            jobRelationInfo.setChildren(children);
        }
        return jobRelationInfo;
    }

    private ArrayList<JobRelationInfo> getChildren(GroupJobTreeModel result) throws JSONException {
        ArrayList<JobRelationInfo> children = new ArrayList<>();
        for (GroupJobTreeModel child : result.getChildren()) {
            children.add(getJsonData(child));
        }
        return children;
    }

    @RequestMapping(value = "/get_depender_tree", method = RequestMethod.GET)
    public CommonResponse<JobRelationInfo> getDependerTreeJson(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        try {
            JobRelationInfo jobRelationInfo = getJsonData(getDependerTree(jobId));
            return this.buildResponse(jobRelationInfo,ReturnCode.SUCCESS);
        } catch (Exception e) {
            log.error("get_depender_tree", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public GroupJobTreeModel getDependeeTree(String jobId) {
        GroupBean globe = readOnlyGroupManagerWithJob.getGlobeGroupBean();
        JobBean jb = globe.getAllSubJobBeans().get(jobId);
        if (jb != null) {
            GroupJobTreeModel root = new GroupJobTreeModel();
            root.setName(jb.getJobDescriptor().getName());
            root.setId(jb.getJobDescriptor().getId());
            root.setGroup(false);
            root.setDirectory(jb.getDependee().isEmpty() ? false : true);
            root.setJob(true);
            root.setOwner(jb.getJobDescriptor().getOwner());

            setJob(root, jb.getDependee(), true);
            return root;
        }
        return null;
    }

    public GroupJobTreeModel getDependerTree(String jobId) {
        GroupBean globe = readOnlyGroupManagerWithJob.getGlobeGroupBean();
        JobBean jb = globe.getAllSubJobBeans().get(jobId);
        if (jb != null) {
            GroupJobTreeModel root = new GroupJobTreeModel();
            root.setName(jb.getJobDescriptor().getName());
            root.setId(jb.getJobDescriptor().getId());
            root.setGroup(false);
            root.setDirectory(jb.getDepender().isEmpty() ? false : true);
            root.setJob(true);
            root.setOwner(jb.getJobDescriptor().getOwner());

            setJob(root, jb.getDepender(), false);
            return root;
        }
        return null;
    }

    private void setJob(GroupJobTreeModel parent, Collection<JobBean> children, boolean dependee) {
        for (JobBean g : children) {
            GroupJobTreeModel job = new GroupJobTreeModel();
            job.setName(g.getJobDescriptor().getName());
            job.setId(g.getJobDescriptor().getId());
            job.setGroup(false);
            job.setJob(true);
            job.setOwner(g.getJobDescriptor().getOwner());
            Boolean dir = false;
            Collection<JobBean> childs = null;
            if (dependee) {
                dir = g.getDependee().isEmpty() ? false : true;
                childs = g.getDependee();
            } else {
                dir = g.getDepender().isEmpty() ? false : true;
                childs = g.getDepender();
            }
            job.setDirectory(dir);
            parent.getChildren().add(job);
            if (!childs.isEmpty()) {
                setJob(job, childs, dependee);
            }
        }
    }
}
