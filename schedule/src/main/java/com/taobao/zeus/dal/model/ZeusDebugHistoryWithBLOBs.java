package com.taobao.zeus.dal.model;

public class ZeusDebugHistoryWithBLOBs extends ZeusDebugHistory {
    private String log;

    private String script;

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log == null ? null : log.trim();
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script == null ? null : script.trim();
    }
}