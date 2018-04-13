package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusHostRelation;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusHostRelationMapper {

    List<ZeusHostRelation> selectAll();

    List<ZeusHostRelation> selectByHostGroupId(Map<String,Object> map);

}