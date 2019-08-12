package com.taobao.zeus.web.platform.module;

import java.util.List;

public interface FilterPagingLoadConfig extends PagingLoadConfig {
    List<FilterConfig> getFilters();

    void setFilters(List<FilterConfig> var1);
}
