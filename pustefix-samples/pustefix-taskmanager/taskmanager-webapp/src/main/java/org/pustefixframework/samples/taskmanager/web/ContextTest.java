package org.pustefixframework.samples.taskmanager.web;

import org.pustefixframework.samples.taskmanager.user.ContextUser;

public class ContextTest {

	private ContextUser contextUser;
	
	public void setContextUser(ContextUser contextUser) {
		this.contextUser = contextUser;
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@  "+contextUser.getClass().getName());
	}
	
	public ContextUser getContextUser() {
		return contextUser;
	}
	
}
