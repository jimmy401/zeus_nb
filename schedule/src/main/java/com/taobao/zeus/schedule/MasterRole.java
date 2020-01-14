package com.taobao.zeus.schedule;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.zeus.socket.master.MasterContext;

/**
 * Zeus 调度系统
 * @author zhoufang
 *
 */
public class MasterRole {
	
	private static Logger log = LoggerFactory.getLogger(MasterRole.class);
	private AtomicBoolean running=new AtomicBoolean(false);

	@Autowired
	private MasterContext context;

	public MasterRole(){
	}
	
	public void startup(int port){
		if(!running.compareAndSet(false, true)){
			return;
		}
		log.info("begin to init master");
		context.init(port);
	}
	
	public void shutdown(){
		if(running.compareAndSet(true, false)){
			context.destory();
		}
	}
}
