package com.taobao.zeus.dal.logic;

import com.taobao.zeus.dal.model.ZeusUser;

import java.util.List;


public interface UserManager {

	 List<ZeusUser> selectPageByParams(int page,int end,String uid);

	 int selectRecordCountByParams(String... uid);

	 List<ZeusUser> getAllEffectiveUsers();

	List<ZeusUser> getAllUsers();

	 List<ZeusUser> getAllJobsNeedAlertUsers();

	 ZeusUser findByUid(String uid);
	
	 List<ZeusUser> findListByUid(List<String> uids);
	
	 ZeusUser addOrUpdateUser(ZeusUser user);
	/**
	 * 按照uids的顺序返回user列表
	 * @param uids
	 * @return
	 */
	 List<ZeusUser> findListByUidByOrder(List<String> uids);
	
	//2015-02-04 add--------
	 ZeusUser findByUidFilter(String uid);
	
	 List<ZeusUser> findAllUsers(String sortField, String sortOrder);

	 List<ZeusUser> getPageAllUsers(int page, int rows);
	
	 List<ZeusUser> findListByFilter(String filter, String sortField, String sortOrder);
	
	 void update(ZeusUser user);
}
