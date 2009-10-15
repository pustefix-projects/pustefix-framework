package org.pustefixframework.samples.taskmanager.web;

import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;

public class ContextTasks {

	private ContextTaskLists contextTaskLists;
	
	@InsertStatus
	public void toXML(ResultDocument resdoc, Element elem) {
		TaskList taskList = contextTaskLists.getSelectedTaskList();
		if(taskList != null) {
			ResultDocument.addObject(elem, taskList);
		}
	}
	
	@Autowired
	public void setContextTaskLists(ContextTaskLists contextTaskLists) {
		this.contextTaskLists = contextTaskLists;
	}
	
	
}
