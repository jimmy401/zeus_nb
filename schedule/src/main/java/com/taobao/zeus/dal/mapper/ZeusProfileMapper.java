package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusProfile;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusProfileMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusProfile record);

    int insertSelective(ZeusProfile record);

    ZeusProfile selectByPrimaryKey(Long id);

    List<ZeusProfile> selectByUid(Map<String,Object> params);

    int updateByPrimaryKeySelective(ZeusProfile record);

    int updateByPrimaryKey(ZeusProfile record);
}