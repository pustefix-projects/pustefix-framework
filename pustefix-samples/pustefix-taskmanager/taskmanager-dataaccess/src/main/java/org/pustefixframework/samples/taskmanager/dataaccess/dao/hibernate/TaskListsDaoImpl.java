package org.pustefixframework.samples.taskmanager.dataaccess.dao.hibernate;

import java.util.List;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class TaskListsDaoImpl extends HibernateDaoSupport implements TaskListsDao {

    public List<TaskList> getTaskLists() {
        return getHibernateTemplate().find("select t from TaskList t");
    }

    public void addTaskList(TaskList taskList) {
        getHibernateTemplate().persist(taskList);
    }

    public void deleteTaskList(TaskList taskList) {
        getHibernateTemplate().delete(taskList);
    }

    public void updateTaskList(TaskList taskList) {
        getHibernateTemplate().merge(taskList);
    }

    public TaskList getTaskList(int id) {
        return (TaskList)getHibernateTemplate().load(TaskList.class, id);
    }

}
