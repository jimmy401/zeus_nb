package com.taobao.zeus.web.platform.module;

import java.io.Serializable;
import java.util.List;

public interface ListLoadConfig extends Serializable {
    List<? extends SortInfo> getSortInfo();

    void setSortInfo(List<? extends SortInfo> var1);
}