package com.taobao.zeus.web.platform.shared.rpc;

import com.taobao.zeus.web.platform.module.*;

import java.util.List;

/**
 * hive表管理服务
 * 
 * @author gufei.wzy 2012-9-17
 */
public interface TableManagerService {
	
	

	/**
	 * 获取一张hive表的model
	 * 
	 * @param tableName
	 * @return
	 */
	TableModel getTableModel(String dataBaseName, String tableName);

	PagingLoadResult<TableModel> getPagingTables(
			FilterPagingLoadConfig loadConfigString, String uid, String dbName) throws Exception;

	/**
	 * 获取预览数据
	 * 
	 * @param tableName
	 * @param part
	 * @throws Exception
	 */
	TablePreviewModel getPreviewData(PartitionModel model) throws Exception;

	public List<PartitionModel> getPartitions(TableModel t) throws Exception;

	public PartitionModel fillPartitionSize(PartitionModel p);


}
