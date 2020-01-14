package com.taobao.zeus.jobs;


import com.taobao.zeus.dal.tool.HierarchyProperties;

public abstract class AbstractJob implements Job {

	protected JobContext jobContext;

	protected boolean canceled=false;

	public AbstractJob(JobContext jobContext){
		this.jobContext=jobContext;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	protected String getProperty(String key,String defaultValue){
		String value=jobContext.getProperties().getProperty(key);
		if(value==null){
			value=defaultValue;
		}
		return value;
	}

	public HierarchyProperties getProperties(){
		return jobContext.getProperties();
	}
	public JobContext getJobContext() {
		return jobContext;
	}

	protected void logConsole(String log){
		if(jobContext.getZeusActionHistory()!=null){
			jobContext.getZeusActionHistory().getLog().appendConsole(log);
		}
		if(jobContext.getDebugHistory()!=null){
			jobContext.getDebugHistory().getLog().appendConsole(log);
		}
	}

	protected void log(String log){
		if(jobContext.getZeusActionHistory()!=null){
			jobContext.getZeusActionHistory().getLog().appendZeus(log);
		}
		if(jobContext.getDebugHistory()!=null){
			jobContext.getDebugHistory().getLog().appendZeus(log);
		}
	}
	protected void log(Exception e){
		if(jobContext.getZeusActionHistory()!=null){
			jobContext.getZeusActionHistory().getLog().appendZeusException(e);
		}
		if(jobContext.getDebugHistory()!=null){
			jobContext.getDebugHistory().getLog().appendZeusException(e);
		}
	}

}