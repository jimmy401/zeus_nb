package com.taobao.zeus.dal.model;

import java.io.Serializable;
import java.util.Date;

public class ZeusLog  implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String logtype;

    private Date createtime=new Date();

    private String username;

    private String ip;

    private String url;

    private String rpc;

    private String delegate;

    private String method;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogtype() {
        return logtype;
    }

    public void setLogtype(String logtype) {
        this.logtype = logtype == null ? null : logtype.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc == null ? null : rpc.trim();
    }

    public String getDelegate() {
        return delegate;
    }

    public void setDelegate(String delegate) {
        this.delegate = delegate == null ? null : delegate.trim();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method == null ? null : method.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}