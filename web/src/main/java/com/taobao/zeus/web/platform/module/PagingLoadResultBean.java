package com.taobao.zeus.web.platform.module;

import java.util.List;

public class PagingLoadResultBean<Data> extends ListLoadResultBean<Data> implements PagingLoadResult<Data> {
    private int totalLength;
    private int offset;

    public PagingLoadResultBean() {
    }

    public PagingLoadResultBean(List<Data> list, int totalLength, int offset) {
        super(list);
        this.totalLength = totalLength;
        this.offset = offset;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getTotalLength() {
        return this.totalLength;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }
}
