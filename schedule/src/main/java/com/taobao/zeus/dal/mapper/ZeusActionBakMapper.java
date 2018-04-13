package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusActionBak;
import com.taobao.zeus.dal.model.ZeusActionBakWithBLOBs;
@MybatisMapper
public interface ZeusActionBakMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusActionBakWithBLOBs record);

    int insertSelective(ZeusActionBakWithBLOBs record);

    ZeusActionBakWithBLOBs selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ZeusActionBakWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ZeusActionBakWithBLOBs record);

    int updateByPrimaryKey(ZeusActionBak record);
}