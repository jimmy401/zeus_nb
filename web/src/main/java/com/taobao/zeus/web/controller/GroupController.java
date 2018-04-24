package com.taobao.zeus.web.controller;

import com.taobao.zeus.dal.logic.FollowManagerWithJob;
import com.taobao.zeus.dal.logic.JobHistoryManager;
import com.taobao.zeus.dal.logic.PermissionManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.logic.impl.ReadOnlyGroupManagerWithJob;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.ZeusFollow;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.platform.client.module.jobmanager.GroupModel;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.util.PermissionGroupManagerWithJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController extends BaseController{
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
    private FollowManagerWithJob followManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private PermissionManager permissionManager;

    @RequestMapping(value = "/get_upstream_group", method = RequestMethod.GET)
    public CommonResponse<GroupModel> getUpstreamGroup(@RequestParam(value = "groupId") String groupId) throws Exception {
        try {

            GroupBean bean = permissionGroupManagerWithJob.getUpstreamGroupBean(groupId);
            GroupDescriptor gd = bean.getGroupDescriptor();
            GroupModel model = new GroupModel();
            model.setParent(bean.getParentGroupBean() == null ? null : bean.getParentGroupBean().getGroupDescriptor().getId());
            model.setLocalResources(gd.getResources());
            model.setAllResources(bean.getHierarchyResources());
            model.setLocalProperties(new HashMap<String, String>(gd.getProperties()));
            model.setDesc(gd.getDesc());
            model.setDirectory(gd.isDirectory());
            model.setId(gd.getId());
            model.setName(gd.getName());
            model.setOwner(gd.getOwner());
            String ownerName = userManager.findByUid(gd.getOwner()).getName();
            if (ownerName == null || "".equals(ownerName.trim()) || "null".equals(ownerName)) {
                ownerName = gd.getOwner();
            }
            model.setOwnerName(ownerName);
            model.setParent(gd.getParent());
            model.setAllProperties(bean.getHierarchyProperties().getAllProperties());
            model.setAdmin(permissionGroupManagerWithJob.hasGroupPermission(LoginUser.getUser().getUid(), groupId));
            List<ZeusFollow> follows = followManager.findGroupFollowers(Arrays.asList(groupId));
            if (follows != null) {
                List<String> followsName = new ArrayList<String>();
                for (ZeusFollow zf : follows) {
                    String name = userManager.findByUid(zf.getUid()).getName();
                    if (name == null || "".equals(name.trim())) {
                        name = zf.getUid();
                    }
                    followsName.add(name);
                }
                model.setFollows(followsName);
            }

            List<String> ladmins = permissionManager.getGroupAdmins(bean.getGroupDescriptor().getId());
            List<String> admins = new ArrayList<String>();
            for (String s : ladmins) {
                String name = userManager.findByUid(s).getName();
                if (name == null || "".equals(name.trim())) {
                    name = s;
                }
                admins.add(name);
            }
            model.setAdmins(admins);

            List<String> owners = new ArrayList<String>();
            owners.add(bean.getGroupDescriptor().getOwner());
            GroupBean parent = bean.getParentGroupBean();
            while (parent != null) {
                if (!owners.contains(parent.getGroupDescriptor().getOwner())) {
                    owners.add(parent.getGroupDescriptor().getOwner());
                }
                parent = parent.getParentGroupBean();
            }
            model.setOwners(owners);

            //所有secret. 开头的配置项都进行权限控制
            for (String key : model.getAllProperties().keySet()) {
                boolean isLocal = model.getLocalProperties().get(key) == null ? false : true;
                if (key.startsWith("secret.")) {
                    if (!isLocal) {
                        model.getAllProperties().put(key, "*");
                    } else {
                        if (!model.isAdmin() && !model.getOwner().equals(LoginUser.getUser().getUid())) {
                            model.getLocalProperties().put(key, "*");
                        }
                    }
                }
            }
            //本地配置项中的hadoop.hadoop.job.ugi 只有管理员和owner才能查看，继承配置项不能查看
            String SecretKey = "hadoop.hadoop.job.ugi";
            if (model.getLocalProperties().containsKey(SecretKey)) {
                String value = model.getLocalProperties().get(SecretKey);
                if (value.lastIndexOf("#") == -1) {
                    value = "*";
                } else {
                    value = value.substring(0, value.lastIndexOf("#"));
                    value += "#*";
                }
                if (!model.isAdmin() && !model.getOwner().equals(LoginUser.getUser().getUid())) {
                    model.getLocalProperties().put(SecretKey, value);
                }
                model.getAllProperties().put(SecretKey, value);
            } else if (model.getAllProperties().containsKey(SecretKey)) {
                String value = model.getAllProperties().get(SecretKey);
                if (value.lastIndexOf("#") == -1) {
                    value = "*";
                } else {
                    value = value.substring(0, value.lastIndexOf("#"));
                    value += "#*";
                }
                model.getAllProperties().put(SecretKey, value);
            }
            return this.buildResponse(model);

        } catch (Exception e) {
            log.error("load my group upstream data failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }
}
