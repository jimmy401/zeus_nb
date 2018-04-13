package com.taobao.zeus.dal.mapper;

import com.taobao.zeus.annotation.MybatisMapper;
import com.taobao.zeus.dal.model.BigIdWithAction;
import com.taobao.zeus.dal.model.ZeusActionHistory;
import com.taobao.zeus.dal.model.ZeusActionReport;

import java.util.List;
import java.util.Map;
@MybatisMapper
public interface ZeusActionHistoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ZeusActionHistory record);

    int insertSelective(ZeusActionHistory record);

    ZeusActionHistory selectByPrimaryKey(Long id);

    List<ZeusActionHistory> selectRecent(Map<String,Object> map);

    int countByJobId(Map<String,Object> map);

    List<BigIdWithAction> findIdsWithActionId(List<String> list);

    List<ZeusActionHistory> selectByBigIds(List<Long> list);

    List<ZeusActionHistory> selectByParams(Map<String,Object> map);

    List<ZeusActionReport> selectSuccessReportByDate(Map<String,Object> map);

    List<ZeusActionReport> selectFailedReportByDate(Map<String,Object> map);

    List<ZeusActionReport> selectOwnerFailedReportByDate(Map<String,Object> map);

    List<ZeusActionReport> selectFailedActionByDate(Map<String,Object> map);

    List<ZeusActionHistory> find(Map<String,Object> map);

    int updateByPrimaryKeySelective(ZeusActionHistory record);

    int updateByPrimaryKeyWithBLOBs(ZeusActionHistory record);

    int updateByPrimaryKey(ZeusActionHistory record);
}