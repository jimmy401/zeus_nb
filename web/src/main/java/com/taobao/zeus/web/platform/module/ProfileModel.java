package com.taobao.zeus.web.platform.module;

import java.io.Serializable;
import java.util.Map;

public class ProfileModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String uid;
    private Map<String, String> hadoopConf;
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public Map<String, String> getHadoopConf() {
        return hadoopConf;
    }
    public void setHadoopConf(Map<String, String> hadoopConf) {
        this.hadoopConf = hadoopConf;
    }
}
