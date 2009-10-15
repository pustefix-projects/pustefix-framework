package org.pustefixframework.samples.taskmanager.web;

import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class DeleteTaskHandler implements IHandler {

	private ContextTaskLists ctxTaskLists;
	
	public void handleSubmittedData(Context context, IWrapper wrapper)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isActive(Context context) throws Exception {
		return ctxTaskLists.getSelectedTaskList() != null;
	}
	
	public boolean needsData(Context context) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean prerequisitesMet(Context context) throws Exception {
		return true;
	}
	
	public void retrieveCurrentStatus(Context context, IWrapper wrapper)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Autowired
	public void setContextTaskLists(ContextTaskLists ctxTaskLists) {
		this.ctxTaskLists = ctxTaskLists;
	}
	
}
