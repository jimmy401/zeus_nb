package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.ZeusTimeZone;
@MybatisMapper
public interface ZeusTimeZoneMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ZeusTimeZone record);

    int insertSelective(ZeusTimeZone record);

    ZeusTimeZone selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ZeusTimeZone record);

    int updateByPrimaryKey(ZeusTimeZone record);
}