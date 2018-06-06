package com.taobao.zeus.web.util;

import com.taobao.zeus.dal.logic.impl.MysqlZeusAction;
import com.taobao.zeus.dal.logic.impl.MysqlZeusActionBak;
import com.taobao.zeus.dal.mapper.ZeusActionBakMapper;
import com.taobao.zeus.dal.mapper.ZeusActionMapper;
import com.taobao.zeus.dal.model.ZeusActionBakWithBLOBs;
import com.taobao.zeus.dal.model.ZeusActionWithBLOBs;
import com.taobao.zeus.dal.model.ZeusJobWithBLOBs;
import com.taobao.zeus.dal.tool.PersistenceAndBeanConvertWithAction;
import com.taobao.zeus.model.HostGroupCache;
import com.taobao.zeus.model.JobDescriptor;
import com.taobao.zeus.model.JobStatus;
import com.taobao.zeus.model.JobStatus.Status;
import com.taobao.zeus.mvc.Controller;
import com.taobao.zeus.mvc.Dispatcher;
import com.taobao.zeus.schedule.DistributeLocker;
import com.taobao.zeus.schedule.ZeusSchedule;
import com.taobao.zeus.schedule.mvc.JobController;
import com.taobao.zeus.schedule.mvc.event.Events;
import com.taobao.zeus.schedule.mvc.event.JobMaintenanceEvent;
import com.taobao.zeus.socket.master.JobElement;
import com.taobao.zeus.socket.master.MasterContext;
import com.taobao.zeus.socket.master.MasterWorkerHolder;
import com.taobao.zeus.socket.master.MasterWorkerHolder.HeartBeatInfo;
import com.taobao.zeus.util.Environment;
import com.taobao.zeus.util.Tuple;
import org.jboss.netty.channel.Channel;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Dump调度系统内的Job状态，用来调试排查问题
 *
 * @author zhoufang
 */
@Service
public class ScheduleDump extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(ScheduleDump.class);

    private static final long serialVersionUID = 1L;
    private DistributeLocker locker;
    private MysqlZeusAction mysqlZeusAction;
    private MysqlZeusActionBak mysqlZeusActionBak;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ApplicationContext context = WebApplicationContextUtils
                .getWebApplicationContext(config.getServletContext());
        locker = (DistributeLocker) context.getBean("distributeLocker");
        mysqlZeusAction = (MysqlZeusAction) context.getBean("mysqlZeusAction");
        mysqlZeusActionBak=(MysqlZeusActionBak) context.getBean("mysqlZeusActionBak");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("utf-8");
        try {
            if (locker != null) {
                Field zeusScheduleField = locker.getClass().getDeclaredField(
                        "zeusSchedule");
                zeusScheduleField.setAccessible(true);
                ZeusSchedule zeusSchedule = (ZeusSchedule) zeusScheduleField
                        .get(locker);
                if (zeusSchedule != null) {
                    Field masterContextField = zeusSchedule.getClass()
                            .getDeclaredField("context");
                    masterContextField.setAccessible(true);
                    MasterContext context = (MasterContext) masterContextField
                            .get(zeusSchedule);
                    if (context != null) {
                        String op = req.getParameter("op");
                        if ("workers".equals(op)) {
                            Map<Channel, MasterWorkerHolder> workers = context
                                    .getWorkers();
                            SimpleDateFormat format = new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss");
                            StringBuilder builder = new StringBuilder();
                            builder.append("<table border=\"1\">");
                            for (Channel channel : workers.keySet()) {
                                MasterWorkerHolder holder = workers.get(channel);
                                Set<String> runnings = holder.getRunnings().keySet();
                                Set<String> manualRunnings = holder.getManualRunnings().keySet();
                                Set<String> debugRunnings = holder.getDebugRunnings().keySet();
                                HeartBeatInfo heart = holder.getHeart();
                                builder.append("<tr>");
                                builder.append("<td>");
                                builder.append(channel.getRemoteAddress() + ":");

                                builder.append("<br>" + "quque:" + context.getQueue().size());

                                for (JobElement job : context.getQueue()) {
                                    builder.append(job.getActionId() + ",");
                                }
                                builder.append("<br>" + "manual queue:" + context.getManualQueue().toString());
                                builder.append("<br>" + "debug queue:" + context.getDebugQueue().toString());


                                builder.append("<br>" + "runnings:" + runnings.toString());
                                builder.append("<br>" + "manual runnings:" + manualRunnings.toString());
                                builder.append("<br>" + "debug runnings:" + debugRunnings.toString());
                                builder.append("<br>" + "heart beat: ");
                                if (heart != null) {
                                    Date now = new Date();
                                    if (heart.timestamp == null) {
                                        builder.append("<br>" + "<font color=\"red\">last heartbeat: null</font>");
                                    } else if ((now.getTime() - heart.timestamp.getTime()) > 1000 * 60) {
                                        builder.append("<br>" + "<font color=\"red\">last heartbeat:" + format.format(heart.timestamp) + "</font>");
                                    } else {
                                        builder.append("<br>" + "last heartbeat:" + format.format(heart.timestamp));
                                    }
                                    if (heart.memRate < Environment.getMaxMemRate()) {
                                        builder.append("<br>" + "mem use rate:" + heart.memRate);
                                    } else {
                                        builder.append("<br>" + "<font color=\"red\">mem use rate:" + heart.memRate + "</font>");
                                    }
                                    if (heart.cpuLoadPerCore < Environment.getMaxCpuLoadPerCore()) {
                                        builder.append("<br>" + "cpu load per core:" + heart.cpuLoadPerCore);
                                    } else {
                                        builder.append("<br>" + "<font color=\"red\">cpu load per core:" + heart.cpuLoadPerCore + "</font>");
                                    }
                                    builder.append("<br>" + "runnings:" + heart.runnings.toString());
                                    builder.append("<br>" + "manual runnings:" + heart.manualRunnings.toString());
                                    builder.append("<br>" + "debug runnings:" + heart.debugRunnings.toString());
                                }
                                builder.append("</td>");
                                builder.append("</tr>");
                            }
                            builder.append("<tr>");
                            builder.append("<td>");
                            builder.append("scheduled below Mem use Rate:" + Environment.getMaxMemRate());
                            builder.append("<br>" + "scheduled below Cpu Load Per Core:" + Environment.getMaxCpuLoadPerCore());
                            builder.append("<br>" + "scan rate:" + Environment.getScanRate());
                            builder.append("<br>" + "number of hosts:" + workers.size());
                            builder.append("</td>");
                            builder.append("</tr>");
                            builder.append("</table>");
                            resp.getWriter().println(builder.toString());
                        } else if ("queue".equals(op)) {
                            Queue<JobElement> queue = context.getQueue();
                            Queue<JobElement> exceptionQueue = context.getExceptionQueue();
                            Queue<JobElement> debugQueue = context.getDebugQueue();
                            Queue<JobElement> manualQueue = context.getManualQueue();
                            resp.getWriter().println("<br>" + "schedule jobs in queue:");
                            for (JobElement jobId : queue) {
                                resp.getWriter().print(jobId.getActionId() + "\t");
                            }
                            resp.getWriter().println("<br>" + "exception jobs in queue:");
                            for (JobElement jobId : exceptionQueue) {
                                resp.getWriter().print(jobId.getActionId() + "\t");
                            }
                            resp.getWriter().println("<br>" + "manual jobs in queue:");
                            for (JobElement jobId : manualQueue) {
                                resp.getWriter().print(jobId.getActionId() + "\t");
                            }
                            resp.getWriter().println("<br>" + "debug jobs in queue:");
                            for (JobElement jobId : debugQueue) {
                                resp.getWriter().print(jobId.getActionId() + "\t");
                            }

                        } else if ("jobstatus".equals(op)) {
                            Dispatcher dispatcher = context.getDispatcher();
                            if (dispatcher != null) {
                                for (Controller c : dispatcher.getControllers()) {
                                    resp.getWriter().println(
                                            "<br>" + c.toString());
                                }
                            }
                        }/*
                         * else if ("clearschedule".equals(op)) { Date now = new
						 * Date(); SimpleDateFormat df = new SimpleDateFormat(
						 * "yyyyMMddHHmmss"); String currentDateStr =
						 * df.format(now) + "0000"; Dispatcher dispatcher =
						 * context.getDispatcher(); if (dispatcher != null) {
						 * List<Controller> controllers = dispatcher
						 * .getControllers(); if (controllers != null &&
						 * controllers.size() > 0) { for (Controller c :
						 * controllers) { JobController jobc = (JobController)
						 * c; String jobId = jobc.getActionId(); if
						 * (Long.parseLong(jobId) < Long
						 * .parseLong(currentDateStr)) {
						 * context.getScheduler().deleteAction( jobId, "zeus"); } }
						 * } } resp.getWriter().println("清理完毕！");
						 * 
						 * }
						 */ else if ("action".equals(op)) {
                            Date now = new Date();
                            SimpleDateFormat df2 = new SimpleDateFormat(
                                    "yyyy-MM-dd");
                            SimpleDateFormat df3 = new SimpleDateFormat(
                                    "yyyyMMddHHmmss");
                            String currentDateStr = df3.format(now) + "0000";
                            List<ZeusJobWithBLOBs> jobDetails = context
                                    .getGroupManagerWithJob().getAllJobs();
                            Map<Long, ZeusActionWithBLOBs> actionDetails = new HashMap<Long, ZeusActionWithBLOBs>();
                            context.getMaster().runScheduleJobToAction(
                                    jobDetails, now, df2, actionDetails,
                                    currentDateStr);
                            context.getMaster().runDependencesJobToAction(
                                    jobDetails, actionDetails, currentDateStr,
                                    0);

                            Dispatcher dispatcher = context.getDispatcher();
                            if (dispatcher != null) {
                                // 增加controller，并修改event
                                if (actionDetails != null && actionDetails.size() > 0) {
                                    List<Long> rollBackActionId = new ArrayList<Long>();
                                    for (Long id : actionDetails.keySet()) {
                                        dispatcher
                                                .addController(new JobController(
                                                        context, context
                                                        .getMaster(),
                                                        id.toString()));
                                        if (id > Long.parseLong(currentDateStr)) {
                                            context.getDispatcher()
                                                    .forwardEvent(
                                                            new JobMaintenanceEvent(
                                                                    Events.UpdateJob,
                                                                    id.toString()));
                                        } else if (id < (Long
                                                .parseLong(currentDateStr) - 15000000)) {
                                            int loopCount = 0;
                                            context.getMaster()
                                                    .rollBackLostJob(id,
                                                            actionDetails,
                                                            loopCount,
                                                            rollBackActionId);

                                        }
                                    }

                                    //取当前日期的后一天.
                                    Calendar cal = Calendar.getInstance();
                                    cal.add(Calendar.DAY_OF_MONTH, +1);
                                    SimpleDateFormat dfNextDate = new SimpleDateFormat("yyyyMMdd0000000000");
                                    String nextDateStr = dfNextDate.format(cal.getTime());

                                    // 清理schedule
                                    List<Controller> controllers = dispatcher
                                            .getControllers();
                                    if (controllers != null
                                            && controllers.size() > 0) {
                                        Iterator<Controller> itController = controllers
                                                .iterator();
                                        while (itController.hasNext()) {
                                            JobController jobc = (JobController) itController
                                                    .next();
                                            String jobId = jobc.getActionId();
                                            if (Long.parseLong(jobId) < (Long
                                                    .parseLong(currentDateStr) - 15000000)) {
                                                try {
                                                    context.getScheduler()
                                                            .deleteJob(jobId,
                                                                    "zeus");
                                                } catch (SchedulerException e) {
                                                    e.printStackTrace();
                                                }
                                            } else if (Long.parseLong(jobId) >= Long
                                                    .parseLong(currentDateStr) && Long.parseLong(jobId) < Long.parseLong(nextDateStr)) {
                                                try {
                                                    if (!actionDetails
                                                            .containsKey(Long
                                                                    .valueOf(jobId))) {
                                                        context.getScheduler()
                                                                .deleteJob(jobId,
                                                                        "zeus");
                                                        context.getGroupManagerWithAction()
                                                                .removeAction(
                                                                        Long.valueOf(jobId));
                                                        itController.remove();
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            resp.getWriter().println("Action生成完毕！");
                        } else if ("hostgroup".equals(op)) {
                            Map<String, HostGroupCache> allHostGroupInfomations = context.getHostGroupCache();
                            List<HostGroupCache> infos = new ArrayList<HostGroupCache>();
                            for (HostGroupCache info : allHostGroupInfomations.values()) {
                                infos.add(info);
                            }
                            Collections.sort(infos, new Comparator<HostGroupCache>() {

                                @Override
                                public int compare(HostGroupCache o1, HostGroupCache o2) {
                                    return Integer.parseInt(o1.getId()) - Integer.parseInt(o2.getId());
                                }
                            });
                            StringBuilder builder = new StringBuilder();
                            builder.append("<h3>host组信息：</h3>");
                            builder.append("<table border=\"1\">");
                            builder.append("<tr>");
                            builder.append("<th>组id</th>");
                            builder.append("<th>名称</th>");
                            builder.append("<th>描述</th>");
                            builder.append("<th>host</th>");
                            builder.append("<th>CurrentPosition</th>");
                            builder.append("</tr>");
                            for (HostGroupCache info : infos) {
                                builder.append("<tr>");
                                builder.append("<td>" + info.getId() + "</td>");
                                builder.append("<td>" + info.getName()
                                        + "</td>");
                                builder.append("<td>" + info.getDescription()
                                        + "</td>");
                                builder.append("<td>");
                                for (String hosts : info.getHosts()) {
                                    builder.append(hosts + "<br/>");
                                }
                                builder.append("</td>");
                                builder.append("<td>" + info.getCurrentPositon() + "</td>");
                                builder.append("</tr>");
                            }
                            builder.append("</table>");
                            builder.append("<br><a href='dump.do?op=refreshhostgroup'>刷新</a>");
                            resp.getWriter().println(builder.toString());
                        } else if ("refreshhostgroup".equals(op)) {
                            context.refreshHostGroupCache();
                            resp.sendRedirect("dump.do?op=hostgroup");
                        } else if ("clearactions".equals(op)) {
                            int before7day = -7;
                            int cnt = 0;
                            int sum = 0;
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, before7day);
                            Date date = cal.getTime();
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd000000");
                            String dateStr = df.format(date) + "0000";
                            Dispatcher dispatcher = context.getDispatcher();
                            if (dispatcher != null) {
                                List<Controller> controllers = dispatcher.getControllers();
                                if (controllers != null && controllers.size() > 0) {
                                    resp.getWriter().println("开始清理内存中controller id为" + dateStr + "以前的controller：");
                                    List<JobDescriptor> toBeTransferred = new ArrayList<JobDescriptor>();
                                    Iterator<Controller> iter = controllers.iterator();
                                    while (iter.hasNext()) {
                                        sum++;
                                        JobController jobc = (JobController) iter.next();
                                        String jobId = jobc.getActionId();
                                        if (Long.parseLong(jobId) < Long.parseLong(dateStr)) {
                                            Tuple<JobDescriptor, JobStatus> tuple = context.getGroupManagerWithAction().getActionDescriptor(jobId);
                                            JobStatus status = tuple.getY();
                                            if (!Status.RUNNING.equals(status.getStatus())) {
                                                toBeTransferred.add(tuple.getX());
                                                iter.remove();
                                                //resp.getWriter().println("<br>成功清理了id为" + jobId + "的controller");
                                                cnt++;
                                            }
                                        }
                                    }
                                    resp.getWriter().println("<br>内存中共" + sum + "个controllers，清理了一周前" + cnt + "个controllers");
                                    if (toBeTransferred != null && toBeTransferred.size() > 0) {
                                        int bakCount = 0;
                                        try {
                                            //String sqlBak = "insert into JobPersistenceBackup (id,toJobId) select c.id, c.toJobId from JobPersistence c where c.id<"+dateStr+" and c.status<>'running'";
                                            //bakCount = session.createSQLQuery(sqlBak).executeUpdate();
                                            //String sqlDel = "delete JobPersistence where id<" + dateStr + " and status<>'running'";
                                            //delCount = session.createSQLQuery(sqlDel).executeUpdate();
                                            resp.getWriter().println("<br><br>开始备份action表");
                                            for (JobDescriptor job : toBeTransferred) {
                                                ZeusActionWithBLOBs persist = PersistenceAndBeanConvertWithAction.convert(job);
                                                ZeusActionBakWithBLOBs backup = new ZeusActionBakWithBLOBs(persist);
                                                resp.getWriter().println("<br>备份数据库中id为" + backup.getId() + "的action");
                                                ZeusActionBakWithBLOBs bakItem = mysqlZeusActionBak.selectByPrimaryKey(backup.getId());
                                                Boolean flag = true;
                                                try {
                                                    if (bakItem != null) {
                                                        mysqlZeusActionBak.updateByPrimaryKeySelective(backup);
                                                    } else {
                                                        mysqlZeusActionBak.insertSelective(backup);
                                                    }
                                                } catch (Exception ex) {
                                                    resp.getWriter().println(ex);
                                                    flag=false;
                                                }
                                                if (flag){
                                                    mysqlZeusAction.deleteByPrimaryKey(persist.getId());
                                                    bakCount++;
                                                }
                                            }
                                            resp.getWriter().println("<br>完成数据库备份， 共备份" + bakCount + "条数据");
                                        } catch (RuntimeException e) {
                                            resp.getWriter().println("<br>出现错误");
                                            resp.getWriter().println(e);
                                        }
                                    }

                                }
                            }

                        } else {
                            resp.getWriter().println("<a href='dump.do?op=jobstatus'>查看Job调度状态</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                            resp.getWriter().println("<a href='dump.do?op=workers'>查看master-worker 状态</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                            resp.getWriter().println("<a href='dump.do?op=queue'>等待队列任务</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                            resp.getWriter().println("<a href='dump.do?op=action'>生成Action版本</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                            resp.getWriter().println("<a href='dump.do?op=hostgroup'>查看host分组信息</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                            resp.getWriter().println("<a href='dump.do?op=clearactions'>清理一周前的action</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp.getWriter().close();
        // req.getRequestDispatcher("/login.jsp").forward(req, resp);
        // resp.sendRedirect("/login.jsp");
    }

}
