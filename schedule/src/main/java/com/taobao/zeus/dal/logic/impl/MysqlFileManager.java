package com.taobao.zeus.dal.logic.impl;

import com.taobao.zeus.dal.mapper.ZeusFileMapper;
import com.taobao.zeus.dal.model.ZeusFile;
import com.taobao.zeus.dal.tool.PersistenceAndBeanConvertWithAction;
import com.taobao.zeus.model.FileDescriptor;
import com.taobao.zeus.dal.logic.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository("mysqlFileManager")
public class MysqlFileManager implements
		FileManager {

	private static Logger log = LoggerFactory.getLogger(MysqlFileManager.class);

	@Autowired
	ZeusFileMapper zeusFileMapper;

	@Override
	public FileDescriptor addFile(String uid, String parentId, String name,boolean folder) {
		ZeusFile fp = new ZeusFile();
		fp.setName(name);
		fp.setOwner(uid);
		fp.setParent(Long.valueOf(parentId));
		fp.setType(folder ? ZeusFile.FOLDER : ZeusFile.FILE);
		zeusFileMapper.insertSelective(fp);
		return PersistenceAndBeanConvertWithAction.convert(fp);
	}

	@Override
	public void deleteFile(String fileId) {
		zeusFileMapper.deleteByPrimaryKey(Long.valueOf(fileId));
	}

	@Override
	public FileDescriptor getFile(String id) {
		ZeusFile fp = zeusFileMapper.selectByPrimaryKey( Long.valueOf(id));
		if (fp != null) {
			return PersistenceAndBeanConvertWithAction.convert(fp);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FileDescriptor> getSubFiles(final String id) {

		Map<String,Object> params = new HashMap<String,Object>();
		params.put("parent", Long.valueOf(id));

		List<ZeusFile> fps= zeusFileMapper.findByParent(params);

		List<FileDescriptor> list = new ArrayList<FileDescriptor>();
		for (ZeusFile fp : fps) {
			list.add(PersistenceAndBeanConvertWithAction.convert(fp));
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FileDescriptor> getUserFiles(final String uid) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("owner", uid);
		List<ZeusFile> list = zeusFileMapper.findByOwner(params);
		if (list == null || list.isEmpty()) {
			if (list == null) {
				list = new ArrayList<ZeusFile>();
			}
			ZeusFile personal = new ZeusFile();
			personal.setName(PERSONAL);
			personal.setOwner(uid);
			personal.setType(ZeusFile.FOLDER);
			ZeusFile common = new ZeusFile();
			common.setName(SHARE);
			common.setOwner(uid);
			common.setType(ZeusFile.FOLDER);

			zeusFileMapper.insertSelective(personal);
			zeusFileMapper.insertSelective(common);

			Map<String,Object> p_params = new HashMap<String,Object>();
			p_params.put("name", PERSONAL);
			p_params.put("owner", uid);
			p_params.put("type", ZeusFile.FOLDER);

			Map<String,Object> s_params = new HashMap<String,Object>();
			s_params.put("name", SHARE);
			s_params.put("owner", uid);
			s_params.put("type", ZeusFile.FOLDER);
			personal= zeusFileMapper.selectByParams(p_params);
			common= zeusFileMapper.selectByParams(s_params);

			list.add(personal);
			list.add(common);
		}
		List<FileDescriptor> result = new ArrayList<FileDescriptor>();
		if (list != null) {
			for (ZeusFile fp : list) {
				result.add(PersistenceAndBeanConvertWithAction.convert(fp));
			}
		}
		return result;
	}

	@Override
	public void update(FileDescriptor fd) {
		fd.setGmtModified(new Date());
		zeusFileMapper.updateByPrimaryKeySelective(PersistenceAndBeanConvertWithAction.convert(fd));
	}

}
