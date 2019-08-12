package com.taobao.zeus.web.platform.module;

public  interface PagingLoadConfig extends ListLoadConfig {
    void setLimit(int var1);

    void setOffset(int var1);

    int getLimit();

    int getOffset();
}
