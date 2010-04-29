package org.pustefixframework.samples.taskmanager.web;

import java.util.Date;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.Task;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.pustefixframework.samples.taskmanager.model.Task.Priority;
import org.pustefixframework.samples.taskmanager.model.Task.State;
import org.pustefixframework.samples.taskmanager.user.ContextUser;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class CreateTaskHandler implements IHandler {

	private ContextUser ctxUser;
	private TaskListsDao taskListsDao;
	private ContextTaskLists ctxTaskLists;
	
	public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
		CreateTask sel = (CreateTask)wrapper;
		Task task = new Task();
		task.setSummary(sel.getSummary());
		task.setDescription(sel.getDescription());
		task.setCreationDate(new Date());
		task.setTargetDate(new Date());
		task.setPriority(Priority.NORMAL);
		task.setState(State.OPEN);
		TaskList taskList = ctxTaskLists.getSelectedTaskList();
		task.setTaskList(taskList);
		taskList.getTasks().add(task);
		taskListsDao.updateTaskList(taskList);
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
	
	@Autowired
	public void setContextTaskLists(ContextTaskLists ctxTaskLists) {
		this.ctxTaskLists = ctxTaskLists;
	}
	
}
