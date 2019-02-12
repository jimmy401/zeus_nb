package com.taobao.zeus.web.controller;

import com.taobao.zeus.dal.logic.impl.MysqlLogManager;
import com.taobao.zeus.dal.model.ZeusLog;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.GridContent;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.util.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/job_check")
public class JobCheckListController  extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(JobCheckListController.class);

    @Autowired
    @Qualifier("mysqlLogManager")
    private MysqlLogManager mysqlLogManager;

    @RequestMapping(value = "/get_all_list", method = RequestMethod.POST)
    public GridContent getAllList() {
        GridContent gridcontent = new GridContent();
        try {
            List<ZeusLog> ret = mysqlLogManager.selectLogByLogType("update_job");
            int totalCount = 500;

            gridcontent.rows = ret;
            gridcontent.total = totalCount;
        } catch (Exception e) {
            log.error("get job check list failed!", e);
        }
        return gridcontent;
    }

    @RequestMapping(value = "/check_result", method = RequestMethod.POST)
    public CommonResponse<Void> checkResult(@RequestParam(value = "id") String id,
                                            @RequestParam(value = "status") String status,
                                            @RequestParam(value = "checkInfo") String checkInfo ) {
        try {
            ZeusLog item= new ZeusLog();
            item.setId(Long.valueOf(id));
            item.setStatus(Integer.valueOf(status));
            item.setCheckTime(new Date());
            item.setDescription(checkInfo);
            item.setCheckUid(LoginUser.getUser().getUid());

            mysqlLogManager.updateByPrimaryKeySelective(item);
            return this.buildResponse();
        } catch (Exception e) {
            log.error("check job result failed!", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }
}


