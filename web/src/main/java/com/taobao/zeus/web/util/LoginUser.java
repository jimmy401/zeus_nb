package com.taobao.zeus.web.util;


import com.taobao.zeus.dal.model.ZeusUser;

public class LoginUser {
	public static ThreadLocal<ZeusUser> user=new ThreadLocal<ZeusUser>();
	
	public static ZeusUser getUser(){
		return user.get();
	}

	public static void setUser(ZeusUser user) {
		LoginUser.user.set(user);
	}
	
}
