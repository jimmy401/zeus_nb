package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusLog;

import java.util.List;
import java.util.Map;

@MybatisMapper
public interface ZeusLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusLog record);

    int insertSelective(ZeusLog record);

    ZeusLog selectByPrimaryKey(Long id);

    ZeusLog selectByActionId(String actionId);

    List<ZeusLog> selectByLogType(String logType);

    int updateByPrimaryKeySelective(ZeusLog record);

    int updateByPrimaryKey(ZeusLog record);
}