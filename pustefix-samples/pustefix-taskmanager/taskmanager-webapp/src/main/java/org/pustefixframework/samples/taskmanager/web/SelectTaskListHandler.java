package org.pustefixframework.samples.taskmanager.web;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.pustefixframework.samples.taskmanager.user.ContextUser;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class SelectTaskListHandler implements IHandler {

	private ContextUser ctxUser;
	private ContextTaskLists ctxTaskLists;
	private TaskListsDao taskListsDao;
	
	public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
		SelectTaskList sel = (SelectTaskList)wrapper;
		TaskList taskList = taskListsDao.getTaskListByUser(ctxUser.getUserId(), sel.getId());
		ctxTaskLists.setSelectedTaskList(taskList);
	}
	
	public boolean isActive(Context context) throws Exception {
		System.out.println("UUUUUUUUUUUUUUUUUUUUUUUU: "+ctxUser.getUserName());
		return true;
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
	
	@Autowired
	public void setContextUser(ContextUser ctxUser) {
		this.ctxUser = ctxUser;
	}
	
	@Autowired
	public void setTaskListsDao(TaskListsDao taskListsDao) {
		this.taskListsDao = taskListsDao;
	}
	
}
