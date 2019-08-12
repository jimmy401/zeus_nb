package com.taobao.zeus.web.platform.shared.rpc;


import com.taobao.zeus.web.platform.module.FileModel;

import java.util.List;

public interface FileManagerService{
	FileModel addFile(String parentId, String name, boolean folder);

	public void deleteFile(String fileId) ;

	void updateFileContent(String fileId, String content);

	void updateFileName(String fileId, String name);

	public FileModel getFile(String id);

	FileClientBean getUserFiles();
	
	void moveFile(String sourceId,String targetId);
	
	List<FileModel> getCommonFiles(FileModel fm);
	
	public FileModel getHomeFile(String id);

	void updateHostGroupId(String fileId, String hostGroupId);
}
