package com.taobao.zeus.web.controller;

import com.taobao.zeus.dal.logic.DebugHistoryManager;
import com.taobao.zeus.dal.logic.FileManager;
import com.taobao.zeus.dal.model.ZeusFile;
import com.taobao.zeus.dal.tool.Super;
import com.taobao.zeus.model.DebugHistory;
import com.taobao.zeus.model.ActionDescriptor;
import com.taobao.zeus.socket.protocol.Protocol;
import com.taobao.zeus.socket.worker.ClientWorker;
import com.taobao.zeus.util.DateUtil;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.platform.module.DebugHistoryModel;
import com.taobao.zeus.web.platform.module.PagingLoadConfig;
import com.taobao.zeus.web.platform.module.PagingLoadResult;
import com.taobao.zeus.web.platform.module.PagingLoadResultBean;
import com.taobao.zeus.web.util.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/develop_center")
public class DevelopCenterController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(DevelopCenterController.class);

    @Autowired
    private DebugHistoryManager debugHistoryManager;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ClientWorker worker;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView homePage(HttpServletResponse response) throws Exception {
        return new ModelAndView("developcenter");
    }

    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    public CommonResponse<String> debug(@RequestParam(value = "fileId", defaultValue = "") String fileId,
                                        @RequestParam(value = "mode", defaultValue = "") String mode,
                                        @RequestParam(value = "script", defaultValue = "") String script,
                                        @RequestParam(value = "hostGroupId", defaultValue = "") String hostGroupId) {
        String debugId="";
        try {
            String uid = LoginUser.getUser().getUid();
            ZeusFile fd = fileManager.getFile(Long.valueOf(fileId));
            if (!fd.getOwner().equals(uid)) {
                return this.buildResponse(ReturnCode.INVALID_ERROR, "");
            }
            String now = DateUtil.date2String(new Date());
            DebugHistory history = new DebugHistory();
            history.setFileId(Long.valueOf(fileId));
            history.setOwner(uid);
            history.setJobRunType(ActionDescriptor.JobRunType.parser(mode));
            history.setScript(script);
            history.setHostGroupId(hostGroupId);
            history.setGmtCreate(DateUtil.string2Date(now));
            history.setGmtModified(DateUtil.string2Date(now));
            debugHistoryManager.addDebugHistory(history);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("fileId", fileId);
            params.put("runtype", mode);
            params.put("owner", uid);
            params.put("gmtCreate", now);
            params.put("gmtModified", now);
            DebugHistory newHistory = debugHistoryManager.selectByParams(params);
            debugId = newHistory.getId();

            try {
                worker.executeJobFromWeb(Protocol.ExecuteKind.DebugKind, debugId);
            } catch (Exception e) {
                log.error("execute job from web failed.", e);
            }

        } catch (Exception e) {
            log.error("debug failed.", e);
        }
        return this.buildResponse(ReturnCode.SUCCESS, debugId);
    }

    @RequestMapping(value = "/get_log", method = RequestMethod.POST)
    public CommonResponse<DebugHistoryModel> getHistoryModel(@RequestParam(value = "debugId", defaultValue = "") String debugId) {
        DebugHistory his = debugHistoryManager.findDebugHistory(debugId);
        DebugHistoryModel model = convert(his);
        return this.buildResponse(ReturnCode.SUCCESS,model);
    }

    private DebugHistoryModel convert(DebugHistory his) {
        DebugHistoryModel ret = new DebugHistoryModel();
        ret.setEndTime(his.getEndTime());
        ret.setExecuteHost(his.getExecuteHost());
        ret.setFileId(his.getFileId());
        ret.setGmtCreate(his.getGmtCreate());
        ret.setGmtModified(his.getGmtModified());
        ret.setId(his.getId());
        if (his.getJobRunType() != null)
            ret.setJobRunType(DebugHistoryModel.JobRunType.parser(his
                    .getJobRunType().toString()));
        ret.setLog(his.getLog().getContent());
        ret.setScript(his.getScript());
        ret.setStartTime(his.getStartTime());
        if (his.getStatus() != null)
            ret.setStatus(DebugHistoryModel.Status.parser(his.getStatus()
                    .getId()));
        return ret;
    }

    public String getLog(String debugId) {
        return debugHistoryManager.findDebugHistory(debugId).getLog()
                .getContent();
    }

    public String getStatus(String debugId) {
        return debugHistoryManager.findDebugHistory(debugId).getStatus()
                .getId();
    }

    public PagingLoadResult<DebugHistoryModel> getDebugHistory(
            PagingLoadConfig config, String fileId) {
        int total = debugHistoryManager.pagingTotal(fileId);
        List<DebugHistory> historyList = debugHistoryManager.pagingList(fileId,
                config.getOffset(), config.getLimit());
        List<DebugHistoryModel> modelList = convert(historyList);

        return new PagingLoadResultBean<DebugHistoryModel>(modelList, total,
                config.getOffset());
    }

    private List<DebugHistoryModel> convert(List<DebugHistory> list) {
        List<DebugHistoryModel> ret = new ArrayList<DebugHistoryModel>();
        for (DebugHistory his : list)
            ret.add(convert(his));
        return ret;
    }

    public void cancelDebug(String debugId) throws Exception {
        String uid = LoginUser.getUser().getUid();
        DebugHistory his = debugHistoryManager.findDebugHistory(debugId);
        ZeusFile fd = fileManager.getFile(his.getFileId());
        if (!fd.getOwner().equals(uid) && !Super.getSupers().contains(uid)) {
            throw new RuntimeException("您无权操作\nuid=" + uid + " fileOwner="
                    + fd.getOwner());
        }
        try {
            worker.cancelJobFromWeb(Protocol.ExecuteKind.DebugKind, debugId, LoginUser
                    .getUser().getUid());
        } catch (Exception e) {
            log.error("cancelDebug error", e);
            throw new Exception(e.getMessage());
        }
    }
}
