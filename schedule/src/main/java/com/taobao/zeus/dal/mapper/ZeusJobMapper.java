package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@MybatisMapper
public interface ZeusJobMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusJobWithBLOBs record);

    int insertSelective(ZeusJobWithBLOBs record);

    ZeusJobWithBLOBs selectByPrimaryKey(Long id);

    List<ZeusJobShort> selectLikeName(@Param("name")String name);

    List<ZeusJobWithBLOBs> selectByParams(Map<String,Object> params);

    List<ZeusJobWithBLOBs> findJobWithIds(List<Long> list);

    List<ZeusJobShort> findJobShortWithIds(List<Long> list);

    List<ZeusJobWithBLOBs> selectAll();

    List<ZeusJobWithBLOBs> selectGreatThanGmtModified(Map<String,Object> params);

    ZeusJobStatistic selectJobStatistic();

    int updateByPrimaryKeySelective(ZeusJobWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ZeusJobWithBLOBs record);

    int updateByPrimaryKey(ZeusJob record);
}