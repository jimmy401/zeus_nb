package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.logic.ZeusLockManager;
import com.taobao.zeus.dal.mapper.ZeusLockMapper;
import com.taobao.zeus.dal.model.ZeusLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MysqlZeusLockManager implements ZeusLockManager{

    @Autowired
    ZeusLockMapper zeusLockMapper;

    @Override
    public ZeusLock selectBySubGroup(String subGroup) {
        return zeusLockMapper.selectBySubGroup(subGroup);
    }

    @Override
    public int updateByPrimaryKeySelective(ZeusLock record) {
        return zeusLockMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int insertSelective(ZeusLock record) {
        return zeusLockMapper.insertSelective(record);
    }
}
