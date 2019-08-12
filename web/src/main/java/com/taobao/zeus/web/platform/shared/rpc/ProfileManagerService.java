package com.taobao.zeus.web.platform.shared.rpc;

import com.taobao.zeus.web.platform.module.ProfileModel;

import java.util.Map;

public interface ProfileManagerService{

	void updateHadoopConf(Map<String, String> conf) throws Exception;
	
	ProfileModel getProfile();
}
