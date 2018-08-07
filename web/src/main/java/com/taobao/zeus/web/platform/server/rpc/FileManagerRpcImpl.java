package com.taobao.zeus.web.platform.server.rpc;

import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;

import com.taobao.zeus.dal.logic.FileManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.logic.impl.MysqlLogManager;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.dal.tool.Super;
import com.taobao.zeus.model.LogDescriptor;
import com.taobao.zeus.util.ContentUtil;
import com.taobao.zeus.util.Environment;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.taobao.zeus.model.FileDescriptor;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.platform.client.module.filemanager.FileModel;
import com.taobao.zeus.web.platform.shared.rpc.FileClientBean;
import com.taobao.zeus.web.platform.shared.rpc.FileManagerService;
import org.springframework.stereotype.Service;

@Service("file.rpc")
public class FileManagerRpcImpl implements FileManagerService {
    private static Logger log = LoggerFactory.getLogger(FileManager.class);
    @Autowired
    private FileManager fileManager;
    @Autowired
    @Qualifier("mysqlUserManager")
    private UserManager userManager;

    @Autowired
    @Qualifier("mysqlLogManager")
    private MysqlLogManager mysqlLogManager;

    @Override
    public FileModel addFile(String parentId, String name, boolean folder) {
        log.info("add file info,parent id :" + parentId + "name :" + name + "folder：" + folder);
        String uid = LoginUser.getUser().getUid();
        FileDescriptor parent = fileManager.getFile(parentId);
        if (Super.getSupers().contains(uid) || parent.getOwner().equalsIgnoreCase(uid)) {
            FileDescriptor fd = fileManager.addFile(uid, parentId, name, folder);
            return convert(fd);
        } else {
            throw new RuntimeException("权限不足");
        }
    }

    private FileModel convert(FileDescriptor fd) {
        FileModel fm = new FileModel();
		/*if(fd==null){
			return fm;
		}*/
        fm.setContent(fd.getContent());
        fm.setFolder(fd.isFolder());
        fm.setId(fd.getId());
        fm.setName(fd.getName());
        fm.setParentId(fd.getParent());
        fm.setOwner(fd.getOwner());
        fm.setHostGroupId(fd.getHostGroupId());
        if (LoginUser.getUser().getUid().equals(fd.getOwner())) {
            fm.setAdmin(true);
        } else {
            fm.setAdmin(false);
        }
        return fm;
    }

    @Override
    public void deleteFile(String fileId) {
        FileDescriptor fd = fileManager.getFile(fileId);
        String user = LoginUser.getUser().getUid();
        if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
            if (fd.getParent() == null && fd.getName().equals(FileManager.SHARE)) {
                throw new RuntimeException("此目录不得删除");
            }
            recursionDelete(fd);
            LogDescriptor log = new LogDescriptor();
            log.setCreateTime(new Date());
            log.setUserName(user);
            log.setLogType("delete_file");
            log.setUrl(fd.getName());

            mysqlLogManager.addLog(log);
        } else {
            throw new RuntimeException("权限不足");
        }

    }

    private void recursionDelete(FileDescriptor parent) {
        if (parent.isFolder()) {
            List<FileDescriptor> subs = fileManager.getSubFiles(parent.getId());
            for (FileDescriptor fd : subs) {
                recursionDelete(fd);
            }
        }
        fileManager.deleteFile(parent.getId());
    }


    @Override
    public FileModel getFile(String id) {
        log.info("getFile by param id :{}", id);
        FileDescriptor fd = fileManager.getFile(id);
        return convert(fd);
    }

    @Override
    public void updateFileContent(String fileId, String content) {
        FileDescriptor fd = fileManager.getFile(fileId);
        String user = LoginUser.getUser().getUid();
        if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
            if (ContentUtil.containInvalidContent(content)){
                throw new RuntimeException("没有数据仓库DDL权限！");
            }
            else if (ContentUtil.containRmCnt(content)!=ContentUtil.contentValidRmCnt(content,Environment.getZeusSafeDeleteDir())){
                throw new RuntimeException("不能使用rm删除非许可文件路径！");
            }
            else {
                fd.setContent(content);
                fileManager.update(fd);
            }
        } else {
            throw new RuntimeException("权限不足");
        }
    }

    @Override
    public void updateFileName(String fileId, String name) {
        FileDescriptor fd = fileManager.getFile(fileId);
        String user = LoginUser.getUser().getUid();
        if (fd.getParent() == null) {
            throw new RuntimeException("不允许修改此文件夹名称");
        }
        if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
            fd.setName(name);
            fileManager.update(fd);
        } else {
            throw new RuntimeException("权限不足");
        }

    }

    @Override
    public FileClientBean getUserFiles() {
        List<FileDescriptor> files = fileManager.getUserFiles(LoginUser.getUser().getUid());
        FileModel root = new FileModel();
        root.setName("根节点，不展示");
        FileClientBean bean = new FileClientBean(root);
        for (FileDescriptor fd : files) {
            FileClientBean nodebean = new FileClientBean(convert(fd));
            nodebean.setParent(bean);
            bean.addSubFile(nodebean);
            recursion(nodebean);
        }
        return bean;
    }

    private void recursion(FileClientBean parent) {
        FileModel model = parent.getFileModel();
        if (model.isFolder()) {
            List<FileDescriptor> fds = fileManager.getSubFiles(model.getId());
            for (FileDescriptor fd : fds) {
                FileClientBean bean = new FileClientBean(convert(fd));
                bean.setParent(parent);
                parent.addSubFile(bean);
                recursion(bean);
            }
        }
        return;
    }

    @Override
    public void moveFile(String sourceId, String targetId) {
        FileDescriptor source = fileManager.getFile(sourceId);
        FileDescriptor target = fileManager.getFile(targetId);
        String uid = LoginUser.getUser().getUid();
        if (target.isFolder() && target.getOwner().equals(source.getOwner())) {
            if (Super.getSupers().contains(uid) || (source.getOwner().equalsIgnoreCase(uid) && target.getOwner().equalsIgnoreCase(uid))) {
                source.setParent(target.getId());
                fileManager.update(source);
            } else {
                throw new RuntimeException("权限不足");
            }
        } else {
            throw new RuntimeException("目标地址不是文件夹,无法移动");
        }

    }

    @Override
    public List<FileModel> getCommonFiles(FileModel fm) {
        List<FileModel> result = new ArrayList<FileModel>();
        if (fm == null) {
            List<ZeusUser> users = userManager.getAllUsers();
            for (ZeusUser zu : users) {
                //没有公共文档则不返回该用户
                if (!hasCommonFiles(zu)) {
                    continue;
                }
                FileModel model = new FileModel();
                model.setAdmin(false);
                model.setFolder(true);
                model.setId(zu.getUid());
                model.setName(zu.getName() + "(" + zu.getUid() + ")");
                model.setOwner(zu.getUid());

                result.add(model);
            }
        } else if (fm.getId().contains("\\")) {
            List<FileDescriptor> files = fileManager.getUserFiles(fm.getId());
            for (FileDescriptor fd : files) {
                if (fd.getName().equalsIgnoreCase(FileManager.SHARE)) {
                    FileModel model = new FileModel();
                    model.setAdmin(false);
                    model.setFolder(fd.isFolder());
                    model.setId(fd.getId());
                    model.setName(fd.getName());
                    model.setOwner(fd.getOwner());
                    model.setParentId(fm.getId());
                    model.setHostGroupId(fm.getHostGroupId());
                    result.add(model);
                }
            }
        } else {
            List<FileDescriptor> files = fileManager.getSubFiles(fm.getId());
            for (FileDescriptor fd : files) {
                FileModel model = convert(fd);
                result.add(model);
            }
        }
        return result;
    }

    /**
     * 判断用户是不是有公共文档
     *
     * @param zu
     * @return
     */
    private boolean hasCommonFiles(ZeusUser zu) {
        List<FileDescriptor> files = fileManager.getUserFiles(zu.getUid());
        for (FileDescriptor fd : files) {
            if (fd.getName().equalsIgnoreCase(FileManager.SHARE)) {
                List<FileDescriptor> fds = fileManager.getSubFiles(fd.getId());
                if (fds == null || fds.isEmpty()) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public FileModel getHomeFile(String id) {

        log.error("HomeFildId:" + id);
        FileModel homeTemplate = getFile(id);
        log.error("homeTemplate:" + homeTemplate);
        log.error("homeTemplate:" + homeTemplate.getContent());

        String content = homeTemplate.getContent();
        ZeusUser user = LoginUser.getUser();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", user.getUid());
        String out = RenderHomePage(content, params);
        homeTemplate.setContent(out);
        return homeTemplate;

    }

    private String RenderHomePage(String in, Map<String, Object> params) {
        try {
            VelocityEngine engine = new VelocityEngine();
            Properties props = new Properties();
            props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            props.put("runtime.log.logsystem.log4j.category", "velocity");
            props.put("runtime.log.logsystem.log4j.logger", "velocity");
            VelocityContext context = new VelocityContext();

            for (Entry<String, Object> entry : params.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
            StringWriter out = new StringWriter();
            engine.init(props);
            engine.evaluate(context, out, "", in);
            return out.toString();
        } catch (Exception e) {
            log.error("render error", e);
            return in;
        }
    }


    @Override
    public void updateHostGroupId(String fileId, String hostGroupId) {
        FileDescriptor fd = fileManager.getFile(fileId);
        String user = LoginUser.getUser().getUid();
        if (Super.getSupers().contains(user) || fd.getOwner().equalsIgnoreCase(user)) {
            fd.setHostGroupId(hostGroupId);
            fileManager.update(fd);
        } else {
            throw new RuntimeException("权限不足");
        }
    }

}
