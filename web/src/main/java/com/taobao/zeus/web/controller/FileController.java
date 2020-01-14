package com.taobao.zeus.web.controller;

import com.taobao.zeus.dal.logic.FileManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.logic.impl.MysqlFileManager;
import com.taobao.zeus.dal.logic.impl.MysqlLogManager;
import com.taobao.zeus.dal.model.ZeusFile;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.dal.tool.Super;
import com.taobao.zeus.model.LogDescriptor;
import com.taobao.zeus.util.ContentUtil;
import com.taobao.zeus.util.Environment;
import com.taobao.zeus.web.common.CurrentUser;
import com.taobao.zeus.web.controller.response.CommonResponse;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.taobao.zeus.dal.model.ZeusUser.ADMIN;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private MysqlFileManager mysqlFileManager;

    @Autowired
    @Qualifier("mysqlLogManager")
    private MysqlLogManager mysqlLogManager;

    @Autowired
    @Qualifier("mysqlUserManager")
    private UserManager mysqlUserManager;

    @RequestMapping(value = "/personal_files", method = RequestMethod.GET)
    public CommonResponse<List<ZeusFile>> getPersonalFiles() {
        try {
            String uid = CurrentUser.getUser().getUid();

            List<ZeusFile> fileList = mysqlFileManager.getPersonalFiles(uid);
            ZeusFile item = getTreeData(fileList);
            List<ZeusFile> ret = new ArrayList<>();
            ret.add(item);
            return this.buildResponse(ret);
        } catch (Exception e) {
            log.error("load personal files tree data failed.", e);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR, null);
        }
    }

    private ZeusFile getTreeData(List<ZeusFile> list) {
        ZeusFile root = new ZeusFile();
        root.setName("文件夹");
        root.setId(2L);
        root.setType(ZeusFile.FOLDER);
        for (ZeusFile item : list) {
            if (item.getParent() == null) {
                if (root.getChildren() == null) {
                    List<ZeusFile> children = new ArrayList<>();
                    root.setChildren(children);
                }
                root.getChildren().add(item);
            } else {
                setChildren(item, list);
            }
        }
        return root;
    }

    private void setChildren(ZeusFile item, List<ZeusFile> list) {
            for (ZeusFile one : list) {
                if (item.getParent().longValue() == one.getId().longValue()) {
                    if (one.getType() == ZeusFile.FOLDER && one.getChildren() == null) {
                        List<ZeusFile> subChildren = new ArrayList<>();
                        one.setChildren(subChildren);
                    }
                    one.getChildren().add(item);
                }
            }
    }

    @RequestMapping(value = "/add_file", method = RequestMethod.GET)
    public CommonResponse<Void> addFile(@RequestParam(value = "parentId", defaultValue = "") Long parentId,
                                        @RequestParam(value = "name", defaultValue = "") String name,
                                        @RequestParam(value = "folder", defaultValue = "") Boolean folder) {
        try {
            log.info("add file info,parent id :" + parentId + "name :" + name + "folder：" + folder);
            String uid = CurrentUser.getUser().getUid();
            ZeusFile parent = mysqlFileManager.getFile(parentId);
            if (Super.getSupers().contains(uid) || parent.getOwner().equalsIgnoreCase(uid)) {
                mysqlFileManager.addFile(uid, parentId, name, folder);
                return this.buildResponse(ReturnCode.SUCCESS);
            } else {
                return this.buildResponse(ReturnCode.INVALID_ERROR);
            }
        } catch (Exception ex) {
            return this.buildResponse(ReturnCode.FAILED);
        }
    }

    @RequestMapping(value = "/delete_file", method = RequestMethod.GET)
    public CommonResponse<Void> deleteFile(@RequestParam(value = "fileId", defaultValue = "") Long fileId) {
        ZeusFile fd = mysqlFileManager.getFile(fileId);
        String user = CurrentUser.getUser().getUid();
        if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
            if (fd.getParent() == null && fd.getName().equals(FileManager.SHARE)) {
                return this.buildResponse(ReturnCode.INVALID_ERROR);
            }
            recursionDelete(fd);
            LogDescriptor log = new LogDescriptor();
            log.setCreateTime(new Date());
            log.setUserName(user);
            log.setLogType("delete_file");
            log.setUrl(fd.getName());

            mysqlLogManager.addLog(log);
        } else {
            return this.buildResponse(ReturnCode.INVALID_ERROR);
        }
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    private void recursionDelete(ZeusFile parent) {
        if (parent.getType()==ZeusFile.FOLDER) {
            List<ZeusFile> subs = mysqlFileManager.getSubFiles(parent.getId());
            for (ZeusFile fd : subs) {
                recursionDelete(fd);
            }
        }
        mysqlFileManager.deleteFile(parent.getId());
    }

    @RequestMapping(value = "/update_file_content", method = RequestMethod.POST)
    public CommonResponse<String> updateFileContent(@RequestParam(value = "fileId", defaultValue = "") Long fileId,
                                  @RequestParam(value = "content", defaultValue = "") String content) {
        try {
            ZeusFile fd = mysqlFileManager.getFile(fileId);
            String user = CurrentUser.getUser().getUid();
            if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
                if (!ADMIN.getUid().equalsIgnoreCase(user) && ContentUtil.containInvalidContent(content)) {
                    throw new RuntimeException("没有数据仓库DDL权限！");
                } else if (!ADMIN.getUid().equalsIgnoreCase(user) && ContentUtil.containRmCnt(content) != ContentUtil.contentValidRmCnt(content, Environment.getZeusSafeDeleteDir())) {
                    throw new RuntimeException("不能使用rm删除非许可文件路径！");
                } else {
                    fd.setContent(content);
                    mysqlFileManager.update(fd);
                }

                return this.buildResponse(ReturnCode.SUCCESS,"");
            } else {
                return this.buildResponse(ReturnCode.INVALID_ERROR,"");
            }
        }catch (Exception ex){
            return this.buildResponse(ReturnCode.FAILED,"");
        }
    }

    @RequestMapping(value = "/update_file_name", method = RequestMethod.GET)
    public CommonResponse<Void> updateFileName(
            @RequestParam(value = "fileId", defaultValue = "") Long fileId,
            @RequestParam(value = "name", defaultValue = "") String name) {
        try {
            ZeusFile fd = mysqlFileManager.getFile(fileId);
            String user = CurrentUser.getUser().getUid();
            if (fd.getParent() == null) {
                return this.buildResponse(ReturnCode.INVALID_ERROR);
            }
            if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
                fd.setName(name);
                mysqlFileManager.update(fd);
                return this.buildResponse(ReturnCode.SUCCESS);
            } else {
                return this.buildResponse(ReturnCode.INVALID_ERROR);
            }
        } catch (Exception ex) {
            return this.buildResponse(ReturnCode.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/move_file", method = RequestMethod.GET)
    public CommonResponse<Void> moveFile(@RequestParam(value = "sourceId", defaultValue = "") Long sourceId,
                                         @RequestParam(value = "targetId", defaultValue = "") Long targetId) {
        try {
            ZeusFile source = mysqlFileManager.getFile(sourceId);
            ZeusFile target = mysqlFileManager.getFile(targetId);
            String uid = CurrentUser.getUser().getUid();
            if (target.getType()==ZeusFile.FOLDER && target.getOwner().equals(source.getOwner())) {
                if (Super.getSupers().contains(uid) || (source.getOwner().equalsIgnoreCase(uid) && target.getOwner().equalsIgnoreCase(uid))) {
                    source.setParent(target.getId());
                    mysqlFileManager.update(source);
                } else {
                    return this.buildResponse(ReturnCode.INVALID_ERROR);
                }
            } else {
                return this.buildResponse(ReturnCode.TARGET_ERROR);
            }
            return  this.buildResponse(ReturnCode.SUCCESS);
        } catch (Exception ex) {
            log.error("move file to target failed.", ex);
            return this.buildResponse(ReturnCode.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/common_files", method = RequestMethod.GET)
    public List<ZeusFile> getCommonFiles(ZeusFile fm) {

        return null;
    }

    private boolean hasCommonFiles(ZeusUser zu) {
        List<ZeusFile> files = mysqlFileManager.getUserFiles(zu.getUid());
        for (ZeusFile fd : files) {
            if (fd.getName().equalsIgnoreCase(FileManager.SHARE)) {
                List<ZeusFile> fds = mysqlFileManager.getSubFiles(fd.getId());
                if (fds == null || fds.isEmpty()) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    @RequestMapping(value = "/update_host_group", method = RequestMethod.GET)
    public void updateHostGroupId(
            @RequestParam(value = "fileId", defaultValue = "") Long fileId,
            @RequestParam(value = "hostGroupId", defaultValue = "") Integer hostGroupId) {
        ZeusFile fd = mysqlFileManager.getFile(fileId);
        String user = LoginUser.getUser().getUid();
        if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
            fd.setHostGroupId(hostGroupId);
            mysqlFileManager.update(fd);
        } else {
            throw new RuntimeException("权限不足");
        }
    }

    @RequestMapping(value = "/get_file_content", method = RequestMethod.GET)
    public CommonResponse<String> getFileContent(@RequestParam(value = "fileId", defaultValue = "") Long fileId) {
        ZeusFile fd = mysqlFileManager.getFile(fileId);
        String user = CurrentUser.getUser().getUid();
        if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
            return this.buildResponse(ReturnCode.SUCCESS,fd.getContent());
        } else {
            return this.buildResponse(ReturnCode.INVALID_ERROR,"");
        }
    }
}
