package com.taobao.zeus.dal.logic;

import com.taobao.zeus.dal.model.ZeusLock;

public interface ZeusLockManager {

	ZeusLock selectBySubGroup(String subGroup);

	int updateByPrimaryKeySelective(ZeusLock record);

	int insertSelective(ZeusLock record);
}
