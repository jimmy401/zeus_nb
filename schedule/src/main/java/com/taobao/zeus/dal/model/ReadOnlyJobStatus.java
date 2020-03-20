package com.taobao.zeus.dal.model;

import com.taobao.zeus.model.JobStatus;

import java.util.HashMap;
import java.util.Map;

public class ReadOnlyJobStatus extends JobStatus {
    private static final long serialVersionUID = 1L;
    private JobStatus jobStatus;
    public ReadOnlyJobStatus(JobStatus js){
        jobStatus=js;
    }
    @Override
    public String getJobId(){
        return jobStatus.getJobId();
    }
    @Override
    public void setJobId(String jobId){
        throw new UnsupportedOperationException();
    }

    @Override
    public Status getStatus(){
        return jobStatus.getStatus();
    }
    @Override
    public void setStatus(Status status){
        throw new UnsupportedOperationException();
    }
    @Override
    public Map<String, String> getReadyDependency() {
        return new HashMap<String, String>(jobStatus.getReadyDependency());
    }
    @Override
    public void setReadyDependency(Map<String, String> readyDependency){
        throw new UnsupportedOperationException();
    }
    @Override
    public String getHistoryId() {
        return jobStatus.getHistoryId();
    }
    @Override
    public void setHistoryId(String historyId) {
        throw new UnsupportedOperationException();
    }
}
