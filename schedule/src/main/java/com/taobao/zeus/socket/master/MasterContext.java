package com.taobao.zeus.socket.master;

import com.taobao.zeus.broadcast.alarm.MailAlarm;
import com.taobao.zeus.broadcast.alarm.SMSAlarm;
import com.taobao.zeus.dal.logic.*;
import com.taobao.zeus.dal.logic.impl.*;
import com.taobao.zeus.model.HostGroupCache;
import com.taobao.zeus.mvc.Dispatcher;
import com.taobao.zeus.schedule.mvc.ScheduleInfoLog;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Component
public class MasterContext {

	private static Logger log = LoggerFactory.getLogger(MasterContext.class);

	@Autowired
	public MysqlHostGroupManager mysqlHostGroupManager;
	@Autowired
	public MysqlJobHistoryManager mysqlJobHistoryManager;
	@Autowired
	public MysqlDebugHistoryManager mysqlDebugHistoryManager;
	@Autowired
	public MysqlFileManager mysqlFileManager;
	@Autowired
	public MysqlProfileManager mysqlProfileManager;
	@Autowired
	public MysqlGroupManagerWithJob mysqlGroupManagerWithJob;
	@Autowired
	public MysqlGroupManagerWithAction mysqlGroupManagerWithAction;
	@Autowired
	public ReadOnlyGroupManagerWithAction readOnlyGroupManagerWithAction;
	@Autowired
	public MysqlUserManager mysqlUserManager;
	@Autowired
	public MailAlarm mailAlarm;
	@Autowired
	public SMSAlarm smsAlarm;

	private Map<Channel, MasterWorkerHolder> workers=new ConcurrentHashMap<Channel, MasterWorkerHolder>();
	private Master master;
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private Map<String,HostGroupCache> hostGroupCache;
	//调度任务 jobId
//	private Queue<JobElement> queue=new ArrayBlockingQueue<JobElement>(10000);
	private Queue<JobElement> queue=new PriorityBlockingQueue<JobElement>(10000, new Comparator<JobElement>() {
					public int compare(JobElement je1, JobElement je2) {
						int numbera = je1.getPriorityLevel();
						int numberb = je2.getPriorityLevel();
						if (numberb > numbera) {
							return 1;
						} else if (numberb < numbera) {
							return -1;
						} else {
							return 0;
						}
					}
				});

	private Queue<JobElement> exceptionQueue = new LinkedBlockingQueue<JobElement>();

	//调试任务  debugId
	private Queue<JobElement> debugQueue=new ArrayBlockingQueue<JobElement>(1000);
	//手动任务  historyId
	private Queue<JobElement> manualQueue=new ArrayBlockingQueue<JobElement>(1000);

	private MasterHandler handler;
	private MasterServer server;
	private ExecutorService threadPool=Executors.newCachedThreadPool();
	private ScheduledExecutorService schedulePool=Executors.newScheduledThreadPool(12);

	public void init(int port){
		log.info("init begin");
		try {
			StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
			stdSchedulerFactory.initialize("zeusQuartz.properties");
			scheduler = stdSchedulerFactory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			ScheduleInfoLog.error("schedule start fail", e);
		}
		dispatcher=new Dispatcher();
		log.info("init dispatcher");

		handler=new MasterHandler(this);
		log.info("init handler");

		server=new MasterServer(handler);
		log.info("init server");

		server.start(port);
		log.info(" server start");

		master=new Master(this);
		log.info("init finish");
	}
	public void destory(){
		threadPool.shutdown();
		schedulePool.shutdown();
		if(server!=null){
			server.shutdown();
		}
		if(scheduler!=null){
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
		ScheduleInfoLog.info("destory finish");
	}
	
	public Map<Channel, MasterWorkerHolder> getWorkers() {
		return workers;
	}
	public void setWorkers(Map<Channel, MasterWorkerHolder> workers) {
		this.workers = workers;
	}
	public Scheduler getScheduler() {
		return scheduler;
	}
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	public Dispatcher getDispatcher() {
		return dispatcher;
	}
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	public JobHistoryManager getJobHistoryManager() {
		return this.mysqlJobHistoryManager;
	}
	public DebugHistoryManager getDebugHistoryManager(){
		return this.mysqlDebugHistoryManager;
	}
	public FileManager getFileManager(){
		return this.mysqlFileManager;
	}
	public ProfileManager getProfileManager(){
		return this.mysqlProfileManager;
	}
	public Queue<JobElement> getQueue() {
		return queue;
	}
	public void setQueue(Queue<JobElement> queue) {
		this.queue = queue;
	}
	public GroupManagerWithJob getGroupManagerWithJob() {
		return this.mysqlGroupManagerWithJob;
	}
	public GroupManagerWithAction getGroupManagerWithAction() {
		return this.mysqlGroupManagerWithAction;
	}
	public ReadOnlyGroupManagerWithAction getReadOnlyGroupManagerWithAction() {
		return this.readOnlyGroupManagerWithAction;
	}
	public UserManager getMysqlUserManager() {
		return this.mysqlUserManager;
	}
	public MailAlarm getMailAlarm() {
		return this.mailAlarm;
	}
	public SMSAlarm getSmsAlarm() {
		return this.smsAlarm;
	}

	public MasterHandler getHandler() {
		return handler;
	}
	public void setHandler(MasterHandler handler) {
		this.handler = handler;
	}
	public MasterServer getServer() {
		return server;
	}
	public void setServer(MasterServer server) {
		this.server = server;
	}
	public ExecutorService getThreadPool() {
		return threadPool;
	}
	public Master getMaster() {
		return master;
	}
	public void setMaster(Master master) {
		this.master = master;
	}
	public ScheduledExecutorService getSchedulePool() {
		return schedulePool;
	}
	public Queue<JobElement> getDebugQueue() {
		return debugQueue;
	}
	public void setDebugQueue(Queue<JobElement> debugQueue) {
		this.debugQueue = debugQueue;
	}
	public Queue<JobElement> getManualQueue() {
		return manualQueue;
	}


	public synchronized void refreshHostGroupCache(){
		try {
			hostGroupCache = mysqlHostGroupManager.getAllHostGroupInfomations();
		} catch (Exception e) {
			ScheduleInfoLog.error("refresh hostgroupcache error", e);
		}
		
	}
	public synchronized Map<String,HostGroupCache> getHostGroupCache() {
		return hostGroupCache;
	}
	public Queue<JobElement> getExceptionQueue() {
		return exceptionQueue;
	}
}
