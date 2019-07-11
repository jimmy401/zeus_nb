package com.taobao.zeus.web.controller;

import com.taobao.zeus.dal.logic.impl.MysqlFileManager;
import com.taobao.zeus.dal.model.ZeusFile;
import com.taobao.zeus.web.common.CurrentUser;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.util.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private MysqlFileManager mysqlFileManager;

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

    private ZeusFile getTreeData(List<ZeusFile> list){
        ZeusFile root = new ZeusFile();
        root.setName("文件夹");
        root.setId(0L);
        for (ZeusFile item:list) {
            if (item.getParent()==null){
                if (root.getChildren()==null){
                    List<ZeusFile> children = new ArrayList<>();
                    root.setChildren(children);
                }
                root.getChildren().add(item);
            }else
            {
                setChildren(item,root);
            }
        }
        return root;
    }

    private void setChildren(ZeusFile item,ZeusFile root){
        if (root.getChildren()!=null){
            for (ZeusFile children: root.getChildren()) {
                if (children.getId()==item.getParent()){
                    if (children.getChildren()==null)
                    {
                        List<ZeusFile> subChildren = new ArrayList<>();
                        children.setChildren(subChildren);
                    }
                    children.getChildren().add(item);
                }
            }
        }
    }
}
