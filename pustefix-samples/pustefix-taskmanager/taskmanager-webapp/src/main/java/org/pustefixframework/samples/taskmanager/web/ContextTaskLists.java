package org.pustefixframework.samples.taskmanager.web;

import java.util.List;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.pustefixframework.samples.taskmanager.user.ContextUser;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;

public class ContextTaskLists {

	private ContextUser contextUser;
	private TaskListsDao taskListsDao;
	
	private int currentIndex;
	private int displaySize;
	
	@InsertStatus
	public void toXML(ResultDocument resdoc, Element elem) {
		//TODO: get user-specific tasklists
		List<TaskList> taskLists = taskListsDao.getTaskListsByUser(contextUser.getUserId());
		ResultDocument.addObject(elem, taskLists);
	}
	
	public void setTaskListsDao(TaskListsDao taskListsDao) {
		this.taskListsDao = taskListsDao;
	}
	
	public void setContextUser(ContextUser contextUser) {
		this.contextUser = contextUser;
	}
	
}
