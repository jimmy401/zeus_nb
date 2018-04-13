package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusWorker;

import java.util.List;

@MybatisMapper
public interface ZeusWorkerMapper {
    int deleteByPrimaryKey(String host);

    int insert(ZeusWorker record);

    int insertSelective(ZeusWorker record);

    ZeusWorker selectByPrimaryKey(String host);

    List<ZeusWorker> selectAll();

    int updateByPrimaryKeySelective(ZeusWorker record);

    int updateByPrimaryKey(ZeusWorker record);
}