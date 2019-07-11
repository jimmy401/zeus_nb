package com.taobao.zeus.web.controller;

import com.taobao.zeus.dal.mapper.ZeusJobMapper;
import com.taobao.zeus.dal.model.ZeusActionShort;
import com.taobao.zeus.web.util.PermissionGroupManagerWithAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/action")
public class ActionController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(ActionController.class);

    @Autowired
    private ZeusJobMapper zeusJobMapper;

    @Autowired
    private PermissionGroupManagerWithAction permissionGroupManagerWithAction;

    @RequestMapping(value = "/get_action_list_by_job_id", method = RequestMethod.POST)
    public List<ZeusActionShort> getJobLikeName(@RequestParam(value = "jobId") Long jobId) {
        List<ZeusActionShort> result = new ArrayList<>();
        try {
            List<Long> actionList = permissionGroupManagerWithAction.getJobACtion(jobId.toString());
            for (Long item:actionList){
                result.add(new ZeusActionShort(item));
            }
        } catch (Exception e) {
            log.error("get action list by job id failed.", e);
        }
        return result;
    }
}
