package com.taobao.zeus.dal.tool;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.model.ZeusGroupWithBLOBs;

public class GroupValidate {
	public static boolean valide(ZeusGroupWithBLOBs group) throws ZeusException{
		if(group.getName()==null || group.getName().trim().equals("")){
			throw new ZeusException("name字段不能为空");
		}
		
		return true;
	}
}
