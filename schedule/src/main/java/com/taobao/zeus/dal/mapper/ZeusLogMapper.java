package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusLog;
@MybatisMapper
public interface ZeusLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusLog record);

    int insertSelective(ZeusLog record);

    ZeusLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ZeusLog record);

    int updateByPrimaryKey(ZeusLog record);
}