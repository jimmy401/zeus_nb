package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusActionBakMapper;
import com.taobao.zeus.dal.model.ZeusActionBakWithBLOBs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("mysqlZeusActionBak")
public class MysqlZeusActionBak {

    @Autowired
    ZeusActionBakMapper zeusActionBakMapper;

    public ZeusActionBakWithBLOBs selectByPrimaryKey(Long id) {
        return zeusActionBakMapper.selectByPrimaryKey(id);
    }

    public void updateByPrimaryKeySelective(ZeusActionBakWithBLOBs item){
        zeusActionBakMapper.updateByPrimaryKeySelective(item);
    }

    public void insertSelective(ZeusActionBakWithBLOBs item){
        zeusActionBakMapper.insertSelective(item);
    }
}
