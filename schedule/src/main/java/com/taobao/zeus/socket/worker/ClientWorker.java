package com.taobao.zeus.socket.worker;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.taobao.zeus.model.ZeusActionHistory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.jobs.Job;
import com.taobao.zeus.model.DebugHistory;
import com.taobao.zeus.schedule.mvc.ScheduleInfoLog;
import com.taobao.zeus.socket.SocketLog;
import com.taobao.zeus.socket.protocol.Protocol;
import com.taobao.zeus.socket.protocol.Protocol.ExecuteKind;
import com.taobao.zeus.socket.protocol.Protocol.Status;
import com.taobao.zeus.socket.protocol.Protocol.WebResponse;
import com.taobao.zeus.socket.worker.reqresp.WorkerHeartBeat;
import com.taobao.zeus.socket.worker.reqresp.WorkerWebCancel;
import com.taobao.zeus.socket.worker.reqresp.WorkerWebExecute;
import com.taobao.zeus.socket.worker.reqresp.WorkerWebUpdate;
import org.springframework.stereotype.Service;

@Service
public class ClientWorker {
	// Client服务启动器
	private ClientBootstrap bootstrap;
	private WorkerContext context = new WorkerContext();
	private static Logger log = LoggerFactory.getLogger(ClientWorker.class);

	@Autowired
	public ClientWorker(ApplicationContext applicationContext) {
		ClientBootstrap bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		// 每建立一个connection,也就是channel,就会调用pipelinFacty().getPipeline()方法一次.
		// 设置一个处理服务端消息和各种消息事件的类(WorkHandler)  
		// Pipeline：管道，传输途径。也就是说，在这里他是控制ChannelEvent事件分发和传递的
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new ProtobufVarint32FrameDecoder(),
						new ProtobufDecoder(Protocol.SocketMessage
								.getDefaultInstance()),
						new ProtobufVarint32LengthFieldPrepender(),
						new ProtobufEncoder(), new WorkerHandler(context));
			}
		});
		this.bootstrap = bootstrap;
		//将clientworker对象注入到上下文对象中
		context.setClientWorker(this);
		//将spring容器注入到上下文对象中
		context.setApplicationContext(applicationContext);
		// 定时发送心跳
		ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

		service.scheduleAtFixedRate(new Runnable() {
			private WorkerHeartBeat heartbeat = new WorkerHeartBeat();
			private int failCount = 0;

			@Override
			public void run() {
				try {
					if (context.getServerChannel() == null) {
						return;
					}
					ChannelFuture cf = heartbeat.execute(context);
					SocketLog.info("heart beat send .........."+context.getServerHost());

					cf.addListener(new ChannelFutureListener() {
						public void operationComplete(ChannelFuture future)
								throws Exception {
							if (!future.isSuccess()) {
								failCount++;
								SocketLog.error("heart beat send fail"+context.getServerHost()+" ,failCount="+ failCount

								+"Cause:" + future.getCause()
								);
							} else {
								failCount = 0;
								SocketLog.info("heart beat send success!~~"+context.getServerHost());

							}
							if (failCount > 3) {
								future.getChannel().close();
							}
						}
					});
				} catch (Exception e) {
					log.error("heart beat error:", e);
				}
			}
		}, 0, 5, TimeUnit.SECONDS);



		service.scheduleAtFixedRate(new Runnable() {

			private void exLog(Job job,Exception e){
				try {
					ZeusActionHistory his = job.getJobContext().getZeusActionHistory();
					String jlog = his.getLog().getContent();
					if (jlog == null) {
						jlog = "";
					}
					log.error(new StringBuilder("log output error!\n")
									.append("[jobId:").append(his.getActionId())
									.append(", hisId:").append(his.getId())
									.append(", logLength:")
									.append(jlog.length()).append("]")
									.toString(), e);
				} catch (Exception ex) {
					log.error("log exception error!");
				}
			}

			private void exDebugLog(Job job, Exception e) {
				try {
					DebugHistory his = job.getJobContext().getDebugHistory();
					String jlog = his.getLog().getContent();
					if (jlog == null) {
						jlog = "";
					}
					log.error(new StringBuilder("log output error!\n")
									.append("[fileId:").append(his.getFileId())
									.append(", hisId:").append(his.getId())
									.append(", logLength:")
									.append(jlog.length()).append("]")
									.toString(), e);
				} catch (Exception ex) {
					log.error("log exception error!");
				}
			}
			
			public void run() {

				for (Job job : new HashSet<Job>(context.getRunnings().values())) {
					try {
						ZeusActionHistory his = job.getJobContext().getZeusActionHistory();
						context.getJobHistoryManager().updateJobHistoryLog(his.getId(), his.getLog().getContent());
					} catch (Exception e) {
						exLog(job, e);
					}
				}
				for (Job job : new HashSet<Job>(context.getManualRunnings()
						.values())) {
					try {
						ZeusActionHistory his = job.getJobContext().getZeusActionHistory();
						context.getJobHistoryManager().updateJobHistoryLog(his.getId(), his.getLog().getContent());
					} catch (Exception e) {
						exLog(job, e);
					}
				}
				for (Job job : new HashSet<Job>(context.getDebugRunnings()
						.values())) {
					try {
						DebugHistory his = job.getJobContext().getDebugHistory();
						context.getDebugHistoryManager().updateDebugHistoryLog(his.getId(), his.getLog().getContent());
					} catch (Exception e) {
						exDebugLog(job, e);
					}
				}

			}
		}, 0, 3, TimeUnit.SECONDS);

	}

	//client 与 server 建立连接
	//DistributeLocker类中worker.connect(lock.getHost(),port) 
	public synchronized void connect(String host,int port) throws Exception {
		if (context.getServerChannel() != null) {
			if (host.equals(context.getServerHost())) {
				return;
			} else {
				context.getServerChannel().close();
				context.setServerChannel(null);
			}
		}
		context.setServerHost(host);
		final CountDownLatch latch = new CountDownLatch(1);
		// 等2秒
		final ChannelFutureListener listener = new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				if (future.isSuccess()) {
					context.setServerChannel(future.getChannel());
				}
				latch.countDown();
			}
		};

		final ChannelFuture connectFuture = bootstrap
				.connect(new InetSocketAddress(host, port));

		connectFuture.addListener(listener);
		if (!latch.await(2, TimeUnit.SECONDS)) {
			connectFuture.removeListener(listener);
			connectFuture.cancel();
			throw new ExecutionException(new TimeoutException("创建链接2秒超时"));
		}
		if (!connectFuture.isSuccess()) {
			throw new RuntimeException("connect server fail " + host,
					connectFuture.getCause());
		}
		ScheduleInfoLog.info("worker connect server success");
	}

	public void cancelDebugJob(String debugId) {
		Job job = context.getDebugRunnings().get(debugId);
		if (job == null) {
			throw new RuntimeException("任务已经不存在");
		}
		job.cancel();
		context.getDebugRunnings().remove(debugId);

		DebugHistory his = job.getJobContext().getDebugHistory();
		his.setEndTime(new Date());
		his.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
		context.getDebugHistoryManager().updateDebugHistory(his);
		his.getLog().appendZeus("任务被取消");
		context.getDebugHistoryManager().updateDebugHistoryLog(his.getId(),
				his.getLog().getContent());
	}

	public void cancelManualJob(String historyId) {
		Job job = context.getManualRunnings().get(historyId);
		context.getManualRunnings().remove(historyId);
		job.cancel();

		ZeusActionHistory his = job.getJobContext().getZeusActionHistory();
		his.setEndTime(new Date());
		String illustrate = his.getIllustrate();
		if(illustrate!=null && illustrate.trim().length()>0){
			his.setIllustrate(illustrate+"；手动取消该任务");
		}else{
			his.setIllustrate("手动取消该任务");
		}
		his.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
		context.getJobHistoryManager().updateJobHistory(his);
		his.getLog().appendZeus("任务被取消");
		context.getJobHistoryManager().updateJobHistoryLog(his.getId(),
				his.getLog().getContent());
	}

	public void cancelScheduleJob(String jobId) {
		Job job = context.getRunnings().get(jobId);
		context.getRunnings().remove(jobId);
		job.cancel();

		ZeusActionHistory his = job.getJobContext().getZeusActionHistory();
		his.setEndTime(new Date());
		String illustrate = his.getIllustrate();
		if(illustrate!=null && illustrate.trim().length()>0){
			his.setIllustrate(illustrate+"；手动取消该任务");
		}else{
			his.setIllustrate("手动取消该任务");
		}
		his.setStatus(com.taobao.zeus.model.JobStatus.Status.FAILED);
		context.getJobHistoryManager().updateJobHistory(his);
		his.getLog().appendZeus("任务被取消");
		context.getJobHistoryManager().updateJobHistoryLog(his.getId(),his.getLog().getContent());
	}

	/**
	 * 以下是一些来自web请求的处理方法
	 * 
	 * worker->master 执行任务 包含手动恢复，手动执行，调试执行
	 * 
	 * @param kind
	 * @param id
	 */
	public void executeJobFromWeb(ExecuteKind kind, String id) throws Exception {
		WebResponse resp = new WorkerWebExecute().send(context, kind, id).get();
		if (resp.getStatus() == Status.ERROR) {
			throw new Exception(resp.getErrorText());
		}
	}

	public void cancelJobFromWeb(ExecuteKind kind, String id, String operator)
			throws Exception {
		WebResponse resp = new WorkerWebCancel().cancel(context, kind, id,operator).get();
		if (resp.getStatus() == Status.ERROR) {
			throw new ZeusException(resp.getErrorText());
		}
	}

	public void updateJobFromWeb(String jobId) throws Exception {
		WebResponse resp = new WorkerWebUpdate().execute(context, jobId).get();
		if (resp.getStatus() == Status.ERROR) {
			throw new Exception(resp.getErrorText());
		}
	}

}
