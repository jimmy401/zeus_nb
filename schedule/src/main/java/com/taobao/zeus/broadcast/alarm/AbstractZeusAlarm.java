package com.taobao.zeus.broadcast.alarm;

import com.taobao.zeus.dal.logic.FollowManagerWithJob;
import com.taobao.zeus.dal.logic.GroupManagerWithJob;
import com.taobao.zeus.dal.logic.JobHistoryManager;
import com.taobao.zeus.dal.logic.impl.MysqlUserManager;
import com.taobao.zeus.dal.model.ZeusUser;
import com.taobao.zeus.model.ZeusActionHistory;
import com.taobao.zeus.model.JobStatus.TriggerType;
import com.taobao.zeus.model.ZeusFollow;
import com.taobao.zeus.schedule.mvc.JobFailListener.ChainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractZeusAlarm implements ZeusAlarm{
	protected static Logger log=LoggerFactory.getLogger(AbstractZeusAlarm.class);
	@Autowired
	protected JobHistoryManager jobHistoryManager;
	@Autowired
	@Qualifier("mysqlFollowManagerWithJob")
	protected FollowManagerWithJob followManagerWithJob;
	@Autowired
	@Qualifier("mysqlGroupManagerWithJob")
	protected GroupManagerWithJob groupManagerWithJob;
	@Autowired
	@Qualifier("mysqlUserManager")
	MysqlUserManager mysqlUserManager;
/*	
	@Override
	public void alarm(String historyId, String title, String content,ChainException chain)
			throws Exception {
		ZeusActionHistory history=jobHistoryManager.findJobHistory(historyId);
		TriggerType type=history.getTriggerType();
		//获得action_id
		String jobId=history.getActionId();
		//获得job_id
		String tojobId=history.getActionId();
		List<String> users=new ArrayList<String>();
		if(type==TriggerType.SCHEDULE){
			users=followManagerOld.findActualJobFollowers(tojobId);
		}else{
			users.add(groupManagerOld.getActionDescriptor(tojobId).getX().getOwner());
			if(history.getOperator()!=null){
				if(!users.contains(history.getOperator())){
					users.add(history.getOperator());
				}
			}
		}
		List<String> result=new ArrayList<String>();
		if(chain==null){
			result=users;
		}else{
			for(String uid:users){
				Integer count=chain.getUserCountMap().get(uid);
				if(count==null){
					count=1;
					chain.getUserCountMap().put(uid, count);
				}
				if(count<20){//一个job失败，最多发给同一个人20个报警
					chain.getUserCountMap().put(uid, ++count);
					result.add(uid);
				}
			}
		}
		alarm(jobId, result, title, content);
	}
*/
	@Override
	public void alarm(String historyId, String title, String content,ChainException chain)
			throws Exception {
		ZeusActionHistory history=jobHistoryManager.findJobHistory(historyId);
		TriggerType type=history.getTriggerType();
		//获得action_id
		String actionId=history.getActionId();
		//获得job_id
		String jobId=history.getJobId();
		List<String> users=new ArrayList<String>();
		if(type==TriggerType.SCHEDULE){
			List<ZeusFollow> zeusFollowers = followManagerWithJob.findAllFollowers(jobId);
			List<ZeusFollow> importantContacts = new ArrayList<ZeusFollow>();
			List<ZeusFollow> otherFollowers = new ArrayList<ZeusFollow>();
			for(ZeusFollow zf : zeusFollowers){
				if (zf.isImportant() && ZeusFollow.JobType.equals(zf.getType())) {
					importantContacts.add(zf);
				}else {
					otherFollowers.add(zf);
				}
			}
			String owner = groupManagerWithJob.getJobDescriptor(jobId).getX().getOwner();
			
			//首先添加重要联系人，然后是job本身的owner，最后是关注者。
			for(ZeusFollow person : importantContacts){
				if (!users.contains(person.getUid())) {
					users.add(person.getUid());
				}
			}
			if (!users.contains(owner)) {
				users.add(owner);
			}
			for (ZeusFollow other : otherFollowers) {
				if (!users.contains(other.getUid())) {
					users.add(other.getUid());
				}
			}
			List<ZeusUser> needAlertUsers = mysqlUserManager.getAllJobsNeedAlertUsers();
			for (ZeusUser item:needAlertUsers){
				if (!users.contains(item.getUid())) {
					users.add(item.getUid());
				}
			}
		}else{
			users.add(groupManagerWithJob.getJobDescriptor(jobId).getX().getOwner());
			if(history.getOperator()!=null){
				if(!users.contains(history.getOperator())){
					users.add(history.getOperator());
				}
			}
		}
		List<String> result=new ArrayList<String>();
		if(chain==null){
			result=users;
		}else{
			for(String uid:users){
				Integer count=chain.getUserCountMap().get(uid);
				if(count==null){
					count=1;
					chain.getUserCountMap().put(uid, count);
				}
				if(count<20){//一个job失败，最多发给同一个人20个报警
					chain.getUserCountMap().put(uid, ++count);
					result.add(uid);
				}
			}
		}
		alarm(actionId, result, title, content);
	}
	
	@Override
	public void alarm(String historyId, String title, String content)
			throws Exception {
		alarm(historyId, title, content, null);
	}
	/**
	 * @param jobId anction_id
	 * @param users 用户域账号id
	 * @param title
	 * @param content
	 * @throws Exception
	 */
	public abstract void alarm(String jobId, List<String> users,String title,String content) throws Exception;

}