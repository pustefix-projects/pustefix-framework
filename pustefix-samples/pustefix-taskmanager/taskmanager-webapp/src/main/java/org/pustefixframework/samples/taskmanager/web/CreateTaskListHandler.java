package org.pustefixframework.samples.taskmanager.web;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.pustefixframework.samples.taskmanager.user.ContextUser;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class CreateTaskListHandler implements IHandler {

	private ContextUser ctxUser;
	private TaskListsDao taskListsDao;
	
	public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
		CreateTaskList sel = (CreateTaskList)wrapper;
		TaskList taskList = new TaskList();
		taskList.setName(sel.getName());
		taskList.setDescription(sel.getDescription());
		taskList.setUser(ctxUser.getUserId());
		taskListsDao.addTaskList(taskList);
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
	
	@Autowired
	public void setContextUser(ContextUser ctxUser) {
		this.ctxUser = ctxUser;
	}
	
	@Autowired
	public void setTaskListsDao(TaskListsDao taskListsDao) {
		this.taskListsDao = taskListsDao;
	}
	
}
