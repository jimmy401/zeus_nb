package com.taobao.zeus.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.zeus.dal.logic.UserManager;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.web.common.CurrentUser;
import com.taobao.zeus.web.util.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 登陆信息设置
 * @author zhoufang
 *
 */
public class LoginFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

	private UserManager userManager;

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
		httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
		httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		httpResponse.setHeader("Access-Control-Max-Age", "3600");
		httpResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");

//		HttpServletRequest httpRequest=(HttpServletRequest) request;
//		HttpServletResponse httpResponse=(HttpServletResponse)response;
//		httpResponse.setCharacterEncoding("utf-8");
//		String uri=httpRequest.getRequestURI();
//        log.info("request url : " + uri);
//		//线上服务器检测
//
//		if(uri.equals("/zeus.check")){
//			response.getWriter().write("success");
//			return;
//		}
//
//		ZeusUser zeusUser=null;
//
//		if(uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".gif") ||
//				uri.endsWith(".jpg") || uri.endsWith(".png") || uri.endsWith(".jsp")||
//				uri.endsWith(".ico")||uri.endsWith("logon")||uri.endsWith("login")){
//			chain.doFilter(request, response);
//			return;
//		}
//
//		if(null!=CurrentUser.getUser()){//如果存在session
//			Boolean check = false;
//			Cookie[] cookies = httpRequest.getCookies();
//			String uid= CurrentUser.getUser().getUid();
//            for(Cookie c :cookies ){
//            	if(c.getName().equals("LOGIN_USERNAME")){
//            		if(c.getValue().equals(uid)){
//            			zeusUser=new ZeusUser();
//            			zeusUser.setUid(uid);
//            			check = true;
//            			//userManager.addOrUpdateUser(zeusUser);
//            			LoginUser.user.set(zeusUser);
//            		}
//            	}
//
//            }
//           if(!check){
//			   httpResponse.sendRedirect("/zeus-web/login");
//        	   return;
//           }
//
//		}else{//不存在user
//			 httpResponse.sendRedirect("/zeus-web/login");
//			 return;
//		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		ApplicationContext applicationContext=WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		userManager=(UserManager) applicationContext.getBean("mysqlUserManager");
	}

}
