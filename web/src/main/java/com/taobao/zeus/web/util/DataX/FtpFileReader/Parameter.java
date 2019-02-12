package com.taobao.zeus.web.util.DataX.FtpFileReader;

import java.util.List;

public class Parameter {
    private String protocol;
    private String host;
    private int port;
    private String username;
    private String password;
    private List<String> path;
    private List<Column> column;
    private String fieldDelimiter;
    private String compress;
    private String skipHeader;

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public String getProtocol() {
        return protocol;
    }

    public void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }
    public List<String> getPath() {
        return path;
    }

    public void setColumn(List<Column> column) {
        this.column = column;
    }
    public List<Column> getColumn() {
        return column;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }
    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setCompress(String compress) {
        this.compress = compress;
    }
    public String getCompress() {
        return compress;
    }

    public void setSkipHeader(String skipHeader) {
        this.skipHeader = skipHeader;
    }
    public String getSkipHeader() {
        return skipHeader;
    }
}
