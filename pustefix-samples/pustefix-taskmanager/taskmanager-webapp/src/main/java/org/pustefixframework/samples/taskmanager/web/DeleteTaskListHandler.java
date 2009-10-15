package org.pustefixframework.samples.taskmanager.web;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class DeleteTaskListHandler implements IHandler {

	public void handleSubmittedData(Context context, IWrapper wrapper)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isActive(Context context) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean needsData(Context context) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean prerequisitesMet(Context context) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void retrieveCurrentStatus(Context context, IWrapper wrapper)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
