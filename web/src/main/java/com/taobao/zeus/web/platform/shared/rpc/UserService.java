package com.taobao.zeus.web.platform.shared.rpc;

import java.util.List;

import com.taobao.zeus.web.platform.module.PagingLoadConfig;
import com.taobao.zeus.web.platform.module.PagingLoadResult;
import com.taobao.zeus.web.platform.module.ZUser;

public interface UserService {

	ZUser getUser();
	
	List<ZUser> getAllUsers();
	
	List<ZUser> getAllGroupUsers();
	
	PagingLoadResult<ZUser> getUsersPaging(PagingLoadConfig config,
										   String filter);
	
	ZUser updateUser(ZUser zu) throws Exception;
	
	String checkUser(String username,String password);
	
	String checkUserSession();

	void checkpass(List<String> uids) throws Exception;
	
	void checknotpass(List<String> uids) throws Exception;
	
	void delete(List<String> uids) throws Exception;
}
