package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusFollowUp;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusFollowUpMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusFollowUp record);

    int insertSelective(ZeusFollowUp record);

    ZeusFollowUp selectByPrimaryKey(Long id);

    List<ZeusFollowUp> selectByParams(Map<String,Object> params);

    List<ZeusFollowUp> selectByTargetIds(Map<String,Object> params);

    int updateByPrimaryKeySelective(ZeusFollowUp record);

    int updateByPrimaryKey(ZeusFollowUp record);
}