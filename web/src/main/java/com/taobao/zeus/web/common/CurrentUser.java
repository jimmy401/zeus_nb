package com.taobao.zeus.web.common;

import com.taobao.zeus.dal.model.ZeusUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class CurrentUser {

    public static ZeusUser getUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (request.getSession().getAttribute("user") == null) {
            return null;
        }
        return (ZeusUser) request.getSession().getAttribute("user");
    }

    public static void setUser(ZeusUser user) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().setAttribute("user", user);
    }

    public static void destroyUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().invalidate();
    }
}
