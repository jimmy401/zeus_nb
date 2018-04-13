package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusHostGroup;

import java.util.List;
@MybatisMapper
public interface ZeusHostGroupMapper {

    ZeusHostGroup selectByPrimaryKey(Integer id);

    List<ZeusHostGroup> selectAll();

}