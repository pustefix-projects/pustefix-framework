package org.pustefixframework.samples.taskmanager.web;

import java.util.List;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.pustefixframework.samples.taskmanager.user.ContextUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;

public class ContextTaskLists {

	private ContextUser contextUser;
	private TaskListsDao taskListsDao;
	
	private TaskList selectedTaskList;
	
	@InsertStatus
	public void toXML(ResultDocument resdoc, Element elem) {
		List<TaskList> taskLists = taskListsDao.getTaskListsByUser(contextUser.getUserId());
		ResultDocument.addObject(elem, taskLists);
	}
	
	@Autowired
	public void setTaskListsDao(TaskListsDao taskListsDao) {
		this.taskListsDao = taskListsDao;
	}
	
	@Autowired
	public void setContextUser(ContextUser contextUser) {
		this.contextUser = contextUser;
	}
	
	
	public void setSelectedTaskList(TaskList taskList) {
		selectedTaskList = taskList;
	}
	
	public TaskList getSelectedTaskList() {
		return selectedTaskList;
	}
	
}
