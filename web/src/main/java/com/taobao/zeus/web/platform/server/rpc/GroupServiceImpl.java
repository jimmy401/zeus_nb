
package com.taobao.zeus.web.platform.server.rpc;

import java.util.*;

import com.taobao.zeus.dal.logic.FollowManagerWithJob;
import com.taobao.zeus.dal.logic.PermissionManager;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.logic.impl.MysqlLogManager;
import com.taobao.zeus.dal.model.ZeusGroupWithBLOBs;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.model.LogDescriptor;
import com.taobao.zeus.web.platform.module.GroupModel;
import com.taobao.zeus.web.platform.module.ZUser;
import com.taobao.zeus.web.util.PermissionGroupManagerWithJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.model.GroupDescriptor;
import com.taobao.zeus.model.ZeusFollow;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.platform.shared.rpc.GroupService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("group.rpc")
public class GroupServiceImpl implements GroupService{
	private static Logger log=LoggerFactory.getLogger(GroupServiceImpl.class);
	@Autowired
	private PermissionGroupManagerWithJob permissionGroupManagerWithJob;
	@Autowired
	private FollowManagerWithJob followManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PermissionManager permissionManager;

	@Autowired
	@Qualifier("mysqlLogManager")
	private MysqlLogManager mysqlLogManager;

	@Override
	public String createGroup(String groupName, String parentGroupId,
			boolean isDirectory) throws Exception {
		try {
			ZeusGroupWithBLOBs gd= permissionGroupManagerWithJob.createGroup(LoginUser.getUser().getUid(), groupName, parentGroupId, isDirectory);
			return gd.getId().toString();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void deleteGroup(String groupId) throws Exception {
		try {
			permissionGroupManagerWithJob.deleteGroup(LoginUser.getUser().getUid(), groupId);
			String user=LoginUser.getUser().getUid();
			LogDescriptor log = new LogDescriptor();
			log.setCreateTime(new Date());
			log.setUserName(user);
			log.setLogType("delete_group");
			log.setIp(groupId);

			mysqlLogManager.addLog(log);
		} catch (ZeusException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public GroupModel getGroup(String groupId) throws Exception {
		ZeusGroupWithBLOBs gd= permissionGroupManagerWithJob.getGroupDescriptor(groupId);
		GroupModel model=new GroupModel();
		model.setLocalResources(gd.getFileResources());
		model.setLocalProperties(gd.getProperties());
		model.setDesc(gd.getDescr());
		model.setDirectory(gd.getbDirectory());
		model.setId(gd.getId().toString());
		model.setName(gd.getName());
		model.setOwner(gd.getOwner());
		model.setParent(gd.getParent().toString());
		model.setAdmin(permissionGroupManagerWithJob.hasGroupPermission(LoginUser.getUser().getUid(), groupId));
		List<ZeusFollow> follows= followManager.findGroupFollowers(Arrays.asList(groupId));
		if(follows!=null){
			List<String> followsName=new ArrayList<String>();
			for(ZeusFollow zf:follows){
				String name=userManager.findByUidFilter(zf.getUid()).getName();
				if(name==null){
					name=zf.getUid();
				}
				followsName.add(name);
			}
			model.setFollows(followsName);
		}
		return model;
	}
	
	
	public GroupModel getUpstreamGroup(String groupId) throws Exception{
		GroupBean bean= permissionGroupManagerWithJob.getUpstreamGroupBean(groupId);
		ZeusGroupWithBLOBs gd=bean.getGroupDescriptor();
		GroupModel model=new GroupModel();
		model.setParent(bean.getParentGroupBean()==null?null:bean.getParentGroupBean().getGroupDescriptor().getId().toString());
		model.setLocalResources(gd.getFileResources());
		model.setAllResources(bean.getHierarchyResources());
		model.setLocalProperties(new HashMap<String, String>(gd.getProperties()));
		model.setDesc(gd.getDescr());
		model.setDirectory(gd.getbDirectory());
		model.setId(gd.getId().toString());
		model.setName(gd.getName());
		model.setOwner(gd.getOwner());
		String ownerName=userManager.findByUid(gd.getOwner()).getName();
		if(ownerName==null || "".equals(ownerName.trim()) || "null".equals(ownerName)){
			ownerName=gd.getOwner();
		}
		model.setOwnerName(ownerName);
		model.setParent(gd.getParent().toString());
		model.setAllProperties(bean.getHierarchyProperties().getAllProperties());
		model.setAdmin(permissionGroupManagerWithJob.hasGroupPermission(LoginUser.getUser().getUid(), groupId));
		List<ZeusFollow> follows= followManager.findGroupFollowers(Arrays.asList(groupId));
		if(follows!=null){
			List<String> followsName=new ArrayList<String>();
			for(ZeusFollow zf:follows){
				String name=userManager.findByUid(zf.getUid()).getName();
				if(name==null || "".equals(name.trim())){
					name=zf.getUid();
				}
				followsName.add(name);
			}
			model.setFollows(followsName);
		}
		
		List<String> ladmins=permissionManager.getGroupAdmins(bean.getGroupDescriptor().getId().toString());
		List<String> admins=new ArrayList<String>();
		for(String s:ladmins){
			String name=userManager.findByUid(s).getName();
			if(name==null || "".equals(name.trim())){
				name=s;
			}
			admins.add(name);
		}
		model.setAdmins(admins);
		
		List<String> owners=new ArrayList<String>();
		owners.add(bean.getGroupDescriptor().getOwner());
		GroupBean parent=bean.getParentGroupBean();
		while(parent!=null){
			if(!owners.contains(parent.getGroupDescriptor().getOwner())){
				owners.add(parent.getGroupDescriptor().getOwner());
			}
			parent=parent.getParentGroupBean();
		}
		model.setOwners(owners);
		
		//所有secret. 开头的配置项都进行权限控制
		for(String key:model.getAllProperties().keySet()){
			boolean isLocal=model.getLocalProperties().get(key)==null?false:true;
			if(key.startsWith("secret.")){
				if(!isLocal){
					model.getAllProperties().put(key, "*");
				}else{
					if(!model.isAdmin() && !model.getOwner().equals(LoginUser.getUser().getUid())){
						model.getLocalProperties().put(key, "*");
					}
				}
			}
		}
		//本地配置项中的hadoop.hadoop.job.ugi 只有管理员和owner才能查看，继承配置项不能查看
		String SecretKey="hadoop.hadoop.job.ugi";
		if(model.getLocalProperties().containsKey(SecretKey)){
			String value=model.getLocalProperties().get(SecretKey);
			if(value.lastIndexOf("#")==-1){
				value="*";
			}else{
				value=value.substring(0, value.lastIndexOf("#"));
				value+="#*";
			}
			if(!model.isAdmin() && !model.getOwner().equals(LoginUser.getUser().getUid())){
				model.getLocalProperties().put(SecretKey, value);
			}
			model.getAllProperties().put(SecretKey, value);
		}else if(model.getAllProperties().containsKey(SecretKey)){
			String value=model.getAllProperties().get(SecretKey);
			if(value.lastIndexOf("#")==-1){
				value="*";
			}else{
				value=value.substring(0, value.lastIndexOf("#"));
				value+="#*";
			}
			model.getAllProperties().put(SecretKey, value);
		}
		return model;
	}



	@Override
	public void updateGroup(GroupModel group) throws Exception {
		ZeusGroupWithBLOBs gd=new ZeusGroupWithBLOBs();
		gd.setFileResources(group.getLocalResources());
		gd.setDescr(group.getDesc());
		gd.setId(Integer.valueOf(group.getId()));
		gd.setName(group.getName());
		gd.setProperties(group.getLocalProperties());
		gd.setExisted(1);
		
		try {
			permissionGroupManagerWithJob.updateGroup(LoginUser.getUser().getUid(), gd);
		} catch (ZeusException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void addGroupAdmin(String groupId, String uid) throws Exception {
		try {
			permissionGroupManagerWithJob.addGroupAdmin(LoginUser.getUser().getUid(),uid, groupId);
		} catch (ZeusException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public List<ZUser> getGroupAdmins(String groupId) {
		List<ZeusUser> users= permissionGroupManagerWithJob.getGroupAdmins(groupId);
		List<ZUser> result=new ArrayList<ZUser>();
		for(ZeusUser zu:users){
			ZUser z=new ZUser();
			z.setName(zu.getName());
			z.setUid(zu.getUid());
			result.add(z);
		}
		return result;
	}

	@Override
	public void removeGroupAdmin(String groupId, String uid)
			throws Exception {
		try {
			permissionGroupManagerWithJob.removeGroupAdmin(LoginUser.getUser().getUid(),uid, groupId);
		} catch (ZeusException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void transferOwner(String groupId, String uid) throws Exception {
		try {
			permissionGroupManagerWithJob.grantGroupOwner(LoginUser.getUser().getUid(), uid, groupId);
		} catch (ZeusException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void move(String groupId, String newParentGroupId)
			throws Exception {
		try {
			permissionGroupManagerWithJob.moveGroup(LoginUser.getUser().getUid(), groupId, newParentGroupId);
		} catch (ZeusException e) {
			log.error("move",e);
			throw new Exception(e.getMessage());
		}
	}

}