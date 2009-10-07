package org.pustefixframework.samples.taskmanager.login.web;

import org.pustefixframework.samples.taskmanager.login.LoginStatusCodes;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class LoginHandler implements IHandler {

	public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
		Login login = (Login)wrapper;
		//TODO: Login service/DB access
		if(login.getUser().equals("test") &&  login.getPassword().equals("test")) {
			context.getAuthentication().addRole("AUTHORIZED");
		} else {
			login.addSCodeUser(LoginStatusCodes.LOGIN_FAILED);
		}
	}
	
	public boolean isActive(Context context) throws Exception {
		return true;
	}
	
	public boolean needsData(Context context) throws Exception {
		return false;
	}
	
	public boolean prerequisitesMet(Context context) throws Exception {
		return true;
	}
	
	public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
	}
	
}
