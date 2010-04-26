package org.pustefixframework.samples.taskmanager.web;


import org.pustefixframework.samples.taskmanager.StatusCodes;
import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.pustefixframework.samples.taskmanager.user.ContextUser;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class EditTaskListHandler implements IHandler {

	private ContextUser ctxUser;
	private ContextTaskLists ctxTaskLists;
	private TaskListsDao taskListsDao;
	
	public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
		EditTaskList sel = (EditTaskList)wrapper;
		TaskList selTaskList = ctxTaskLists.getSelectedTaskList();
		if(selTaskList.getId() != sel.getId()) {
			context.addPageMessage(StatusCodes.UNSELECTED_TASKLIST_CHANGE, null, null);
		} else {
			TaskList taskList = taskListsDao.getTaskListByUser(ctxUser.getUserId(), sel.getId());
			taskList.setName(sel.getName());
		    taskList.setDescription(sel.getDescription());
		    taskListsDao.updateTaskList(taskList);
		    ctxTaskLists.setSelectedTaskList(taskList);
		}
	}
	
	public boolean isActive(Context context) throws Exception {
	    TaskList taskList = ctxTaskLists.getSelectedTaskList();
	    return taskList != null;
	}
	
	public boolean needsData(Context context) throws Exception {
		return false;
	}
	
	public boolean prerequisitesMet(Context context) throws Exception {
		return true;
	}
	
	public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
	    TaskList taskList = ctxTaskLists.getSelectedTaskList();
	    if(taskList != null) {
	        EditTaskList editTaskList = (EditTaskList)wrapper;
	        editTaskList.setId(taskList.getId());
	        editTaskList.setName(taskList.getName());
	        editTaskList.setDescription(taskList.getDescription());
	    }
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
