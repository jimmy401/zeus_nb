package com.taobao.zeus.web.platform.module;

public interface PagingLoadResult<Data> extends ListLoadResult<Data> {
    int getOffset();

    int getTotalLength();

    void setOffset(int var1);

    void setTotalLength(int var1);
}
