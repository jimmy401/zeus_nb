package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusFile;

import java.util.List;
import java.util.Map;

@MybatisMapper
public interface ZeusFileMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusFile record);

    int insertBatch(List<ZeusFile> records);

    int insertSelective(ZeusFile record);

    ZeusFile selectByPrimaryKey(Long id);

    ZeusFile selectByParams(Map<String,Object> params);

    List<ZeusFile> findByParent(Map<String,Object> map);

    List<ZeusFile> findByOwner(Map<String,Object> map);

    int updateByPrimaryKeySelective(ZeusFile record);

    int updateByPrimaryKeyWithBLOBs(ZeusFile record);

    int updateByPrimaryKey(ZeusFile record);
}