package com.taobao.zeus.web.platform.shared.rpc;

import com.taobao.zeus.web.platform.module.GroupModel;
import com.taobao.zeus.web.platform.module.ZUser;

import java.io.IOException;
import java.util.List;

public interface GroupService {
	/**
	 * 创建一个分组
	 * @param group
	 * @throws ServiceException
	 * @throws IOException
	 */
	public String createGroup(String groupName,String parentGroupId,boolean isDirectory) throws Exception;
	/**
	 * 根据名称获取相应的分组
	 * @param name
	 * @return
	 * @throws ServiceException
	 */
	GroupModel getGroup(String groupId) throws Exception;
	/**
	 * 
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	public GroupModel getUpstreamGroup(String groupId) throws Exception;

	/**
	 * 删除分组
	 * @param user
	 * @param groupName
	 * @throws ServiceException
	 */
	void deleteGroup(String groupId) throws Exception;
	/**
	 * 更新组信息
	 * @param group
	 * @throws Exception
	 */
	void updateGroup(GroupModel group) throws Exception;
	
	List<ZUser> getGroupAdmins(String groupId);
	
	void addGroupAdmin(String groupId,String uid) throws Exception;
	
	void removeGroupAdmin(String groupId,String uid) throws Exception;
	
	void transferOwner(String groupId,String uid) throws Exception;
	/**
	 * 移动组
	 * @param groupId
	 * @param newParentGroupId
	 * @throws Exception
	 */
	void move(String groupId,String newParentGroupId) throws Exception;
}
