package com.taobao.zeus.schedule.mvc;

import com.taobao.zeus.mvc.AppEvent;
import com.taobao.zeus.mvc.Controller;
import com.taobao.zeus.mvc.DispatcherListener;
import com.taobao.zeus.mvc.MvcEvent;
import com.taobao.zeus.schedule.mvc.event.Events;
import com.taobao.zeus.schedule.mvc.event.JobMaintenanceEvent;
import com.taobao.zeus.socket.master.Master;
import com.taobao.zeus.socket.master.MasterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
/**
 * 如果是新增操作，这里进行处理，添加controller
 * @author zhoufang
 *
 */
public class AddJobListener extends DispatcherListener{

	private static Logger log=LoggerFactory.getLogger(AddJobListener.class);
	
	private Master master;
	
	private MasterContext context;
	
	public AddJobListener(MasterContext context,Master master){
		this.master=master;
		this.context=context;
	}
	@Override
	public void beforeDispatch(MvcEvent mvce) {
		
		if(mvce.getAppEvent() instanceof JobMaintenanceEvent){
			JobMaintenanceEvent event=(JobMaintenanceEvent)mvce.getAppEvent();
			if (event.getType() != Events.UpdateActions) {
				String jobId=event.getId();
				boolean exist=false;
				for(Controller c:new ArrayList<Controller>(context.getDispatcher().getControllers())){
					if(c instanceof JobController){
						JobController jc=(JobController)c;
						if(jc.getActionId().equals(jobId)){
							exist=true;
							break;
						}
					}
				}
				if(!exist){//新增操作
					JobController controller=new JobController(context,master, jobId);
					context.getDispatcher().addController(controller);
					controller.handleEvent(new AppEvent(Events.Initialize));
					mvce.setCancelled(true);
					log.error("schedule add job with jobId:"+jobId);
				}
			}
		}
	}
}
