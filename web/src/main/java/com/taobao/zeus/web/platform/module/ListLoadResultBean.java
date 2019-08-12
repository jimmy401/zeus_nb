package com.taobao.zeus.web.platform.module;

import java.util.List;

public class ListLoadResultBean<Data> implements ListLoadResult<Data> {
    protected List<Data> list;

    public ListLoadResultBean() {
    }

    public ListLoadResultBean(List<Data> list) {
        this.list = list;
    }

    public List<Data> getData() {
        return this.list;
    }

    public void setData(List<Data> list) {
        this.list = list;
    }
}
