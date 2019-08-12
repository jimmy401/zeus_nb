package com.taobao.zeus.dal.logic;

import com.taobao.zeus.dal.model.ZeusFile;

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
	void addFile(String uid, Long parentId, String name, boolean folder);
		
	/**
	 * 删除文件/文件夹
	 * @param file
	 */
	public void deleteFile(Long fileId);
	/**
	 * 后台查询File最新内容
	 * @param id
	 * @param callback
	 */
	public ZeusFile getFile(Long id);
	
	public void update(ZeusFile fd);
	
	public List<ZeusFile> getSubFiles(Long id);
	
	public List<ZeusFile> getUserFiles(String uid);
}
