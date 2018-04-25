package com.taobao.zeus.web.controller.response;

import java.util.ArrayList;
import java.util.List;

public class GridContent {

	public Object rows;
	public int total;

	public Object getRows() {
		return rows;
	}

	public void setRows(Object rows) {
		this.rows = rows;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public <T> void prepareData(int page, int rows, List<T> materialList) {
		List<T> retList = new ArrayList<T>();
		int totalcount = materialList.size();
		Integer count = 0;
		if (materialList.size() < page * rows) {
			count = rows - (page * rows - materialList.size());
		} else {
			count = rows;
		}
		for (int i = 0; i < count; i++) {
			retList.add(materialList.get((page - 1) * rows + i));
		}

		this.rows = retList;
		this.total = totalcount;
	}

}
