package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("mysqlZeusAction")
public class MysqlZeusAction {

    @Autowired
    ZeusActionMapper zeusActionMapper;

    public void deleteByPrimaryKey(Long id) {
        zeusActionMapper.deleteByPrimaryKey(id);
    }
}
