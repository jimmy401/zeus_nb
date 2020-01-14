package com.taobao.zeus.schedule;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.taobao.zeus.dal.logic.HostGroupManager;
import com.taobao.zeus.dal.logic.ZeusLockManager;
import com.taobao.zeus.dal.model.ZeusLock;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.zeus.schedule.mvc.ScheduleInfoLog;
import com.taobao.zeus.socket.worker.ClientWorker;
import com.taobao.zeus.util.Environment;

/**
 * 分布式服务器的检测器
 * 每隔一分钟查询一次数据库的zeus_lock表
 * @author zhoufang
 *
 */
public class DistributeLocker{

	private static Logger log=LogManager.getLogger(DistributeLocker.class);
	
	public static String host=UUID.randomUUID().toString();
	@Autowired
	private HostGroupManager hostGroupManager;
	@Autowired
	private ClientWorker worker;
	@Autowired
	private ZeusLockManager zeusLockManager;
	
	private MasterRole masterRole;
	
	private int port=9887;
	
	static{
		try {
			host=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			//ignore
		}
	}
	
	public DistributeLocker(String port){
		try {
			this.port=Integer.valueOf(port);
//			System.out.println("DistributeLocker port:"+port);
		} catch (NumberFormatException e) {
			log.error("port must be a number", e);
		}
	}

	/**
	 * 定时扫描
	 * 判断ScheduleServer是否正常运行
	 * @author zhoufang
	 *
	 */
	public void init() throws Exception{
		masterRole =new MasterRole();
		ScheduledExecutorService service=Executors.newScheduledThreadPool(3);
		service.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				try {
					update();
				} catch (Exception e) {
					log.error(e);
				}
			}
		}, 20, 60, TimeUnit.SECONDS);
	}
	/**
	 * 定时扫描任务
	 * 每隔一分钟扫描一次zeus_lock表
	 * 判断ScheduleServer是否正常运行
	 * @author zhoufang
	 *
	 */
	private void update(){
		ZeusLock lock = zeusLockManager.selectBySubGroup(Environment.getScheduleGroup());
		if(lock==null){
			lock=new ZeusLock();
			lock.setHost(host);
			lock.setServerUpdate(new Date());
			lock.setSubgroup(Environment.getScheduleGroup());
			zeusLockManager.insertSelective(lock);
			//取出新记录，主要是拿时间
		}
		
		if(host.equals(lock.getHost())){
			lock.setServerUpdate(new Date());
			zeusLockManager.updateByPrimaryKeySelective(lock);
			log.info("hold the locker and update time");
			masterRole.startup(port);
		}else{//其他服务器抢占了锁
			log.info("not my locker");
			//如果最近更新时间在5分钟以上，则认为抢占的Master服务器已经失去连接，属于抢占组的服务器主动进行抢占
			if(System.currentTimeMillis()-lock.getServerUpdate().getTime()>1000*60*5L && isPreemptionHost()){
				lock.setHost(host);
				lock.setServerUpdate(new Date());
				lock.setSubgroup(Environment.getScheduleGroup());
				zeusLockManager.updateByPrimaryKeySelective(lock);
				log.error("rob the locker and update");
				masterRole.startup(port);
			}else{//如果Master服务器没有问题，本服务器停止server角色
				masterRole.shutdown();
			}
		}

		try {
			worker.connect(lock.getHost(),port);
		} catch (Exception e) {
			ScheduleInfoLog.error("start up worker fail", e);
		}
	}
	//判断该host是否属于抢占组
	public boolean isPreemptionHost(){
		List<String> preemptionhosts = hostGroupManager.getPreemptionHost();
		if (preemptionhosts.contains(host)) {
			return true;
		}else {
			ScheduleInfoLog.info(host + " is not in master gourp: " + preemptionhosts.toString());
			return false;
		}
	}
	
}
