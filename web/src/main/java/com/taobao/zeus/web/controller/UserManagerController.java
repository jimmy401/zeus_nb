package com.taobao.zeus.web.controller;

import com.taobao.zeus.broadcast.alarm.MailAlarm;
import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.web.common.CurrentUser;
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

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView loginpage(ModelMap model, HttpServletResponse response) throws Exception {
        return new ModelAndView("login");
    }

    @RequestMapping(value = "/logon", method = RequestMethod.POST)
    public String logon(@RequestParam(value = "username", defaultValue = "-1") String username,
                        @RequestParam(value = "password", defaultValue = "-1") String password,
                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        ZeusUser u = mysqlUserManager.findByUidFilter(username);
        if (null == u) {
            return "null";
        } else {
            String ps = u.getPassword();
            if (null != ps) {
                if (!EncryptHelper.MD5(password).toUpperCase().equals(ps.toUpperCase())) {
                    return "error";
                }
            }
            String uid = u.getUid();

            ZeusUser user= new ZeusUser();
            user.setUid(uid);
            user.setEmail(u.getEmail());
            user.setName(u.getName());
            user.setPhone(u.getPhone());

            Cookie cookie = new Cookie("LOGIN_USERNAME", uid);
            String host = request.getServerName();
            cookie.setPath("/");
            //cookie.setDomain(host);
            response.addCookie(cookie);

            CurrentUser.setUser(user);

            LoginUser.user.set(user);
            return uid;
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestParam(value = "user", defaultValue = "-1") String uid,
                        @RequestParam(value = "password", defaultValue = "-1") String password,
                           @RequestParam(value = "email", defaultValue = "-1") String email,
                           @RequestParam(value = "phone", defaultValue = "-1") String phone,
                           @RequestParam(value = "userType", defaultValue = "-1") String userType,
                           @RequestParam(value = "description", defaultValue = "-1") String description,
                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        ZeusUser u = mysqlUserManager.findByUid(uid);
        if(null != u){
            return "exist";
        }else{
            try{
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
                if(null != returnUser){
                    List<String> mailUsers = new ArrayList<String>();
                    mailUsers.add(ZeusUser.ADMIN.getUid());
                    mailUsers.add("diadmin");
                    MailAlarm mailAlarm = new MailAlarm();
                    List<String> emails = getEmailsByUsers(mailUsers);
                    if(emails != null && emails.size()>0){
                        emails.add(returnUser.getEmail());

                        mailAlarm.sendEmail("", emails, "Zeus新用户注册申请",
                                "Dear All,"+
                                        "\r\n	Zeus系统有新用户注册，详细信息如下："+
                                        "\r\n		用户类别："+(returnUser.getUserType()==0 ? "组用户" : "个人用户")+
                                        "\r\n		用户账号："+returnUser.getUid()+
                                        "\r\n		用户姓名："+returnUser.getName()+
                                        "\r\n		用户邮箱："+returnUser.getEmail()+
                                        "\r\n	请确认并审核。\r\n"+
                                        "\r\n	另外，请DI团队开通hive帐号及权限，描述如下："+
                                        "\r\n		" + returnUser.getDescription()+
                                        "\r\n	\r\n	\r\n谢谢！");
                    }
                    return returnUser.getUid();
                }else{
                    return "error";
                }
            }catch(Exception ex){
                return "error";
            }
        }
    }

    private List<String> getEmailsByUsers(List<String> users){
        List<String> emails = new ArrayList<String>();
        try{
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
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return emails;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletResponse response) throws Exception {
        return new ModelAndView("login");
    }

    @RequestMapping(value = "/user_manager", method = RequestMethod.GET)
    public ModelAndView homePage(HttpServletResponse response) throws Exception {
        return new ModelAndView("usermanager");
    }
}
