package com.taobao.zeus.dal.model;

import com.taobao.zeus.model.ActionDescriptor;
import com.taobao.zeus.model.FileResource;
import com.taobao.zeus.model.processer.Processer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadOnlyActionDescriptor extends ActionDescriptor {

    private static final long serialVersionUID = 1L;
    private ActionDescriptor jd;
    public ReadOnlyActionDescriptor(ActionDescriptor jd){
        this.jd=jd;
    }

    @Override
    public String getCronExpression() {
        return jd.getCronExpression();
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>(jd.getDependencies());
    }

    @Override
    public String getDesc() {
        return jd.getDesc();
    }

    @Override
    public String getGroupId() {
        return jd.getGroupId();
    }

    @Override
    public String getId() {
        return jd.getId();
    }

    @Override
    public JobRunType getJobType() {
        return jd.getJobType();
    }

    @Override
    public String getName() {
        return jd.getName();
    }

    @Override
    public String getOwner() {
        return jd.getOwner();
    }


    @Override
    public JobScheduleType getScheduleType() {
        return jd.getScheduleType();
    }

    @Override
    public boolean hasDependencies() {
        return !jd.getDependencies().isEmpty();
    }

    @Override
    public void setCronExpression(String cronExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDependencies(List<String> depends) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDesc(String desc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJobType(JobRunType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwner(String owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScheduleType(JobScheduleType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGroupId(String groupId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResources(List<FileResource> resources) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<String, String>(jd.getProperties());
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean getAuto() {
        return jd.getAuto();
    }

    @Override
    public void setAuto(Boolean auto) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScript() {
        return jd.getScript();
    }

    @Override
    public void setScript(String script) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<Processer> getPreProcessers() {
        return new ArrayList<Processer>(jd.getPreProcessers());
    }

    @Override
    public void setPreProcessers(List<Processer> preProcessers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Processer> getPostProcessers() {
        return new ArrayList<Processer>(jd.getPostProcessers());
    }

    @Override
    public void setPostProcessers(List<Processer> postProcessers) {
        throw new UnsupportedOperationException();
    }
}
