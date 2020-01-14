package com.taobao.zeus.schedule.mvc.event;

import com.taobao.zeus.mvc.AppEvent;

public class ScheduleTriggerEvent extends AppEvent{

	private final String actionId;
	public ScheduleTriggerEvent(String actionId) {
		super(Events.ScheduleTrigger);
		this.actionId=actionId;
	}
	public String getActionId() {
		return actionId;
	}

}
