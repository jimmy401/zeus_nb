package com.taobao.zeus.web.platform.module;

public interface FilterConfig  {
    String getComparison();

    String getField();

    String getType();

    String getValue();

    void setComparison(String var1);

    void setField(String var1);

    void setType(String var1);

    void setValue(String var1);
}
