package com.taobao.zeus.socket.master;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusJobWithBLOBs;
import com.taobao.zeus.schedule.mvc.*;
import com.taobao.zeus.util.*;
import org.jboss.netty.channel.Channel;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.zeus.broadcast.alarm.MailAlarm;
import com.taobao.zeus.broadcast.alarm.SMSAlarm;
import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.model.DebugHistory;
import com.taobao.zeus.model.FileDescriptor;
import com.taobao.zeus.model.HostGroupCache;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobHistory;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.JobStatus.TriggerType;
import com.taobao.zeus.model.Profile;
import com.taobao.zeus.mvc.Controller;
import com.taobao.zeus.mvc.Dispatcher;
import com.taobao.zeus.schedule.mvc.event.DebugFailEvent;
import com.taobao.zeus.schedule.mvc.event.DebugSuccessEvent;
import com.taobao.zeus.schedule.mvc.event.Events;
import com.taobao.zeus.schedule.mvc.event.JobFailedEvent;
import com.taobao.zeus.schedule.mvc.event.JobLostEvent;
import com.taobao.zeus.schedule.mvc.event.JobMaintenanceEvent;
import com.taobao.zeus.schedule.mvc.event.JobSuccessEvent;
import com.taobao.zeus.socket.SocketLog;
import com.taobao.zeus.socket.master.MasterWorkerHolder.HeartBeatInfo;
import com.taobao.zeus.socket.master.reqresp.MasterExecuteJob;
import com.taobao.zeus.socket.protocol.Protocol.ExecuteKind;
import com.taobao.zeus.socket.protocol.Protocol.Response;
import com.taobao.zeus.socket.protocol.Protocol.Status;
import com.taobao.zeus.dal.tool.GroupBean;
import com.taobao.zeus.dal.tool.JobBean;

public class Master {

	private MasterContext context;
	private static Logger log = LoggerFactory.getLogger(Master.class);
	private Map<Long, ZeusActionWithBLOBs> actionDetails;

	public Master(final MasterContext context) {
		this.context = context;
		GroupBean root = context.getGroupManagerWithAction().getGlobeGroupBean();

		if (Environment.isPrePub()) {
			// 如果是预发环境，添加stop listener，阻止自动调度执行
			context.getDispatcher().addDispatcherListener(
					new StopScheduleJobListener());
		}
		log.info("add job listener");
		context.getDispatcher().addDispatcherListener(
				new AddJobListener(context, this));
		log.info("add job fail listener");
		context.getDispatcher().addDispatcherListener(
				new JobFailListener(context));
		log.info("add debug listener");
		context.getDispatcher().addDispatcherListener(
				new DebugListener(context));
		log.info("add job success listener");
		context.getDispatcher().addDispatcherListener(
				new JobSuccessListener(context));
		Map<String, JobBean> allJobBeans = root.getAllSubJobBeans();
		log.info("all jobs count {}",allJobBeans.size());
		for (String id : allJobBeans.keySet()) {
			log.info("add controller {}",id);
			context.getDispatcher().addController(
					new JobController(context, this, id));
		}
		// 初始化
		context.getDispatcher().forwardEvent(Events.Initialize);
		context.setMaster(this);
		//刷新host分组关系列表
		log.info("refresh HostGroup Cache start");
		context.refreshHostGroupCache();
		log.info("refresh HostGroup Cache");

		//监测host资源
		context.getSchedulePool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				//刷新host分组
				context.refreshHostGroupCache();
				log.info("refresh HostGroup Cache");
				
				//开始漏跑检测、清理schedule
				log.info("begin clear schedule.");
				try{
					//取当前日期
					Date now = new Date();
					SimpleDateFormat dfDateTime=new SimpleDateFormat("yyyyMMddHHmmss0000");
					String currentDateStr = dfDateTime.format(now);
					
					//取当前日期的后一天. 
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, +1);  
					SimpleDateFormat dfNextDate=new SimpleDateFormat("yyyyMMdd0000000000");
					String nextDateStr = dfNextDate.format(cal.getTime());
					
					Dispatcher dispatcher=context.getDispatcher();  
					if(dispatcher != null){
						///id  20160309 152300 000 6    yyyyMMddHHmmss 000 6
						Map<Long, ZeusActionWithBLOBs> actionDetailsNew = new HashMap<Long, ZeusActionWithBLOBs>();
						actionDetailsNew = actionDetails;
						if(actionDetailsNew != null && actionDetailsNew.size() > 0){
							//增加controller，并修改event
							List<Long> rollBackActionId = new ArrayList<Long>();
							for (Long id : actionDetailsNew.keySet()) {
								log.info("to roll back with atciontId : "+ id +" than "+ (Long.parseLong(currentDateStr)-15000000));
								if(id < (Long.parseLong(currentDateStr)-15000000)){
									//当前时间15分钟之前JOB的才检测漏跑
									int loopCount = 0;
									log.info("roll back with atciontId : "+ id);
									rollBackLostJob(id, actionDetailsNew, loopCount, rollBackActionId);
								}
							}
							log.info("roll back lost job ok");

							//清理schedule
							List<Controller> controllers = dispatcher.getControllers();
							if(controllers!=null && controllers.size()>0){
								Iterator<Controller> itController = controllers.iterator();
								while(itController.hasNext()){
									JobController jobc = (JobController)itController.next();
									String actionId = jobc.getActionId();
									if(Long.parseLong(actionId) < (Long.parseLong(currentDateStr)-15000000)){
										try {
											log.info("clear scheduler delete actionId :" +actionId );
											context.getScheduler().deleteJob(actionId, "zeus");
										} catch (SchedulerException e) {
											e.printStackTrace();
										}
									}else if(Long.parseLong(actionId) >= Long.parseLong(currentDateStr) && Long.parseLong(actionId) < Long.parseLong(nextDateStr)){
										try {
											if(!actionDetailsNew.containsKey(Long.valueOf(actionId))){
												context.getScheduler().deleteJob(actionId, "zeus");
												context.getGroupManagerWithAction().removeAction(Long.valueOf(actionId));
												itController.remove();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
							log.info("clear job scheduler ok");
						}
					}
				}catch(Exception e){
					log.error("roll back lost job failed or clear job schedule failed !", e);
				}
			}
		}, 1, 1, TimeUnit.HOURS);
		
		//***************2014-09-15 定时扫描JOB表,生成action表********************
		context.getSchedulePool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try{
					Date now = new Date();

					SimpleDateFormat dfHour=new SimpleDateFormat("HH");
					SimpleDateFormat dfMinute=new SimpleDateFormat("mm");
					int execHour = Integer.parseInt(dfHour.format(now));
					int execMinute = Integer.parseInt(dfMinute.format(now));
					
//					if((execHour == 0 && execMinute == 0)
//							|| (execHour == 0 && execMinute == 35)
//							|| (execHour > 7 && execMinute == 20)
//							|| (execHour > 7 && execHour < 22 && execMinute == 50)){
					//分钟
					if(execMinute%10==0)    {
						//取当前时间
						SimpleDateFormat dfDate=new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat dfDateTime=new SimpleDateFormat("yyyyMMddHHmmss");
						String currentDateStr = dfDateTime.format(now)+"0000";
						
						if(execHour == 23){
							//取当前日期的后一天.
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.DAY_OF_MONTH, +1);   
							SimpleDateFormat dfNextDate=new SimpleDateFormat("yyyyMMdd0000000000");
							currentDateStr = dfNextDate.format(cal.getTime());
							now = cal.getTime();
						}
						log.info("start to action, date：" + currentDateStr);
						List<ZeusJobWithBLOBs> jobDetails = context.getGroupManagerWithJob().getAllJobs();
						log.info("all jobs count ：" + jobDetails.size());
						Map<Long, ZeusActionWithBLOBs> actionDetailsNew = new HashMap<Long, ZeusActionWithBLOBs>();
						//首先，生成当天的独立任务action
						log.info("generate schedule jobs to action ");
						runScheduleJobToAction(jobDetails, now, dfDate, actionDetailsNew, currentDateStr);
						//其次，生成依赖任务action
						log.info("generate dependency jobs to action ");
						runDependencesJobToAction(jobDetails, actionDetailsNew, currentDateStr, 0);
						
						if(execHour < 23){
							actionDetails = actionDetailsNew;
						}
						log.info("run job to action ok");
						log.info("job to action count:"+actionDetailsNew.size());
						Dispatcher dispatcher=context.getDispatcher();  
						if(dispatcher != null){
							//增加controller，并修改event
							if (actionDetailsNew.size() > 0) {
								for (Long id : actionDetailsNew.keySet()) {
									dispatcher.addController(
											new JobController(context, context.getMaster(),
													id.toString()));
									if (id > Long.parseLong(currentDateStr)) {
										context.getDispatcher().forwardEvent(
												new JobMaintenanceEvent(Events.UpdateJob,
														id.toString()));
									}
								}
							}
						}
						log.info("add job to scheduler ok");
					}
				}catch(Exception e){
					log.error("job to action failed !", e);
				}
			}
		}, 1, 1, TimeUnit.MINUTES);
				
		// 定时扫描等待队列
		log.info("The scan rate is " + Environment.getScanRate());
		context.getSchedulePool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					scan();
				} catch (Exception e) {
					log.error("get job from queue failed!", e);
				}
			}
		}, 0, Environment.getScanRate(), TimeUnit.MILLISECONDS);
	
		log.info("The scan exception rate is " + Environment.getScanExceptionRate());
		context.getSchedulePool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					scanExceptionQueue();
				} catch (Exception e) {
					log.error("get job from exception queue failed!", e);
				}
			}
		}, 0, Environment.getScanExceptionRate(), TimeUnit.MILLISECONDS);
		
		// 定时扫描worker channel，心跳超过1分钟没有连接就主动断掉
		context.getSchedulePool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Date now = new Date();
				for (MasterWorkerHolder holder : new ArrayList<MasterWorkerHolder>(
						context.getWorkers().values())) {
//					log.info("schedule worker start:"+holder.getDebugRunnings().size());
					try {
						if (holder.getHeart().timestamp == null
								|| (now.getTime() - holder.getHeart().timestamp
										.getTime()) > 1000 * 60) {
							holder.getChannel().close();
						}
					    //log.info("dmp.zeus.worker.tasks.max"+LionUtils.getProperty("dmp.zeus.worker.tasks.max"));
					} catch (Exception e) {
						log.error("holder:"+holder+" is in error",e);
					}
					
//					log.info("schedule worker end:"+holder.getDebugRunnings().size());
					//


				}
			}
		}, 30, 30, TimeUnit.SECONDS);
		
		context.getSchedulePool().scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				try {
					// 检测任务超时
					checkTimeOver();
				} catch (Exception e) {
					log.error("error occurs in checkTimeOver",e);
				}
			}
		}, 0, 3, TimeUnit.SECONDS);
	}

	//重新调度漏跑的JOB
	public void rollBackLostJob(Long id, final Map<Long,ZeusActionWithBLOBs> actionDetails, int loopCount, List<Long> rollBackActionId){
		loopCount ++;
		try {
			ZeusActionWithBLOBs lostJob = actionDetails.get(id);
			if(lostJob != null){
				String jobDependStr = lostJob.getDependencies();
				if (jobDependStr != null && jobDependStr.trim().length() > 0) {
					String[] jobDependencies = jobDependStr.split(",");
					boolean isAllComplete = true;
					if(jobDependencies != null && jobDependencies.length > 0){
						for (String jobDepend : jobDependencies) {
							if(actionDetails.get(Long.parseLong(jobDepend)) != null){
								if (actionDetails.get(Long.parseLong(jobDepend)).getStatus() == null
										|| actionDetails.get(Long.parseLong(jobDepend)).getStatus().equals("wait")) {
									isAllComplete = false;
									// 递归查询
									if (loopCount < 30 && rollBackActionId.contains(Long.parseLong(jobDepend))) {
										rollBackLostJob(Long.parseLong(jobDepend), actionDetails, loopCount, rollBackActionId);
									}
								} else if (actionDetails.get(Long.parseLong(jobDepend)).getStatus().equals("failed")) {
									isAllComplete = false;
								}
							}
						}
					}
					if(isAllComplete){
						if(!rollBackActionId.contains(id)){
							context.getDispatcher().forwardEvent(
									new JobLostEvent(Events.UpdateJob, id.toString()));
							rollBackActionId.add(id);
//							System.out.println("roll back lost jobID :" + id.toString());
//							log.info("roll back lost jobID :" + id.toString());
						}
					}
				} else {
					if(!rollBackActionId.contains(id)){
						context.getDispatcher().forwardEvent(
								new JobLostEvent(Events.UpdateJob, id.toString()));
						rollBackActionId.add(id);
//						System.out.println("roll back lost jobID :" + id.toString());
//						log.info("roll back lost jobID :" + id.toString());
					}
				}
			}
		} catch (Exception e) {
			log.error("roll back lost job failed !", e);
		}
	}
	
	private synchronized MasterWorkerHolder getRunableWorker(String hostGroupId,int type) {
		if (hostGroupId == null) {
			hostGroupId = Environment.getDefaultWorkerGroupId();
		}
		MasterWorkerHolder selectWorker = null;

		if (context.getHostGroupCache()!=null) {
			HostGroupCache hostGroupCache = context.getHostGroupCache().get(hostGroupId);

			if (hostGroupCache != null && hostGroupCache.getHosts()!=null && hostGroupCache.getHosts().size()>0) {
				int size = hostGroupCache.getHosts().size();
				for (int i = 0; i < size && selectWorker == null; i++) {
					String host = hostGroupCache.selectHost();
					log.info("hostGroupCache` host  `````````````````````" + host);

					if (host == null) {
						break;
					}

					log.error("context workers:"+context.getWorkers());

					for (MasterWorkerHolder worker : context.getWorkers().values()) {
						int step=0;
						try {

							if (worker!=null && worker.getHeart()!=null && worker.getHeart().host.equals(host)) {
								step=1;
								HeartBeatInfo heart = worker.getHeart();

								int taskMax=Environment.getZeusWorkerTasksMax();
								int runnings=0;
//
//
								if(type==1) {
									runnings = worker.getRunnings().size();
									QueueInfoLog.info("running:"+worker.getRunnings().toString());
								}
//								else if (type==2) {
//									runnings = worker.getManualRunnings().size();
//									QueueInfoLog.info("manual:" + worker.getManualRunnings().toString());
//								}else if(type==3) {
//									runnings = worker.getDebugRunnings().size();
//									QueueInfoLog.info("running:"+worker.getDebugRunnings().toString());
//								}

								int heartRunnings =  worker.getHeart().runnings.size();

								if (heart != null && heart.memRate != null && heart.cpuLoadPerCore!=null
										&& heart.memRate < Environment.getMaxMemRate()
										&&heart.cpuLoadPerCore < Environment.getMaxCpuLoadPerCore()
										&&runnings<taskMax
										&&heartRunnings<taskMax
										){
									selectWorker = worker;
									step=2;

									QueueInfoLog.info("worker match:"+worker.getRunnings()+",heart:"+heartRunnings);

									break;
								}else {
//									selectWorker = worker;
									ScheduleInfoLog.info(" running task" + worker.getRunnings().size() + " >dmp.zeus.worker.tasks.max "+taskMax);
									break;
								}
							}
							else {
								step=3;
								if(worker == null){
									log.error("worker is null");
								}else if(worker!=null && worker.getHeart()==null && worker.getChannel()!=null){
									log.error("worker " + worker.getChannel().toString()+" heart is null");
								}

								if(worker != null){
									SocketLog.error("  heart:"+worker.getHeart()+" channel:{}"+worker.getChannel());


								}
							}
						} catch (Exception e) {
							log.error("worker failed",e);
						}finally {
							log.error("step---------"+step);
						}
					}
				}
			}
		}
		if (selectWorker != null) {
			log.info("select worker: " + selectWorker.getHeart().host + ", for HostGroupId " + hostGroupId);
		}else {
			log.error("can not find proper workers for hostGroupId:::"+hostGroupId);
		}
		QueueInfoLog.info("select worker"+selectWorker);
		return selectWorker;
	}
	
	
 	//扫描可用的worker，给worker分配JOB任务
	private void scan() {
		if (!context.getQueue().isEmpty()) {
//			final JobElement e = context.getQueue().poll();
			final JobElement e = context.getQueue().peek();
			log.info("priority level :"+e.getPriorityLevel()+"; JobID :"+e.getActionId());
			runScheduleAction(e);
		}

		if (!context.getManualQueue().isEmpty()) {
			final JobElement e = context.getManualQueue().poll();
			log.info("priority level: "+e.getPriorityLevel()+"; JobID:"+e.getActionId());
			MasterWorkerHolder selectWorker = getRunableWorker(e.getHostGroupId(),2);

			if (selectWorker == null) {
				context.getManualQueue().offer(e);
				log.info("HostGroupId : "  + e.getHostGroupId() + ","+e.getActionId() +" is offered back to queue");
			} else {
				runManualJob(selectWorker, e.getActionId());
				log.info("HostGroupId : "  + e.getHostGroupId() + ",schedule selectWorker : " +selectWorker+",host :"+selectWorker.getHeart().host);
			}
		}
		
		if (!context.getDebugQueue().isEmpty()) {
			log.info("debug queue :" +context.getDebugQueue().size() );
			final JobElement e = context.getDebugQueue().poll();
			log.info("priority level:null; JobID:"+e.getActionId());
			MasterWorkerHolder selectWorker = getRunableWorker(e.getHostGroupId(),3);
			if (selectWorker == null) {
				context.getDebugQueue().offer(e);
				log.info("HostGroupId : "  + e.getHostGroupId() + ","+e.getActionId() +" is offered back to queue");
			} else {
				runDebugJob(selectWorker, e.getActionId());
				log.info("HostGroupId : "  + e.getHostGroupId() + ",schedule selectWorker : " +selectWorker+",host :"+selectWorker.getHeart().host);
			}
		}
		
//		 检测任务超时
//		checkTimeOver();
	}

	private void runScheduleAction(final JobElement e) {
		MasterWorkerHolder selectWorker = getRunableWorker(e.getHostGroupId(),1);
		if (selectWorker == null) {
				//context.getExceptionQueue().offer(e);
//				log.info("HostGroupId : "  + e.getHostGroupId() + ","+e.getActionId() +" is offered to exceptionQueue");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} else {
//			final JobElement e = context.getQueue().poll();

			context.getQueue().remove(e);

			runScheduleJob(selectWorker, e.getActionId());
			log.info("HostGroupId : "  + e.getHostGroupId() + ",schedule selectWorker : " +selectWorker+",host :"+selectWorker.getHeart().host);
		}
	}
	
	private void scanExceptionQueue(){
		if(!context.getExceptionQueue().isEmpty()){
			log.info("exception queue :" +context.getExceptionQueue().size());
			final JobElement e = context.getExceptionQueue().poll();
			runScheduleAction(e);
		}
	}

	private void runDebugJob(MasterWorkerHolder selectWorker, final String jobID) {
		final MasterWorkerHolder w = selectWorker;
		//final JobElement debugId = context.getDebugQueue().poll();
		SocketLog.info("master scan and poll debugId=" + jobID
				+ " and run!");

		new Thread() {
			@Override
			public void run() {
				DebugHistory history = context.getDebugHistoryManager()
						.findDebugHistory(jobID);
				history.getLog().appendZeus(
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date()) + " 开始运行");
				context.getDebugHistoryManager().updateDebugHistoryLog(
						jobID, history.getLog().getContent());
				Exception exception = null;
				Response resp = null;
				try {
					Future<Response> f = new MasterExecuteJob().executeJob(
							context, w, ExecuteKind.DebugKind,
							jobID);
					resp = f.get();
				} catch (Exception e) {
					exception = e;
					DebugInfoLog.error(
							String.format("debugId:%s run failed",
									jobID), e);
				}
				boolean success = resp.getStatus() == Status.OK ? true : false;

				if (!success) {
					// 运行失败，更新失败状态，发出失败消息
					if (exception != null) {
						exception = new ZeusException(String.format(
								"fileId:%s run failed ", history.getFileId()),
								exception);
					} else {
						exception = new ZeusException(String.format(
								"fileId:%s run failed ", history.getFileId()));
					}
					DebugInfoLog.info("debugId:" + jobID + " run fail ");
					history = context.getDebugHistoryManager()
							.findDebugHistory(jobID);
					DebugFailEvent jfe = new DebugFailEvent(
							history.getFileId(), history, exception);
					context.getDispatcher().forwardEvent(jfe);
				} else {
					// 运行成功，发出成功消息
					DebugInfoLog.info("debugId:" + jobID + " run success");
					DebugSuccessEvent dse = new DebugSuccessEvent(
							history.getFileId(), history);
					context.getDispatcher().forwardEvent(dse);
				}
			}
		}.start();
	}

	private void runManualJob(MasterWorkerHolder selectWorker,final String jobID) {
		final MasterWorkerHolder w = selectWorker;
		//final JobElement historyId = context.getManualQueue().poll();
		SocketLog.info("master scan and poll historyId=" + jobID
				+ " and run!");
		new Thread() {
			@Override
			public void run() {
				JobHistory history = context.getJobHistoryManager()
						.findJobHistory(jobID);
				history.getLog().appendZeus(
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date()) + " 开始运行");
				context.getJobHistoryManager().updateJobHistoryLog(
						jobID, history.getLog().getContent());
				//更新手动运行状态
				JobStatus jobstatus = context.getGroupManagerWithAction().getActionStatus(history.getActionId());
				jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.RUNNING);
				jobstatus.setHistoryId(jobID);
				context.getGroupManagerWithAction().updateActionStatus(jobstatus);
				
				Exception exception = null;
				Response resp = null;
				try {
					Future<Response> f = new MasterExecuteJob().executeJob(
							context, w, ExecuteKind.ManualKind,
							jobID);
					resp = f.get();
				} catch (Exception e) {
					exception = e;
					ScheduleInfoLog.error("JobId:" + history.getActionId()
							+ " run failed", e);
					jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
					context.getGroupManagerWithAction().updateActionStatus(jobstatus);
				}
				boolean success = resp.getStatus() == Status.OK ? true : false;

				if (!success) {
					// 运行失败，更新失败状态，发出失败消息
					ZeusJobException jobException = null;
					if (exception != null) {
						jobException = new ZeusJobException(history.getActionId(),
								String.format("JobId:%s run failed ",
										history.getActionId()), exception);
					} else {
						jobException = new ZeusJobException(history.getActionId(),
								String.format("JobId:%s run failed ",
										history.getActionId()));
					}
					ScheduleInfoLog.info("jobId:" + history.getActionId()
							+ " run fail ");
					history = context.getJobHistoryManager().findJobHistory(
							jobID);
					
					jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
					JobFailedEvent jfe = new JobFailedEvent(history.getActionId(),
							history.getTriggerType(), history, jobException);
					if (history.getIllustrate() == null
							|| !history.getIllustrate().contains("手动取消该任务")) {
						context.getDispatcher().forwardEvent(jfe);
					}
				} else {
					// 运行成功，发出成功消息
					ScheduleInfoLog.info("manual jobId::" + history.getActionId()
							+ " run success");
					jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.SUCCESS);
					JobSuccessEvent jse = new JobSuccessEvent(
							history.getActionId(), history.getTriggerType(),
							jobID);
					context.getDispatcher().forwardEvent(jse);
				}
				context.getGroupManagerWithAction().updateActionStatus(jobstatus);
			};
		}.start();
	}

	private void runScheduleJob(MasterWorkerHolder selectWorker,final String jobID) {
		final MasterWorkerHolder w = selectWorker;
		//final JobElement jobId = context.getQueue().poll();
		SocketLog.info("master scan and poll jobId=" + jobID + " and run!");

		QueueInfoLog.info("put runnings"+w.getRunnings().size()+"");
		w.getRunnings().put(jobID,false);
		new Thread() {
			@Override
			public void run() {
				int runCount = 0;
				int rollBackTimes = 0;
				int rollBackWaitTime = 1;
				try{				
					JobDescriptor jobDes = context.getGroupManagerWithAction().getActionDescriptor(jobID).getX();
					Map<String,String> properties = jobDes.getProperties();
					if(properties!=null && properties.size()>0){
						rollBackTimes = Integer.parseInt(properties.get("roll.back.times")==null ? "0" : properties.get("roll.back.times"));
						rollBackWaitTime = Integer.parseInt(properties.get("roll.back.wait.time")==null ? "1" : properties.get("roll.back.wait.time"));
					}
				}catch(Exception ex){
					rollBackTimes = 0;
					rollBackWaitTime = 1;
				}
				try{
					runScheduleJobContext(w, jobID, runCount, rollBackTimes, rollBackWaitTime);
				}catch(Exception ex){
					w.getRunnings().remove(jobID);
					log.error("roll back failed job failed !",ex);
				}
			}
		}.start();
	}

	//schedule任务运行，失败后重试
	private void runScheduleJobContext(MasterWorkerHolder w, final String actionId, int runCount, final int rollBackTimes, final int rollBackWaitTime){
		runCount++;
		boolean isCancelJob = false;
		if(runCount > 1){
			try {
				Thread.sleep(rollBackWaitTime*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// 先根据任务ID，查询出任务上次执行的历史记录（jobID->historyid->JobHistory)
		JobHistory his = null;
		TriggerType type = null;
		if(runCount == 1){
			String historyId = context.getGroupManagerWithAction()
					.getActionStatus(actionId).getHistoryId();
			ScheduleInfoLog.info("actionId:" + actionId + " and historyId : "+ historyId);
			his = context.getJobHistoryManager().findJobHistory(historyId);
			type = his.getTriggerType();
			ScheduleInfoLog.info("actionId:" + actionId + " run start");
			his.getLog().appendZeus(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()) + " 开始运行");
		}else{
			JobDescriptor jobDescriptor = context.getGroupManagerWithAction().getActionDescriptor(actionId).getX();
			his = new JobHistory();
			his.setIllustrate("失败任务重试，开始执行");
			his.setTriggerType(TriggerType.SCHEDULE);
			type = his.getTriggerType();
			his.setActionId(jobDescriptor.getId());
			his.setOperator(jobDescriptor.getOwner() == null ? null : jobDescriptor.getOwner());
			his.setJobId(jobDescriptor.getJobId() == null ? null : jobDescriptor.getJobId());
			his.setTimezone(jobDescriptor.getTimezone());
			his.setStatus(com.taobao.zeus.model.JobStatus.Status.RUNNING);
			his.setHostGroupId(jobDescriptor.getHostGroupId());
			context.getJobHistoryManager().addJobHistory(his);
			his.getLog().appendZeus(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()) + " 第" + (runCount-1) + "次重试运行");
		}
		context.getJobHistoryManager().updateJobHistoryLog(his.getId(),
				his.getLog().getContent());
		JobStatus jobstatus = context.getGroupManagerWithAction().getActionStatus(his.getActionId());
		jobstatus.setHistoryId(his.getId());
		jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.RUNNING);
		context.getGroupManagerWithAction().updateActionStatus(jobstatus);
		
		Exception exception = null;
		Response resp = null;
		try {
			Future<Response> f = new MasterExecuteJob().executeJob(
					context, w, ExecuteKind.ScheduleKind, his.getId());
			resp = f.get();
		} catch (Exception e) {
			exception = e;
			ScheduleInfoLog.error(
					String.format("JobId:%s run failed", actionId), e);
			jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
			context.getGroupManagerWithAction().updateActionStatus(jobstatus);
		}
		boolean success = resp.getStatus() == Status.OK ? true : false;
		if (success
				&& (his.getTriggerType() == TriggerType.SCHEDULE || his
						.getTriggerType() == TriggerType.MANUAL_RECOVER)) {
			ScheduleInfoLog.info("actionId:" + actionId
					+ " clear ready dependency");
			jobstatus.setReadyDependency(new HashMap<String, String>());
		}
		if (!success) {
			// 运行失败，更新失败状态，发出失败消息
			ZeusJobException jobException = null;
			if (exception != null) {
				jobException = new ZeusJobException(actionId,
						String.format("actionId:%s run failed ",
								actionId), exception);
			} else {
				jobException = new ZeusJobException(actionId,
						String.format("actionId:%s run failed ",
								actionId));
			}
			ScheduleInfoLog.info("actionId:" + actionId
					+ " run fail and dispatch the fail event");
			jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
			JobHistory jobHistory = context.getJobHistoryManager().findJobHistory(his.getId());
			JobFailedEvent jfe = new JobFailedEvent(actionId, type, jobHistory, jobException);
			jfe.setRollBackTime(rollBackTimes);
			jfe.setRunCount(runCount);
			if (jobHistory != null && jobHistory.getIllustrate() != null
					&& jobHistory.getIllustrate().contains("手动取消该任务")) {
				isCancelJob = true;
			} else {
				context.getDispatcher().forwardEvent(jfe);
			}
		} else {
			// 运行成功，发出成功消息
			ScheduleInfoLog.info("actionId:" + actionId
					+ " run success and dispatch the success event");
			jobstatus.setStatus(com.taobao.zeus.model.JobStatus.Status.SUCCESS);
			JobSuccessEvent jse = new JobSuccessEvent(actionId,
					his.getTriggerType(), his.getId());
			jse.setStatisEndTime(his.getStatisEndTime());
			context.getDispatcher().forwardEvent(jse);
		}
		context.getGroupManagerWithAction().updateActionStatus(jobstatus);
		if(runCount < (rollBackTimes + 1) && !success && !isCancelJob){
			runScheduleJobContext(w, actionId, runCount, rollBackTimes, rollBackWaitTime);
		}
	}
	
	/**
	 * 检查任务超时
	 */
	private void checkTimeOver() {
		for (MasterWorkerHolder w : context.getWorkers().values()) {
			checkScheduleTimeOver(w);
			//TODO 未测试
//			checkManualTimeOver(w);
//			checkDebugTimeOver(w);
		}
	}

	private void checkDebugTimeOver(MasterWorkerHolder w) {
		for (Map.Entry<String, Boolean> entry : w.getDebugRunnings().entrySet()) {
			if (entry.getValue() != null && entry.getValue()) {
				continue;
			}
			String historyId = entry.getKey();
			DebugHistory his = context.getDebugHistoryManager()
					.findDebugHistory(historyId);
			long maxTime;
			FileDescriptor fd;
			try {
				fd = context.getFileManager().getFile(his.getFileId());
				Profile pf = context.getProfileManager().findByUid(
						fd.getOwner());
				String maxTimeString = pf.getHadoopConf().get(
						"zeus.job.maxtime");
				if (maxTimeString == null || maxTimeString.trim().isEmpty()) {
					continue;
				}
				maxTime = Long.parseLong(maxTimeString);

				if (maxTime < 0) {
					continue;
				}
			} catch (Exception e) {
				continue;
			}
			long runTime = (System.currentTimeMillis() - his.getStartTime()
					.getTime()) / 1000 / 60;
			if (runTime > maxTime) {
				if (timeOverAlarm(null, fd, runTime, maxTime, 2, null)) {
					w.getDebugRunnings().replace(historyId, false, true);
				}
			}
		}
	}

	private void checkManualTimeOver(MasterWorkerHolder w) {
		for (Map.Entry<String, Boolean> entry : w.getManualRunnings()
				.entrySet()) {
			if (entry.getValue() != null && entry.getValue()) {
				continue;
			}
			String historyId = entry.getKey();
			JobHistory his = context.getJobHistoryManager().findJobHistory(
					historyId);
			JobDescriptor jd = context.getGroupManagerWithAction()
					.getActionDescriptor(his.getActionId()).getX();
			long maxTime;
			try {
				String maxTimeString = jd.getProperties().get(
						"zeus.job.maxtime");
				if (maxTimeString == null || maxTimeString.trim().isEmpty()) {
					continue;
				}
				maxTime = Long.parseLong(maxTimeString);

				if (maxTime < 0) {
					continue;
				}
			} catch (Exception e) {
				continue;
			}
			long runTime = (System.currentTimeMillis() - his.getStartTime()
					.getTime()) / 1000 / 60;
			if (runTime > maxTime) {
				if (timeOverAlarm(his, null, runTime, maxTime, 1, jd)) {
					w.getManualRunnings().replace(historyId, false, true);
				}
			}
		}
	}

	private void checkScheduleTimeOver(MasterWorkerHolder w) {
		for (Map.Entry<String, Boolean> entry : w.getRunnings().entrySet()) {
			if (entry.getValue() != null && entry.getValue()) {
				continue;
			}
			String jobId = entry.getKey();
			JobDescriptor jd = context.getGroupManagerWithAction()
					.getActionDescriptor(jobId).getX();
			String maxTimeString = jd.getProperties().get("zeus.job.maxtime");
			long maxTime;
			try {
				if (maxTimeString == null || maxTimeString.trim().isEmpty()) {
					continue;
				}
				maxTime = Long.parseLong(maxTimeString);

				if (maxTime < 0) {
					continue;
				}
			} catch (Exception e) {
				continue;
			}

			JobHistory his = context.getJobHistoryManager().findJobHistory(
					context.getGroupManagerWithAction().getActionStatus(jobId)
							.getHistoryId());
			if (his != null && his.getStartTime() != null) {
				long runTime = (System.currentTimeMillis() - his.getStartTime()
						.getTime()) / 1000 / 60;
				if (runTime > maxTime) {
					log.info("send the timeOverAlarm of job: " + jobId);
					if (timeOverAlarm(his, null, runTime, maxTime, 0, jd)) {
						w.getRunnings().replace(jobId, false, true);
					}
				}
			}
		}
	}

	private boolean timeOverAlarm(final JobHistory his, FileDescriptor fd,
			long runTime, long maxTime, int type, JobDescriptor jd) {
		final MailAlarm mailAlarm = (MailAlarm) context.getMailAlarm();
		SMSAlarm smsAlarm = (SMSAlarm) context.getSmsAlarm();

		final StringBuffer title = new StringBuffer("宙斯任务超时[");
		switch (type) {
		case 0:
			title.append("自动调度").append("] jobID=").append(his.getActionId());
			break;
		case 1:
			title.append("手动调度").append("] jobID=").append(his.getActionId());
			break;
		case 2:
			title.append("调试任务").append("] 脚本名称：").append(fd.getName());
		}
		final StringBuffer content = new StringBuffer(title);
		if(jd != null){
			title.append(" (").append(jd.getName()).append(")");
			content.append("\nJOB任务名称：").append(jd.getName());
			Map<String, String> properties=jd.getProperties();
			if(properties != null){
				String plevel=properties.get("run.priority.level");
				if("1".equals(plevel)){
					content.append("\nJob任务优先级: ").append("low");
				}else if("2".equals(plevel)){
					content.append("\nJob任务优先级: ").append("medium");
				}else if("3".equals(plevel)){
					content.append("\nJob任务优先级: ").append("high");
				}
			}
			content.append("\nJOB任务Owner：").append(jd.getOwner());
		}
		content.append("\n已经运行时间：").append(runTime).append("分钟")
				.append("\n设置最大运行时间：").append(maxTime).append("分钟");
		if(his != null){
			content.append("\n运行日志：\n").append(his.getLog().getContent().replaceAll("\\n", "<br/>"));
		}
		try {
			if (type == 2) {
				// 此处可以发送IM消息
			} else {
				// 此处可以发送IM消息
				new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(6000);
							mailAlarm
									.alarm(his.getId(),
											title.toString(),
											content.toString()
													.replace("\n", "<br/>"));
						} catch (Exception e) {
							log.error("send run timeover mail alarm failed", e);
						}
					}
				}.start();
				if (type == 0) {
					String priorityLevel = "3";
					if(jd != null){
						priorityLevel = jd.getProperties().get("run.priority.level");
					}
					if(priorityLevel == null || !priorityLevel.trim().equals("1")){
						Calendar now = Calendar.getInstance();
						int hour = now.get(Calendar.HOUR_OF_DAY);
						int day = now.get(Calendar.DAY_OF_WEEK);
						if (day == Calendar.SATURDAY || day == Calendar.SUNDAY
								|| hour < 9 || hour > 18) {
							smsAlarm.alarm(his.getId(), title.toString(),content.toString(), null);
							//mailAlarm.alarm(his.getId(), title.toString(),content.toString(), null);
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			log.error("send run timeover alarm failed", e);
			return false;
		}
	}

	public void workerDisconnectProcess(Channel channel) {
		MasterWorkerHolder holder = context.getWorkers().get(channel);
		if (holder != null) {
//			SocketLog.info("worker disconnect, ip:" + channel.getRemoteAddress().toString());
			context.getWorkers().remove(channel);
			final List<JobHistory> hiss = new ArrayList<JobHistory>();
			Map<String, Tuple<JobDescriptor, JobStatus>> map = context
					.getGroupManagerWithAction().getActionDescriptor(
							holder.getRunnings().keySet());
			for (String key : map.keySet()) {
				JobStatus js = map.get(key).getY();
				if (js.getHistoryId() != null) {
					JobHistory his = context.getJobHistoryManager().findJobHistory(
							js.getHistoryId());
					if(his != null){
						hiss.add(his);
					}
				}
				/*js.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
				context.getGroupManagerWithAction().updateActionStatus(js);*/
			}
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
					}
					for (JobHistory his : hiss) {
						String jobId = his.getActionId();
						his.setEndTime(new Date());
						his.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
						his.setIllustrate("worker断线，任务失败");
						context.getJobHistoryManager().updateJobHistory(his);
						JobHistory history = new JobHistory();
						history.setActionId(jobId);
						history.setJobId(his.getJobId());
						history.setTriggerType(his.getTriggerType());
						history.setIllustrate("worker断线，重新跑任务");
						history.setOperator(his.getOperator());
						history.setHostGroupId(his.getHostGroupId());
						context.getJobHistoryManager().addJobHistory(history);
						Master.this.run(history);
					}
				};
			}.start();

		}
	}

	public void debug(DebugHistory debug) {
		JobElement element = new JobElement(debug.getId(), debug.gethostGroupId());
		debug.setStatus(com.taobao.zeus.model.JobStatus.Status.RUNNING);
		debug.setStartTime(new Date());
		context.getDebugHistoryManager().updateDebugHistory(debug);
		debug.getLog().appendZeus(
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
						+ " 进入任务队列");
		context.getDebugHistoryManager().updateDebugHistoryLog(debug.getId(),
				debug.getLog().getContent());
		context.getDebugQueue().offer(element);
		System.out.println("offer debug queue :" +context.getDebugQueue().size()+ " element :"+element.getActionId());
	}

	public JobHistory run(JobHistory history) {
		String actionId = history.getActionId();
		log.info("run actionId: " +actionId);
		int priorityLevel = 3;
		try{
			JobDescriptor jd = context.getGroupManagerWithAction().getActionDescriptor(actionId).getX();
			String priorityLevelStr = jd.getProperties().get("run.priority.level");
			if(priorityLevelStr!=null){
				priorityLevel = Integer.parseInt(priorityLevelStr);
			}
		}catch(Exception ex){
			priorityLevel = 3;
		}
		JobElement element = new JobElement(actionId, history.getHostGroupId(), priorityLevel);
		history.setStatus(com.taobao.zeus.model.JobStatus.Status.RUNNING);
		if (history.getTriggerType() == TriggerType.MANUAL_RECOVER) {
			for (JobElement e : new ArrayList<JobElement>(context.getQueue())) {
				if (e.getActionId().equals(actionId)) {
					history.getLog().appendZeus("已经在队列中，无法再次运行");
					history.setStartTime(new Date());
					history.setEndTime(new Date());
					history.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
					break;
				}
			}
			for (Channel key : context.getWorkers().keySet()) {
				MasterWorkerHolder worker = context.getWorkers().get(key);
				if (worker.getRunnings().containsKey(actionId)) {
					history.getLog().appendZeus("已经在运行中，无法再次运行");
					history.setStartTime(new Date());
					history.setEndTime(new Date());
					history.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
					break;
				}
			}
		}

		if (history.getStatus() == com.taobao.zeus.model.JobStatus.Status.RUNNING) {
			history.getLog().appendZeus(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()) + " 进入任务队列");
			context.getJobHistoryManager().updateJobHistoryLog(history.getId(),
					history.getLog().getContent());
			if (history.getTriggerType() == TriggerType.MANUAL) {
				element.setActionId(history.getId());
				context.getManualQueue().offer(element);
			} else {
				JobStatus js = context.getGroupManagerWithAction().getActionStatus(
						history.getActionId());
				js.setStatus(com.taobao.zeus.model.JobStatus.Status.RUNNING);
				js.setHistoryId(history.getId());
				context.getGroupManagerWithAction().updateActionStatus(js);
				context.getQueue().offer(element);
			}
		}
		context.getJobHistoryManager().updateJobHistory(history);
		context.getJobHistoryManager().updateJobHistoryLog(history.getId(),
				history.getLog().getContent());
		return history;
	}
	
	//将当天的且执行时间在当前时间后面的，放入actionDetails； 定时任务生成action、以及没有依赖关系的周期任务生成action
	public void runScheduleJobToAction(List<ZeusJobWithBLOBs> jobDetails, Date now, SimpleDateFormat dfDate, Map<Long, ZeusActionWithBLOBs> actionDetails, String currentDateStr){
		for(ZeusJobWithBLOBs jobDetail : jobDetails){
			//ScheduleType: 0 独立任务; 1依赖任务; 2周期任务
			if(jobDetail.getScheduleType() != null && jobDetail.getScheduleType()==0){
				try{
					String jobCronExpression = jobDetail.getCronExpression();
					String cronDate= dfDate.format(now);
					List<String> lTime = new ArrayList<String>();
					if(jobCronExpression != null && jobCronExpression.trim().length() > 0){
						//定时调度
						boolean isCronExp = false;
						try{
							isCronExp = CronExpParser.Parser(jobCronExpression, cronDate, lTime);
						}catch(Exception ex){
							isCronExp = false;
						}
						if (!isCronExp) {
							log.error("无法生成Cron表达式：日期," + cronDate + ";不符合规则cron表达式：" + jobCronExpression);
						}
						for (int i = 0; i < lTime.size(); i++) {
							String actionDateStr = ZeusDateTool.StringToDateStr(lTime.get(i), "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmm");
							String actionCronExpr = ZeusDateTool.StringToDateStr(lTime.get(i), "yyyy-MM-dd HH:mm:ss", "0 m H d M") + " ?";

							ZeusActionWithBLOBs actionPer = new ZeusActionWithBLOBs();
							actionPer.setId(Long.parseLong(actionDateStr)*1000000+jobDetail.getId());//update action id
							actionPer.setJobId(jobDetail.getId());
							actionPer.setAuto(jobDetail.getAuto());
							actionPer.setConfigs(jobDetail.getConfigs());
							actionPer.setCronExpression(actionCronExpr);//update action cron expression
							actionPer.setCycle(jobDetail.getCycle());
							String jobDependencies = jobDetail.getDependencies();
							actionPer.setDependencies(jobDependencies);
							actionPer.setJobDependencies(jobDependencies);
							actionPer.setDescr(jobDetail.getDescr());
							actionPer.setGmtCreate(jobDetail.getGmtCreate());
							actionPer.setGmtModified(new Date());
							actionPer.setGroupId(jobDetail.getGroupId());
							actionPer.setHistoryId(jobDetail.getHistoryId());
							actionPer.setHost(jobDetail.getHost());
							actionPer.setHostGroupId(jobDetail.getHostGroupId());
							actionPer.setLastEndTime(jobDetail.getLastEndTime());
							actionPer.setLastResult(jobDetail.getLastResult());
							actionPer.setName(jobDetail.getName());
							actionPer.setOffset(jobDetail.getOffset());
							actionPer.setOwner(jobDetail.getOwner());
							actionPer.setPostProcessers(jobDetail.getPostProcessers());
							actionPer.setPreProcessers(jobDetail.getPreProcessers());
							actionPer.setReadyDependency(jobDetail.getReadyDependency());
							actionPer.setResources(jobDetail.getResources());
							actionPer.setRunType(jobDetail.getRunType());
							actionPer.setScheduleType(jobDetail.getScheduleType());
/*							actionPer.setScript(jobDetail.getScript());*/
							actionPer.setStartTime(jobDetail.getStartTime());
							actionPer.setStartTimestamp(jobDetail.getStartTimestamp());
							actionPer.setStatisStartTime(jobDetail.getStatisStartTime());
							actionPer.setStatisEndTime(jobDetail.getStatisEndTime());
							actionPer.setStatus(jobDetail.getStatus());
							actionPer.setTimezone(jobDetail.getTimezone());
							try {
								if(lTime.size()==1||actionPer.getId()>Long.parseLong(currentDateStr)){
									actionDetails.put(actionPer.getId(),actionPer);
								}else
								{
									if(actionPer.getStatus()==null || !actionPer.getStatus().equals(JobStatus.Status.SUCCESS.getId()))
										actionPer.setStatus(JobStatus.Status.FAILED.getId());
								}

								log.info("定时JobId: " + jobDetail.getId() + ";  ActionId: " + actionPer.getId() +
										"; Status:"+actionPer.getStatus()+"; currentDateStr:"+currentDateStr +"lTime:"+i)
								;

								context.getGroupManagerWithAction().saveOrUpdateAction(actionPer);

							} catch (ZeusException e) {
								log.error("定时任务JobId:" + jobDetail.getId() + " 生成Action" +actionPer.getId() + "失败", e);
							}
						}
					}
				}catch(Exception ex){
					log.error("定时任务生成Action失败",ex);
				}
			}

		}
	}
	
	//将依赖任务生成action
	public void runDependencesJobToAction(List<ZeusJobWithBLOBs> jobDetails, Map<Long, ZeusActionWithBLOBs> actionDetails,String currentDateStr, int loopCount){
		int noCompleteCount = 0;
		loopCount ++;
//		System.out.println("loopCount："+loopCount);
		for(ZeusJobWithBLOBs jobDetail : jobDetails){
			//ScheduleType: 0 独立任务; 1依赖任务; 2周期任务
			if((jobDetail.getScheduleType() != null && jobDetail.getScheduleType()==1) 
					|| (jobDetail.getScheduleType() != null && jobDetail.getScheduleType()==2)){
				try{
					String jobDependencies = jobDetail.getDependencies();
					String actionDependencies = "";
					if(jobDependencies != null && jobDependencies.trim().length()>0){
	
						//计算这些依赖任务的版本数
						Map<String,List<ZeusActionWithBLOBs>> dependActionList = new HashMap<String,List<ZeusActionWithBLOBs>>();
						String[] dependStrs = jobDependencies.split(",");
						for(String deps : dependStrs){
							List<ZeusActionWithBLOBs> dependActions = new ArrayList<ZeusActionWithBLOBs>();
							Iterator<ZeusActionWithBLOBs> actionIt = actionDetails.values().iterator();
							while(actionIt.hasNext()){
								ZeusActionWithBLOBs action = actionIt.next();
								if(action.getJobId().toString().equals(deps)){
									dependActions.add(action);
								}
							}
							dependActionList.put(deps, dependActions);
							if(loopCount > 20){
								if(!jobDetail.getConfigs().contains("sameday")){
									if(dependActionList.get(deps).size()==0){
										List<ZeusActionWithBLOBs> lastJobActions = context.getGroupManagerWithAction().getLastJobAction(deps);
										if(lastJobActions != null && lastJobActions.size()>0){
											actionDetails.put(lastJobActions.get(0).getId(),lastJobActions.get(0));
											dependActions.add(lastJobActions.get(0));
											dependActionList.put(deps, dependActions);
										}else{
											break;
										}
									}
								}
							}
						}
						//判断是否有未完成的
						boolean isComplete = true;
						String actionMostDeps = "";
						for(String deps : dependStrs){
							if(dependActionList.get(deps).size()==0){
								isComplete = false;
								noCompleteCount ++;
								break;
							}
							if(actionMostDeps.trim().length()==0){
								actionMostDeps = deps;
							}
							if(dependActionList.get(deps).size()>dependActionList.get(actionMostDeps).size()){
								actionMostDeps = deps;
							}else if(dependActionList.get(deps).size()==dependActionList.get(actionMostDeps).size()){
								if(dependActionList.get(deps).get(0).getId()<dependActionList.get(actionMostDeps).get(0).getId()){
									actionMostDeps = deps;
								}
							}
						}
						if(!isComplete){
							continue;
						}else{
							List<ZeusActionWithBLOBs> actions = dependActionList.get(actionMostDeps);
							if(actions != null && actions.size()>0){
								for(ZeusActionWithBLOBs actionModel : actions){
									actionDependencies = String.valueOf(actionModel.getId());
									for(String deps : dependStrs){
										if(!deps.equals(actionMostDeps)){
											List<ZeusActionWithBLOBs> actionOthers = dependActionList.get(deps);
											Long actionOtherId = actionOthers.get(0).getId();
											for(ZeusActionWithBLOBs actionOtherModel : actionOthers){
												if(Math.abs((actionOtherModel.getId()-actionModel.getId()))<Math.abs((actionOtherId-actionModel.getId()))){
													actionOtherId = actionOtherModel.getId();
												}
											}
											if(actionDependencies.trim().length()>0){
												actionDependencies += ",";
											}
											actionDependencies += String.valueOf((actionOtherId/1000000)*1000000 + Long.parseLong(deps));
										}
									}
									//保存多版本的action
									ZeusActionWithBLOBs actionPer = new ZeusActionWithBLOBs();
									actionPer.setId((actionModel.getId()/1000000)*1000000+jobDetail.getId());//update action id
									actionPer.setJobId(jobDetail.getId());
									actionPer.setAuto(jobDetail.getAuto());
									actionPer.setConfigs(jobDetail.getConfigs());
									actionPer.setCronExpression(jobDetail.getCronExpression());//update action cron expression
									actionPer.setCycle(jobDetail.getCycle());
									actionPer.setDependencies(actionDependencies);
									actionPer.setJobDependencies(jobDependencies);
									actionPer.setDescr(jobDetail.getDescr());
									actionPer.setGmtCreate(jobDetail.getGmtCreate());
									actionPer.setGmtModified(new Date());
									actionPer.setGroupId(jobDetail.getGroupId());
									actionPer.setHistoryId(jobDetail.getHistoryId());
									actionPer.setHost(jobDetail.getHost());
									actionPer.setHostGroupId(jobDetail.getHostGroupId());
									actionPer.setLastEndTime(jobDetail.getLastEndTime());
									actionPer.setLastResult(jobDetail.getLastResult());
									actionPer.setName(jobDetail.getName());
									actionPer.setOffset(jobDetail.getOffset());
									actionPer.setOwner(jobDetail.getOwner());
									actionPer.setPostProcessers(jobDetail.getPostProcessers());
									actionPer.setPreProcessers(jobDetail.getPreProcessers());
									actionPer.setReadyDependency(jobDetail.getReadyDependency());
									actionPer.setResources(jobDetail.getResources());
									actionPer.setRunType(jobDetail.getRunType());
									actionPer.setScheduleType(jobDetail.getScheduleType());
/*									actionPer.setScript(jobDetail.getScript());*/
									actionPer.setStartTime(jobDetail.getStartTime());
									actionPer.setStartTimestamp(jobDetail.getStartTimestamp());
									actionPer.setStatisStartTime(jobDetail.getStatisStartTime());
									actionPer.setStatisEndTime(jobDetail.getStatisEndTime());
									actionPer.setStatus(jobDetail.getStatus());
									actionPer.setTimezone(jobDetail.getTimezone());
									try {
										if(!actionDetails.containsKey(actionPer.getId())){
											//System.out.println("依赖任务JobId: " + jobDetail.getId()+";  ActionId: " +actionPer.getId());
											log.info("依赖任务JobId: " + jobDetail.getId()+";  ActionId: " +actionPer.getId()+"Dependencies:"+jobDetail.getDependencies());
											//if(actionPer.getId()>Long.parseLong(currentDateStr)){
												context.getGroupManagerWithAction().saveOrUpdateAction(actionPer);
												//System.out.println("success");
												//log.info("success");
											//}
											actionDetails.put(actionPer.getId(),actionPer);
										}
									} catch (ZeusException e) {
										log.error("依赖任务JobId:" + jobDetail.getId() + " 生成Action" +actionPer.getId() + "失败", e);
									}
								}
							}
						}
					}
				}catch(Exception ex){
					log.error("依赖任务生成Action失败", ex);
				}
			}
		}

		if(noCompleteCount > 0 && loopCount < 40){
			runDependencesJobToAction(jobDetails, actionDetails, currentDateStr, loopCount);
		}
	}
}
