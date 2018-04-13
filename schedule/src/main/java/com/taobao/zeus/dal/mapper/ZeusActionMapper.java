package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusAction;
import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusJobStatistic;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusActionMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusActionWithBLOBs record);

    int insertSelective(ZeusActionWithBLOBs record);

    ZeusActionWithBLOBs selectByPrimaryKey(Long id);

    List<ZeusActionWithBLOBs> selectByGroupId(Map<String,Object> params);

    List<ZeusActionWithBLOBs> selectByParams(Map<String,Object> params);

    List<ZeusActionWithBLOBs> findActionWithIds(List<Long> list);

    List<ZeusActionWithBLOBs> selectByJobId(Map<String,Object> params);

    List<ZeusActionWithBLOBs> selectByActionId(Map<String,Object> params);

    int updateByPrimaryKeySelective(ZeusActionWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ZeusActionWithBLOBs record);

    int updateByPrimaryKey(ZeusAction record);
}