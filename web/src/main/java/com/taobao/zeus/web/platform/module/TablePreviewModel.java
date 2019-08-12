package com.taobao.zeus.web.platform.module;

import com.taobao.zeus.util.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TablePreviewModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> headers = new ArrayList<String>();

    private List<Tuple<Integer, List<String>>> data = new ArrayList<Tuple<Integer, List<String>>>();

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Tuple<Integer, List<String>>> getData() {
        return data;
    }

    public void setData(List<Tuple<Integer, List<String>>> data) {
        this.data = data;
    }
}