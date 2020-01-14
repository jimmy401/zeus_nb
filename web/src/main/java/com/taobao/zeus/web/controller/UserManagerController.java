package com.taobao.zeus.web.controller;

import com.taobao.zeus.broadcast.alarm.MailAlarm;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.web.common.CurrentUser;
import com.taobao.zeus.web.controller.response.CommonResponse;
import com.taobao.zeus.web.controller.response.GridContent;
import com.taobao.zeus.web.controller.response.ReturnCode;
import com.taobao.zeus.web.util.EncryptHelper;
import com.taobao.zeus.web.util.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/")
public class UserManagerController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(UserManagerController.class);

    @Autowired
    UserManager mysqlUserManager;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView loginpage(ModelMap model, HttpServletResponse response) throws Exception {
        return new ModelAndView("login");
    }

    @RequestMapping(value = "/logon", method = RequestMethod.POST)
    public CommonResponse<String> logon(@RequestParam(value = "username", defaultValue = "-1") String username,
                        @RequestParam(value = "password", defaultValue = "-1") String password,
                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        ZeusUser user = mysqlUserManager.findByUidFilter(username);
        if (null == user) {
            return this.buildResponse(ReturnCode.FAILED,"");
        } else {
            String ps = user.getPassword();
            if (null != ps) {
                if (!EncryptHelper.MD5(password).toUpperCase().equals(ps.toUpperCase())) {
                    return this.buildResponse(ReturnCode.FAILED,"");
                }
            }

            //Cookie cookie = new Cookie("LOGIN_USERNAME", user.getUid());
            //cookie.setPath("/");
            //response.addCookie(cookie);

            CurrentUser.setUser(user);
            LoginUser.user.set(user);
            return this.buildResponse(ReturnCode.SUCCESS,"");
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestParam(value = "username", defaultValue = "-1") String uid,
                           @RequestParam(value = "password", defaultValue = "-1") String password,
                           @RequestParam(value = "email", defaultValue = "-1") String email,
                           @RequestParam(value = "phone", defaultValue = "-1") String phone,
                           @RequestParam(value = "userType", defaultValue = "-1") String userType,
                           @RequestParam(value = "description", defaultValue = "-1") String description,
                           HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        ZeusUser u = mysqlUserManager.findByUid(uid);
        if (null != u) {
            return "exist";
        } else {
            try {
                String passwordMD5 = EncryptHelper.MD5(password);
                ZeusUser newUser = new ZeusUser();
                newUser.setUid(uid);
                newUser.setName(uid);
                newUser.setEmail(email);
                newUser.setPhone(phone);
                newUser.setWangwang("");
                newUser.setPassword(passwordMD5);
                newUser.setGmtCreate(new Date());
                newUser.setGmtModified(new Date());
                newUser.setUserType(Integer.parseInt(userType));
                newUser.setDescription(description);
                ZeusUser returnUser = mysqlUserManager.addOrUpdateUser(newUser);
                if (null != returnUser) {
                    List<String> mailUsers = new ArrayList<String>();
                    mailUsers.add(ZeusUser.ADMIN.getUid());
                    mailUsers.add("diadmin");
                    MailAlarm mailAlarm = new MailAlarm();
                    List<String> emails = getEmailsByUsers(mailUsers);
                    if (emails != null && emails.size() > 0) {
                        emails.add(returnUser.getEmail());

                        mailAlarm.sendEmail("", emails, "Zeus新用户注册申请",
                                "Dear All," +
                                        "\r\n	Zeus系统有新用户注册，详细信息如下：" +
                                        "\r\n		用户类别：" + (returnUser.getUserType() == 0 ? "组用户" : "个人用户") +
                                        "\r\n		用户账号：" + returnUser.getUid() +
                                        "\r\n		用户姓名：" + returnUser.getName() +
                                        "\r\n		用户邮箱：" + returnUser.getEmail() +
                                        "\r\n	请确认并审核。\r\n" +
                                        "\r\n	另外，请DI团队开通hive帐号及权限，描述如下：" +
                                        "\r\n		" + returnUser.getDescription() +
                                        "\r\n	\r\n	\r\n谢谢！");
                    }
                    return returnUser.getUid();
                } else {
                    return "error";
                }
            } catch (Exception ex) {
                return "error";
            }
        }
    }

    private List<String> getEmailsByUsers(List<String> users) {
        List<String> emails = new ArrayList<String>();
        try {
            List<ZeusUser> userList = mysqlUserManager.findListByUid(users);
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
            ex.printStackTrace();
        }
        return emails;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public CommonResponse<Void> logout(HttpServletResponse response) throws Exception {
        CurrentUser.destroyUser();
        return this.buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "user_manager/get_all_users_by_page", method = RequestMethod.POST)
    public GridContent getAll(@RequestParam(value = "uid", defaultValue = "") String uid,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "rows", required = false) Integer rows
    ) throws Exception {
        GridContent gridcontent = new GridContent();
        try {
            ZeusUser aUser = CurrentUser.getUser();
            List<ZeusUser> listUsers = new ArrayList<>();
            int count = 0;
            if (aUser.getUserType() == 0) {//组用户的情况下，匹配所有用户
                if (!uid.equals("")) {
                    listUsers.addAll(mysqlUserManager.selectPageByParams(page, rows, uid));
                } else {
                    listUsers.addAll(mysqlUserManager.getPageAllUsers(page, rows));
                }
                count = mysqlUserManager.selectRecordCountByParams(uid);
            } else {
                listUsers.add(CurrentUser.getUser());
                count = 1;
            }

            gridcontent.rows = listUsers;
            gridcontent.total = count;
        } catch (Exception e) {
            logger.error("user_manager/get_all fail", e);
        }
        return gridcontent;
    }

    @RequestMapping(value = "user_manager/get_all_users", method = RequestMethod.POST)
    public List<ZeusUser> getEffectiveUsers() {
        List<ZeusUser> listUsers = new ArrayList<>();
        try {
            listUsers = mysqlUserManager.getAllUsers();
        } catch (Exception e) {
            logger.error("user_manager/get_all_users fail", e);
        }
        return listUsers;
    }

    @RequestMapping(value = "user_manager/current_user", method = RequestMethod.GET)
    public CommonResponse<ZeusUser> getCurrentUser() {
        ZeusUser currentUser = null;
        try {
            currentUser = CurrentUser.getUser();
        } catch (Exception e) {
            logger.error("user_manager/current_user fail", e);
        }
        return buildResponse(ReturnCode.SUCCESS,currentUser);
    }

    @RequestMapping(value = "/user_manager/edit_user", method = RequestMethod.POST)
    public CommonResponse<Void> editUser(@RequestParam(value = "uid", defaultValue = "-1") String uid,
                                         @RequestParam(value = "passwd", defaultValue = "-1") String passwd,
                                         @RequestParam(value = "email", defaultValue = "-1") String email,
                                         @RequestParam(value = "phone", defaultValue = "-1") String phone) throws
            Exception {
        try {
            ZeusUser curUser = CurrentUser.getUser();
            if (curUser.getUserType() == 0 || curUser.getUid().equals(uid)) {//组用户,或者是本人的情况下
                ZeusUser aUser = mysqlUserManager.findByUid(uid);
                aUser.setPassword(EncryptHelper.MD5(passwd));
                aUser.setEmail(email);
                aUser.setPhone(phone);
                mysqlUserManager.update(aUser);
            }
        } catch (Exception e) {
            logger.error("user_manager/edit_user fail", e);
        }

        return buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/user_manager/ok", method = RequestMethod.POST)
    public CommonResponse<Void> ok(@RequestParam(value = "uid", defaultValue = "-1") String uid) throws Exception {
        try {

            ZeusUser curUser = CurrentUser.getUser();
            if (curUser.getUserType() == 0) {//组用户的情况下，匹配所有用户
                ZeusUser aUser = mysqlUserManager.findByUid(uid);
                aUser.setIsEffective(1);
                mysqlUserManager.update(aUser);
            }

        } catch (Exception e) {
            logger.error("user_manager/ok fail", e);
        }

        return buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/user_manager/no", method = RequestMethod.POST)
    public CommonResponse<Void> no(@RequestParam(value = "uid", defaultValue = "-1") String uid) throws Exception {
        try {

            ZeusUser curUser = CurrentUser.getUser();
            if (curUser.getUserType() == 0) {//组用户的情况下，匹配所有用户
                ZeusUser aUser = mysqlUserManager.findByUid(uid);
                aUser.setIsEffective(0);
                mysqlUserManager.update(aUser);
            }

        } catch (Exception e) {
            logger.error("user_manager/no fail", e);
        }

        return buildResponse(ReturnCode.SUCCESS);
    }

    @RequestMapping(value = "/user_manager/delete", method = RequestMethod.POST)
    public CommonResponse<Void> delete(@RequestParam(value = "uid", defaultValue = "-1") String uid) throws Exception {
        try {

            ZeusUser curUser = CurrentUser.getUser();
            if (curUser.getUserType() == 0) {//组用户的情况下
                ZeusUser aUser = mysqlUserManager.findByUid(uid);
                aUser.setIsEffective(ZeusUser.UserStatus.Cancel.value());
                mysqlUserManager.update(aUser);

                MailAlarm mailAlarm = new MailAlarm();
                List<String> emails = new ArrayList<String>();
                emails.add(curUser.getEmail());
                emails.add(aUser.getEmail());
                if (emails != null && emails.size() > 0) {
                    mailAlarm.sendEmail(
                            "",
                            emails,
                            "Zeus用户被删除",
                            "Dear All," + "\r\n	Zeus用户被删除，详细信息如下：" + "\r\n		用户类别："
                                    + (aUser.getUserType() == 0 ? "组用户" : "个人用户")
                                    + "\r\n		用户账号：" + aUser.getUid() + "\r\n		用户姓名："
                                    + aUser.getName() + "\r\n		用户邮箱："
                                    + aUser.getEmail() + "\r\n	请知晓，谢谢！");
                }
            }

        } catch (Exception e) {
            logger.error("user_manager/delete fail", e);
        }

        return buildResponse(ReturnCode.SUCCESS);
    }
}
