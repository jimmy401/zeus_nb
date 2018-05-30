package com.taobao.zeus.web.util.DataX.HiveReader;

import java.util.List;

public class Parameter {
    private String path;
    private String defaultFS;
    private List<Column> column;
    private String fileType;
    private String fieldDelimiter;
    private String haveKerberos;
    private String kerberosKeytabFilePath;
    private String kerberosPrincipal;
    public void setPath(String path) {
        this.path = path;
    }
    public String getPath() {
        return path;
    }

    public void setDefaultFS(String defaultFS) {
        this.defaultFS = defaultFS;
    }
    public String getDefaultFS() {
        return defaultFS;
    }

    public void setColumn(List<Column> column) {
        this.column = column;
    }
    public List<Column> getColumn() {
        return column;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public String getFileType() {
        return fileType;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }
    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setHaveKerberos(String haveKerberos) {
        this.haveKerberos = haveKerberos;
    }
    public String getHaveKerberos() {
        return haveKerberos;
    }

    public void setKerberosKeytabFilePath(String kerberosKeytabFilePath) {
        this.kerberosKeytabFilePath = kerberosKeytabFilePath;
    }
    public String getKerberosKeytabFilePath() {
        return kerberosKeytabFilePath;
    }

    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }
    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }
}
