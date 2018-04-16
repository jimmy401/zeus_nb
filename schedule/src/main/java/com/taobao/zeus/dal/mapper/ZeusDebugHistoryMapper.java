package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusDebugHistory;
import com.taobao.zeus.dal.model.ZeusDebugHistoryWithBLOBs;

import java.util.List;
import java.util.Map;

@MybatisMapper
public interface ZeusDebugHistoryMapper {

    int insertSelective(ZeusDebugHistoryWithBLOBs record);

    ZeusDebugHistoryWithBLOBs selectByPrimaryKey(Long id);

    ZeusDebugHistoryWithBLOBs selectByParams(Map<String,Object> params);

    List<ZeusDebugHistoryWithBLOBs> find(Map<String,Object> map);

    int pagingTotal(Long fileId);

    int updateByPrimaryKeyWithBLOBs(Map<String,Object> map);

    int updateByPrimaryKeySelective(ZeusDebugHistoryWithBLOBs record);
}