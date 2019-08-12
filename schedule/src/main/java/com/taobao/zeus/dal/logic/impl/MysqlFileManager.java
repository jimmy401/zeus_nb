package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusFileMapper;
import com.taobao.zeus.dal.model.ZeusFile;
import com.taobao.zeus.dal.tool.PersistenceAndBeanConvertWithAction;
import com.taobao.zeus.model.FileDescriptor;
import com.taobao.zeus.dal.logic.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository("mysqlFileManager")
public class MysqlFileManager implements FileManager {

    private static Logger log = LoggerFactory.getLogger(MysqlFileManager.class);

    private static Object locker = new Object();

    @Autowired
    ZeusFileMapper zeusFileMapper;

    @Override
    public void addFile(String uid, Long parentId, String name, boolean folder) {
        ZeusFile fp = new ZeusFile();
        fp.setName(name);
        fp.setOwner(uid);
        fp.setParent(Long.valueOf(parentId));
        fp.setType(folder ? ZeusFile.FOLDER : ZeusFile.FILE);
        zeusFileMapper.insertSelective(fp);
    }

    @Override
    public void deleteFile(Long fileId) {
        zeusFileMapper.deleteByPrimaryKey(fileId);
    }

    @Override
    public ZeusFile getFile(Long id) {
        return zeusFileMapper.selectByPrimaryKey(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ZeusFile> getSubFiles(Long id) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parent", id);

        List<ZeusFile> fps = zeusFileMapper.findByParent(params);

        return fps;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ZeusFile> getUserFiles(String uid) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("owner", uid);

        List<ZeusFile> list = new ArrayList<ZeusFile>();

        synchronized (locker) {
            list = zeusFileMapper.findByOwner(params);
            if (list == null || list.isEmpty()) {
                if (list == null) {
                    list = new ArrayList<ZeusFile>();
                }
                ZeusFile personal = new ZeusFile();
                personal.setName(PERSONAL);
                personal.setOwner(uid);
                personal.setType(ZeusFile.FOLDER);
                personal.setCategory(PERSON);
                ZeusFile common = new ZeusFile();
                common.setName(SHARE);
                common.setOwner(uid);
                common.setType(ZeusFile.FOLDER);
                common.setCategory(PUBLIC);

                zeusFileMapper.insertSelective(personal);
                zeusFileMapper.insertSelective(common);

                Map<String, Object> p_params = new HashMap<String, Object>();
                p_params.put("name", PERSONAL);
                p_params.put("owner", uid);
                p_params.put("type", ZeusFile.FOLDER);

                Map<String, Object> s_params = new HashMap<String, Object>();
                s_params.put("name", SHARE);
                s_params.put("owner", uid);
                s_params.put("type", ZeusFile.FOLDER);
                personal = zeusFileMapper.selectByParams(p_params);
                common = zeusFileMapper.selectByParams(s_params);

                list.add(personal);
                list.add(common);
            }

        }
        return list;
    }

    public List<ZeusFile> getPersonalFiles(String uid) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("owner", uid);
        List<ZeusFile> result = zeusFileMapper.selectTreeByOwner(params);
        return result;
    }

    @Override
    public void update(ZeusFile fd) {
        fd.setGmtModified(new Date());
        zeusFileMapper.updateByPrimaryKeySelective(fd);
    }

}
