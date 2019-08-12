package com.taobao.zeus.web.platform.server.rpc;

import com.taobao.zeus.broadcast.alarm.MailAlarm;
import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.dal.logic.GroupManagerWithAction;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.dal.tool.Super;
import com.taobao.zeus.web.platform.module.PagingLoadConfig;
import com.taobao.zeus.web.platform.module.PagingLoadResult;
import com.taobao.zeus.web.platform.module.PagingLoadResultBean;
import com.taobao.zeus.web.platform.module.ZUser;
import com.taobao.zeus.web.util.LoginUser;
import com.taobao.zeus.web.platform.shared.rpc.UserService;
import com.taobao.zeus.web.util.EncryptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service("user.rpc")
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserManager userManager;

    @Autowired
    @Qualifier("mysqlGroupManagerWithAction")
    private GroupManagerWithAction groupManager;

    @Override
    public ZUser getUser() {
        ZeusUser u = LoginUser.getUser();// System.out.println(u);
        ZeusUser user = userManager.findByUid(u.getUid());
        ZUser zu = transform(user);
        zu.setSuper(Super.getSupers().contains(u.getUid()));
        return zu;
    }

    private ZUser transform(ZeusUser u) {
        ZUser zu = new ZUser();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        zu.setName(u.getName());
        zu.setUid(u.getUid());
        zu.setIsEffective(transformIsEffective(u.getIsEffective()));
        zu.setUserType(transformUserType(u.getUserType()));
        zu.setDescription(u.getDescription());
        zu.setEmail(u.getEmail());
        zu.setPhone(u.getPhone());
        if (u.getGmtModified() != null) {
            zu.setGmtModified(df.format(u.getGmtModified()));
        }
        // zu.setSuper(Super.getSupers().contains(u.getUid()));
        return zu;
    }

//	private ZeusUser transform(ZUser zu) {
//		ZeusUser u = new ZeusUser();
//		u.setName(zu.getName());
//		u.setUid(zu.getUid());
//		// u.setUserType(zu.getUserType());
//		u.setDescription(zu.getDescription());
//		// u.setIsEffective(zu.getEffective());
//		u.setEmail(zu.getEmail());
//		u.setPhone(zu.getPhone());
//		return u;
//	}

    @Override
    public List<ZUser> getAllUsers() {
        List<ZUser> result = new ArrayList<ZUser>();
        List<ZeusUser> list = userManager.getAllEffectiveUsers();
        for (ZeusUser u : list) {
            // ZUser zu=new ZUser();
            // zu.setName(u.getName());
            // zu.setUid(u.getUid());
            // zu.setUserType(u.getUserType());
            result.add(transform(u));
        }
        return result;
    }

    @Override
    public String checkUser(String username, String password) {
        // ZeusUser u= LoginUser.getUser();
        // HttpServletRequest httpRequest
        ZeusUser u = userManager.findByUidFilter(username);
        // System.out.println(u);
        if (null == u) {
            return "null";
        } else {
            String ps = u.getPassword();
            // System.out.println(password);
            // System.out.println(ps);
            // System.out.println(MD5(password));
            if (null != ps) {
                if (!EncryptHelper.MD5(password).toUpperCase().equals(ps.toUpperCase())) {
                    return "error";
                }
            }
            // ZeusUser zeusUser=null;
            // zeusUser=new ZeusUser();
            // zeusUser.setEmail(u.getEmail());
            // zeusUser.setUid(u.getUid());
            // zeusUser.setName(u.getName());
            // zeusUser.setPhone(u.getPhone());
            String uid = u.getUid();
            // if(!uid.equals(httpRequest.getSession().getAttribute("user"))){
            // userManager.addOrUpdateUser(zeusUser);
            // httpRequest.getSession().setAttribute("user", zeusUser.getUid());
            // }
            // LoginUser.user.set(zeusUser);
            ZeusUser.USER.setUid(uid);
            ZeusUser.USER.setEmail(u.getEmail());
            ZeusUser.USER.setName(u.getName());
            ZeusUser.USER.setPhone(u.getPhone());

            return uid;
        }

    }

    public String checkUserSession() {
        ZeusUser u = LoginUser.getUser();
        if (null == u) {
            return "null";
        }
        String uid = u.getUid();

        return uid;

    }

    @Override
    public ZUser updateUser(ZUser zu) throws Exception {
        String uid = zu.getUid();
        if (hasPermission(uid)) {
            ZeusUser u = userManager.findByUid(uid);
            u.setName(zu.getName());
            u.setDescription(zu.getDescription());
            u.setEmail(zu.getEmail());
            u.setPhone(zu.getPhone());
            userManager.update(u);
            return transform(userManager.findByUid(uid));
        } else {
            throw new Exception("您无权操作");
        }
    }

    private boolean hasPermission(String uid) {
        if (LoginUser.getUser().getUid().equals(uid)
                || ZeusUser.ADMIN.getUid().equals(LoginUser.getUser().getUid())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasAdminPermission() {
        if (ZeusUser.ADMIN.getUid().equals(LoginUser.getUser().getUid())) {
            return true;
        }
        return false;
    }

    @Override
    public List<ZUser> getAllGroupUsers() {
        List<ZUser> result = new ArrayList<ZUser>();
        List<ZeusUser> list = userManager.getAllEffectiveUsers();
        for (ZeusUser u : list) {
            //if (u.getUserType() == 0) {
                result.add(transform(u));
            //}
        }
        return result;
    }

    private static String transformIsEffective(int isEffective) {
        String result = null;
//		if (isEffective == 1) {
//			result = "审核通过";
//		} else if (isEffective == -2) {
//			result = "审核拒绝";
//		} else if (isEffective == 0) {
//			result = "待审核";
//		} else if (isEffective == -1) {
//			result = "已删除";
//		}
        if (isEffective == ZeusUser.UserStatus.CHECK_SUCCESS.value()) {
            result = "审核通过";
        } else if (isEffective == ZeusUser.UserStatus.CHECK_FAILED.value()) {
            result = "审核拒绝";
        } else if (isEffective == ZeusUser.UserStatus.WAIT_CHECK.value()) {
            result = "待审核";
        } else if (isEffective == ZeusUser.UserStatus.Cancel.value()) {
            result = "已删除";
        }
        return result;
    }

    private static String transformUserType(int type) {
        if (type == 1) {
            return "个人用户";
        } else {
            return "组用户";
        }
    }

    @Override
    public PagingLoadResult<ZUser> getUsersPaging(PagingLoadConfig config, String filter) {
        ZeusUser user = LoginUser.getUser();
        if (ZeusUser.ADMIN.getUid().equals(user.getUid())) {
            int start = config.getOffset();
            int limit = config.getLimit();
            String field = "gmtModified";
            String order = "desc";
            List<ZeusUser> list = null;
            if (filter != null && filter.trim().length() > 0) {
                list = userManager.findListByFilter(filter, field, order);
            } else {
                list = userManager.findAllUsers(field, order);
            }
            int total = list.size();
            if (start >= total) {
                start = 0;
            }
            list = list.subList(start, Math.min(start + limit, total));
            List<ZUser> result = new ArrayList<ZUser>();
            for (ZeusUser u : list) {
                result.add(transform(u));
            }
            return new PagingLoadResultBean(result, total, start);
        }
        return null;
    }

    private void checkpass(String uid) throws Exception {
        ZeusUser user = userManager.findByUid(uid);
        user.setIsEffective(ZeusUser.UserStatus.CHECK_SUCCESS.value());
        userManager.update(user);
        ZeusUser newUser = userManager.findByUid(uid);
        if (newUser.getIsEffective() == ZeusUser.UserStatus.CHECK_SUCCESS.value()) {
            //1.审核通过后,给组帐号添加大目录
            if (newUser != null && newUser.getUserType() == 0) {
                String rootGroupId = groupManager.getRootGroupId();
                if (rootGroupId != null) {
                    try {
                        if (!groupManager.IsExistedBelowRootGroup(newUser.getUid())) {
                            groupManager.createGroup(newUser.getUid(), newUser.getUid(), rootGroupId, true);
                        } else {
                            log.warn("根目录下一层已存在同名组" + newUser.getUid());
                        }
                    } catch (ZeusException e) {
                        throw new Exception("创建group异常");
                    }
                }
            }
            //2.给用户发邮件，告知审核已通过
            List<String> mailUsers = new ArrayList<String>();
            mailUsers.add(ZeusUser.ADMIN.getUid());
            mailUsers.add(newUser.getUid());
            MailAlarm mailAlarm = new MailAlarm();
            List<String> emails = getEmailsByUsers(mailUsers);
            if (emails != null && emails.size() > 0) {
                mailAlarm.sendEmail("", emails, "Zeus新用户审核已通过",
                        "Dear All," +
                                "\r\n	Zeus新用户审核已通过，详细信息如下：" +
                                "\r\n		用户类别：" + (newUser.getUserType() == 0 ? "组用户" : "个人用户") +
                                "\r\n		用户账号：" + newUser.getUid() +
                                "\r\n		用户姓名：" + newUser.getName() +
                                "\r\n		用户邮箱：" + newUser.getEmail() +
                                "\r\n	请确认，谢谢！");
            }
            //3.给用户增加Kerberos权限
//            if (Environment.getKerberosAuth()){
//                giveKerberosAuth(user);
//            }
        }
    }

    private void giveKerberosAuth(ZeusUser user) {

        String script1 = "sudo /usr/sbin/groupadd " + user.getUid();
        String script2 = "sudo /usr/sbin/useradd " + user.getUid() + " -g " + user.getUid();
        String script3 = "sudo sh -c \"echo '0 1 * * * kinit -kt /home/" + user.getKerberosUser() + ".keytab " +
                user.getKerberosUser() + "' > /var/spool/cron/tabs/"+user.getUid()+"\"";
        String script4="sudo chown root:"+user.getUid()+ " /var/spool/cron/tabs/" + user.getUid();
        String script5="sudo -u "+user.getUid()+" kinit -kt /home/"+user.getKerberosUser()+".keytab "+ user.getKerberosUser();

        execShell(script1);
        execShell(script2);
        execShell(script3);
        execShell(script4);
        execShell(script5);
    }

    private void execShell(String command) {
        String returnString = "";
        Process pro = null;
        Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            System.err.println("Create runtime false!");
        }
        BufferedReader input = null;
        PrintWriter output = null;
        try {
            pro = runTime.exec(command);
            input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n";
            }
        } catch (Exception ex) {
            log.error("exec shell command failed : {}", ex.toString());
        } finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (Exception e){
                    log.error(e.toString());
                }
            }
            if (output != null) {
                output.close();
            }
            if (pro != null) {
                pro.destroy();
            }
        }
        log.info("execShell {} ,return : {} ", command, returnString);
    }

    private void checknotpass(String uid) {
        ZeusUser user = userManager.findByUid(uid);
        user.setIsEffective(ZeusUser.UserStatus.CHECK_FAILED.value());
        userManager.update(user);
        ZeusUser newUser = userManager.findByUid(uid);
        List<String> mailUsers = new ArrayList<String>();
        mailUsers.add(ZeusUser.ADMIN.getUid());
        mailUsers.add(newUser.getUid());
        MailAlarm mailAlarm = new MailAlarm();
        List<String> emails = getEmailsByUsers(mailUsers);
        if (emails != null && emails.size() > 0) {
            mailAlarm.sendEmail(
                    "",
                    emails,
                    "Zeus新用户审核被拒绝",
                    "Dear All," + "\r\n	Zeus新用户审核被拒绝，详细信息如下：" + "\r\n		用户类别："
                            + (newUser.getUserType() == 0 ? "组用户" : "个人用户")
                            + "\r\n		用户账号：" + newUser.getUid() + "\r\n		用户姓名："
                            + newUser.getName() + "\r\n		用户邮箱："
                            + newUser.getEmail() + "\r\n	请知晓，谢谢！");
        }
    }

    private List<String> getEmailsByUsers(List<String> users) {
        List<String> emails = new ArrayList<String>();
        try {
            List<ZeusUser> userList = userManager.findListByUid(users);
            if (userList != null && userList.size() > 0) {
                for (ZeusUser user : userList) {
                    String userEmail = user.getEmail();
                    if (userEmail != null && !userEmail.isEmpty()
                            && userEmail.contains("@")) {
                        if (userEmail.contains(";")) {
                            String[] userEmails = userEmail.split(";");
                            for (String ems : userEmails) {
                                if (ems.contains("@")) {
                                    emails.add(ems);
                                }
                            }
                        } else {
                            emails.add(userEmail);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("getEmailsByUsers error", ex);
        }
        return emails;
    }

    private void delete(String uid) {
        ZeusUser user = userManager.findByUid(uid);
        user.setIsEffective(ZeusUser.UserStatus.Cancel.value());
        userManager.update(user);
        ZeusUser newUser = userManager.findByUid(uid);
        List<String> mailUsers = new ArrayList<String>();
        mailUsers.add(ZeusUser.ADMIN.getUid());
        mailUsers.add(newUser.getUid());
        MailAlarm mailAlarm = new MailAlarm();
        List<String> emails = getEmailsByUsers(mailUsers);
        if (emails != null && emails.size() > 0) {
            mailAlarm.sendEmail(
                    "",
                    emails,
                    "Zeus用户被删除",
                    "Dear All," + "\r\n	Zeus用户被删除，详细信息如下：" + "\r\n		用户类别："
                            + (newUser.getUserType() == 0 ? "组用户" : "个人用户")
                            + "\r\n		用户账号：" + newUser.getUid() + "\r\n		用户姓名："
                            + newUser.getName() + "\r\n		用户邮箱："
                            + newUser.getEmail() + "\r\n	请知晓，谢谢！");
        }
    }

    @Override
    public void checkpass(List<String> uids) throws Exception {
        if (hasAdminPermission()) {
            for (String uid : uids) {
                try {
                    checkpass(uid);
                } catch (Exception e) {
                    log.error("error occor in checkpass, uid:" + uid + "", e);
                }
            }
        } else {
            throw new Exception("您无权操作");
        }
    }

    @Override
    public void checknotpass(List<String> uids) throws Exception {
        if (hasAdminPermission()) {
            for (String uid : uids) {
                try {
                    checknotpass(uid);
                } catch (Exception e) {
                    log.error("error occor in checknotpass, uid:" + uid + "", e);
                }
            }

        } else {
            throw new Exception("您无权操作");
        }

    }

    @Override
    public void delete(List<String> uids) throws Exception {

        if (hasAdminPermission()) {
            for (String uid : uids) {
                try {
                    delete(uid);
                } catch (Exception e) {
                    log.error("error occor in delete, uid:" + uid + "", e);
                }
            }
        } else {
            throw new Exception("您无权操作");
        }
    }
}
