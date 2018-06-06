package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusUser;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusUser record);

    int insertSelective(ZeusUser record);

    ZeusUser selectByPrimaryKey(Long id);

    List<ZeusUser> selectByParams(Map<String,Object> params);

    List<ZeusUser> selectByUids(List<String> uids);

    List<ZeusUser> selectAll(Map<String,Object> params);

    List<ZeusUser> selectByFilter(Map<String,Object> params);

    int updateByPrimaryKeySelective(ZeusUser record);

    int updateByPrimaryKey(ZeusUser record);
}