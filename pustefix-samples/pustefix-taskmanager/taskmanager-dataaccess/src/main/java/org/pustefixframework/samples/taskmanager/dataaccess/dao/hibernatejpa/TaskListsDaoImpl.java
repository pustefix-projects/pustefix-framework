package org.pustefixframework.samples.taskmanager.dataaccess.dao.hibernatejpa;

import java.util.List;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.springframework.orm.jpa.support.JpaDaoSupport;

public class TaskListsDaoImpl extends JpaDaoSupport implements TaskListsDao {

    public List<TaskList> getTaskLists() {
        return getJpaTemplate().find("select t from TaskList t order by t.id");
    }

    public void addTaskList(TaskList taskList) {
        getJpaTemplate().persist(taskList);
    }

    public void deleteTaskList(TaskList taskList) {
        getJpaTemplate().remove(taskList);
    }

    public void updateTaskList(TaskList taskList) {
        getJpaTemplate().merge(taskList);
    }

    public TaskList getTaskList(int id) {
        return (TaskList)getJpaTemplate().getReference(TaskList.class, id);
    }

}
