package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusLock;

@MybatisMapper
public interface ZeusLockMapper {
    int insertSelective(ZeusLock record);

    ZeusLock selectBySubGroup(String subgroup);

    int updateByPrimaryKeySelective(ZeusLock record);
}