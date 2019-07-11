package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.mapper.ZeusUserMapper;
import com.taobao.zeus.dal.model.ZeusUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@SuppressWarnings("unchecked")
@Repository("mysqlUserManager")
public class MysqlUserManager implements UserManager {

    @Autowired
    ZeusUserMapper zeusUserMapper;

    private static Logger log = LoggerFactory.getLogger(MysqlUserManager.class);

    @Override
    public List<ZeusUser> selectPageByParams(int page, int rows, String uid) {
        Map<String, Object> params = new HashMap<>();
        int start = (page - 1) * rows;
        int end = page * rows - 1;
        params.put("start", start);
        params.put("end", end);
        params.put("uid", uid);
        List<ZeusUser> list = zeusUserMapper.selectPageByParams(params);

        return list;
    }

    @Override
    public int selectRecordCountByParams(String... uid) {
        Map<String, Object> params = new HashMap<>();
        if (uid.length > 0 &&!uid[0].equals("")) {
            params.put("uid", uid[0]);
        }
        int count = zeusUserMapper.selectRecordCountByParams(params);

        return count;
    }

    public List<ZeusUser> getAllEffectiveUsers() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("isEffective", 1);
        List<ZeusUser> list = zeusUserMapper.selectByParams(params);

        return list;
    }

    public List<ZeusUser> getAllUsers() {
        List<ZeusUser> list = zeusUserMapper.selectAllUsers();
        return list;
    }

    @Override
    public List<ZeusUser> getAllJobsNeedAlertUsers() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("isEffective", 1);
        params.put("isAllJobsNeedAlert", 1);
        List<ZeusUser> list = zeusUserMapper.selectByParams(params);

        return list;
    }

    public ZeusUser findByUid(String uid) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", uid);
        List<ZeusUser> list = zeusUserMapper.selectByParams(params);

        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<ZeusUser> findListByUid(final List<String> uids) {
        if (uids.isEmpty()) {
            return new ArrayList<ZeusUser>();
        }
        List<ZeusUser> list = zeusUserMapper.selectByUids(uids);
        return list;
    }

    public ZeusUser addOrUpdateUser(final ZeusUser user) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", user.getUid());
        List<ZeusUser> list = zeusUserMapper.selectByParams(params);

        if (list != null && !list.isEmpty()) {
            ZeusUser zu = list.get(0);
            zu.setEmail(user.getEmail());
            zu.setWangwang(user.getWangwang());
            zu.setName(user.getName());
            if (user.getPhone() != null && !"".equals(user.getPhone())) {
                zu.setPhone(user.getPhone());
            }
            zu.setGmtModified(new Date());
            zu.setIsEffective(user.getIsEffective());
            zu.setUserType(user.getUserType());
            zu.setDescription(user.getDescription());
            zeusUserMapper.updateByPrimaryKeySelective(zu);
        } else {
            user.setGmtCreate(new Date());
            user.setGmtModified(new Date());
            zeusUserMapper.insertSelective(user);
        }
        return user;
    }

    @Override
    public List<ZeusUser> findListByUidByOrder(final List<String> uids) {
        List<ZeusUser> result = new ArrayList<ZeusUser>();
        if (uids.isEmpty()) {
            return result;
        }
        List<ZeusUser> users = findListByUid(uids);
        for (String uid : uids) {
            for (ZeusUser user : users) {
                if (uid.equals(user.getUid())) {
                    result.add(user);
                }
            }
        }
        return result;
    }

    /**
     * 2015-02-04
     **/
    public ZeusUser findByUidFilter(String uid) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", uid);
        params.put("isEffective", 1);
        List<ZeusUser> list = zeusUserMapper.selectByParams(params);

        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<ZeusUser> findAllUsers(final String sortField, final String sortOrder) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sortField", sortField);
        params.put("sortOrder", sortOrder);
        List<ZeusUser> list = zeusUserMapper.selectAll(params);
        return list;
    }

    public List<ZeusUser> getPageAllUsers(int page, int rows) {
        Map<String, Object> params = new HashMap<>();
        int start = (page - 1) * rows;
        int end = page * rows - 1;
        params.put("start", start);
        params.put("end", end);
        List<ZeusUser> list = zeusUserMapper.selectPageByParams(params);

        return list;
    }

    public List<ZeusUser> findListByFilter(final String filter, final String sortField, final String sortOrder) {
        if (filter.isEmpty()) {
            return new ArrayList<ZeusUser>();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", "%" + filter + "%");
        params.put("name", "%" + filter + "%");
        params.put("email", "%" + filter + "%");
        params.put("sortField", sortField);
        params.put("sortOrder", sortOrder);
        List<ZeusUser> list = zeusUserMapper.selectByFilter(params);
        return list;

    }

    @Override
    public void update(ZeusUser user) {
        user.setGmtModified(new Date());
        zeusUserMapper.updateByPrimaryKeySelective(user);
    }
}
