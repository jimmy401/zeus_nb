package com.taobao.zeus.dal.model;

public class ZeusWorker {
    private String host;

    private Float rate;

    private Long timestamp;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}