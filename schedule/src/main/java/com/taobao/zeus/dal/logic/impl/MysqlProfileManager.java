package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.logic.ProfileManager;
import com.taobao.zeus.dal.mapper.ZeusProfileMapper;
import com.taobao.zeus.dal.model.ZeusProfile;
import com.taobao.zeus.dal.tool.PersistenceAndBeanConvertWithAction;
import com.taobao.zeus.model.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("mysqlProfileManager")
public class MysqlProfileManager implements ProfileManager {

    @Autowired
    ZeusProfileMapper zeusProfileMapper;

    @Override
    public Profile findByUid(final String uid) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", uid);
        List<ZeusProfile> list = zeusProfileMapper.selectByUid(params);
        if(list==null||list.size()==0){
            return null;
        }
        return PersistenceAndBeanConvertWithAction.convert(list.get(0));
    }

    @Override
    public void update(String uid, Profile p) throws Exception {
        Profile old = findByUid(uid);
        if (old == null) {
            old = new Profile();
            old.setUid(uid);
            zeusProfileMapper.insertSelective(PersistenceAndBeanConvertWithAction.convert(old));
            old = findByUid(uid);
        }
        p.setUid(old.getUid());
        p.setGmtModified(new Date());
        zeusProfileMapper.updateByPrimaryKeySelective(PersistenceAndBeanConvertWithAction.convert(p));
    }
}
