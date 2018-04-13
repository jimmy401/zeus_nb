package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusGroup;
import com.taobao.zeus.dal.model.ZeusGroupStatistic;
import com.taobao.zeus.dal.model.ZeusGroupWithBLOBs;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusGroupMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ZeusGroupWithBLOBs record);

    int insertSelective(ZeusGroupWithBLOBs record);

    List<ZeusGroupWithBLOBs> findByParent(Map<String,Object> map);

    List<ZeusGroupWithBLOBs> findByRoot(Map<String,Object> map);

    List<ZeusGroupWithBLOBs> selectByParams(Map<String,Object> map);

    ZeusGroupWithBLOBs selectFirstOne();

    ZeusGroupWithBLOBs selectByPrimaryKey(Integer id);

    ZeusGroupStatistic selectGroupStatistic();

    List<ZeusGroupWithBLOBs> selectGreatThanModified(Map<String,Object> map);

    int updateByPrimaryKeySelective(ZeusGroupWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ZeusGroupWithBLOBs record);

    int updateByPrimaryKey(ZeusGroup record);
}