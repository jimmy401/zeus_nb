package com.taobao.zeus.web.util;


import com.taobao.zeus.dal.model.ZeusUser;

public class LoginUser {
	public static ThreadLocal<ZeusUser> user=new ThreadLocal<ZeusUser>();
	
	public static ZeusUser getUser(){
	
//		System.out.println(Thread. currentThread ().getName() + ":" + user.get().getUid());
		return user.get();
	}

	public static void setUser(ZeusUser user) {
//		System.out.println(user.toString());
		LoginUser.user.set(user);
	}
	
}
