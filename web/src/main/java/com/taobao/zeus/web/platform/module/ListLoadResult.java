package com.taobao.zeus.web.platform.module;

import java.io.Serializable;
import java.util.List;

public interface ListLoadResult<D> extends Serializable {
    List<D> getData();
}
