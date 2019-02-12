package com.taobao.zeus.web.util.DataX.TxtFileReader;

import java.util.List;

public class Parameter {
    private List<String> path;
    private List<Column> column;
    private String fieldDelimiter;
    private String compress;
    private String skipHeader;
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
