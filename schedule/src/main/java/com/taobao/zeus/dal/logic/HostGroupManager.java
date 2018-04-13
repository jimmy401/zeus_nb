package com.taobao.zeus.dal.logic;

import com.taobao.zeus.dal.model.ZeusHostGroup;
import com.taobao.zeus.model.HostGroupCache;

import java.util.List;
import java.util.Map;

public interface HostGroupManager {
	public ZeusHostGroup getHostGroupName(String hostGroupId);
	
	public Map<String,HostGroupCache> getAllHostGroupInfomations();
	
	public List<ZeusHostGroup> getAllHostGroup();
	
	public List<String> getPreemptionHost();
}
