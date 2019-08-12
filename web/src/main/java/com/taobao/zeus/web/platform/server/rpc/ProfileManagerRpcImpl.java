package com.taobao.zeus.web.platform.server.rpc;

import java.util.Map;

import com.taobao.zeus.dal.logic.ProfileManager;
import com.taobao.zeus.web.platform.module.ProfileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.zeus.model.Profile;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.platform.shared.rpc.ProfileManagerService;
import org.springframework.stereotype.Service;

@Service("profile.rpc")
public class ProfileManagerRpcImpl implements ProfileManagerService{
	private static Logger log=LoggerFactory.getLogger(ProfileManagerRpcImpl.class);
	@Autowired
	private ProfileManager profileManager;
	@Override
	public void updateHadoopConf(Map<String, String> conf) throws Exception{
		String uid=LoginUser.getUser().getUid();
		Profile p=profileManager.findByUid(uid);
		if(p==null){
			p=new Profile();
		}
		p.setHadoopConf(conf);
		try {
			profileManager.update(uid, p);
		} catch (Exception e) {
			log.error("updateHadoopConf",e);
			throw new Exception(e.getMessage());
		}
	}
	@Override
	public ProfileModel getProfile() {
		Profile p=profileManager.findByUid(LoginUser.getUser().getUid());
		if(p==null){
			try {
				profileManager.update(LoginUser.getUser().getUid(), new Profile());
				p=profileManager.findByUid(LoginUser.getUser().getUid());
			} catch (Exception e) {
				log.error("getProfile",e);
			}
		}
		if(p!=null){
			ProfileModel pm=new ProfileModel();
			pm.setUid(LoginUser.getUser().getUid());
			pm.setHadoopConf(p.getHadoopConf());
			return pm;
		}
		return null;
	}
	
}
