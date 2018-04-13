package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusHostGroupMapper;
import com.taobao.zeus.dal.mapper.ZeusHostRelationMapper;
import com.taobao.zeus.model.HostGroupCache;
import com.taobao.zeus.dal.logic.HostGroupManager;
import com.taobao.zeus.dal.model.ZeusHostGroup;
import com.taobao.zeus.dal.model.ZeusHostRelation;
import com.taobao.zeus.util.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Repository("mysqlHostGroupManager")
public class MysqlHostGroupManager implements HostGroupManager{
	@Autowired
	ZeusHostGroupMapper zeusHostGroupMapper;

	@Autowired
	ZeusHostRelationMapper zeusHostRelationMapper;

	public List<ZeusHostRelation> getAllHostRelations() {
		List<ZeusHostRelation> all =zeusHostRelationMapper.selectAll();
		return all;
	}
	
	public List<ZeusHostRelation> getHostRelations(final String hostGroupId) {

		Map<String,Object> params = new HashMap<String,Object>();
		params.put("hostGroupId", Integer.valueOf(hostGroupId));
		List<ZeusHostRelation> result = zeusHostRelationMapper.selectByHostGroupId(params);

		return result;
	}
	

	@Override
	public Map<String,HostGroupCache> getAllHostGroupInfomations() {
		Map<String,HostGroupCache> informations = new HashMap<String,HostGroupCache>();
		List<ZeusHostGroup> hostgroups = getAllHostGroup();
		List<ZeusHostRelation> relations = getAllHostRelations();
		for(ZeusHostGroup wg : hostgroups){
			if (wg.getEffective() == 0) {
				continue;
			}
			HostGroupCache info = new HostGroupCache();
			String id = wg.getId().toString();
			info.setId(id);
			info.setCurrentPositon(0);
			info.setName(wg.getName());
			info.setDescription(wg.getDescription());
			List<String> hosts = new ArrayList<String>();
			for(ZeusHostRelation r : relations){
				if (wg.getId().equals(r.getHostGroupId())) {
					hosts.add(r.getHost());
				}
			}
			info.setHosts(hosts);
			informations.put(id, info);
		}
		return informations;
	}
	@Override
	public List<ZeusHostGroup> getAllHostGroup(){
		List<ZeusHostGroup> result=zeusHostGroupMapper.selectAll();
		return result;
	}

	@Override
	public ZeusHostGroup getHostGroupName(String hostGroupId) {
		return zeusHostGroupMapper.selectByPrimaryKey(Integer.valueOf(hostGroupId));
	}

	@Override
	public List<String> getPreemptionHost() {
		String id = Environment.getDefaultMasterGroupId();
		List<ZeusHostRelation> hostRelations = getHostRelations(id);
		List<String> result = new ArrayList<String>();
		for(ZeusHostRelation hostRalation : hostRelations){
			result.add(hostRalation.getHost());
		}
		return result;
	}

}
