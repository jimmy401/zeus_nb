package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusPermission;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusPermissionMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusPermission record);

    int insertSelective(ZeusPermission record);

    ZeusPermission selectByPrimaryKey(Long id);

    List<ZeusPermission> selectByTypes(Map<String,Object> params);

    List<ZeusPermission> selectByUid(Map<String,Object> params);

    int updateByPrimaryKeySelective(ZeusPermission record);

    int updateByPrimaryKey(ZeusPermission record);
}