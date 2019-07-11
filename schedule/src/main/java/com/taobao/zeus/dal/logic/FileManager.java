package com.taobao.zeus.dal.logic;

import com.taobao.zeus.model.FileDescriptor;

import java.util.List;

public interface FileManager {
	String PERSONAL="个人文档";
	String SHARE="共享文档";

	Integer PERSON = 1;
	Integer PUBLIC = 2;
	/**
	 * 添加文件/文件夹
	 * @param file
	 * @return id
	 */
	FileDescriptor addFile(String uid, String parentId, String name, boolean folder);
		
	/**
	 * 删除文件/文件夹
	 * @param file
	 */
	public void deleteFile(String fileId);
	/**
	 * 后台查询File最新内容
	 * @param id
	 * @param callback
	 */
	public FileDescriptor getFile(String id);	
	
	public void update(FileDescriptor fd);
	
	public List<FileDescriptor> getSubFiles(String id);
	
	public List<FileDescriptor> getUserFiles(String uid);
}
