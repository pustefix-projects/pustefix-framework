package org.pustefixframework.samples.taskmanager.dataaccess.dao.hibernatejpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.model.TaskList;

public class TaskListsDaoImpl implements TaskListsDao {
    
    @PersistenceContext
    private EntityManager em;

    public List<TaskList> getTaskListsByUser(int userId) {
       Query query = em.createQuery("from TaskList as t where t.user = :user");
       query.setParameter("user", userId);
       return (List<TaskList>)query.getResultList(); 
    }

    public void addTaskList(TaskList taskList) {
    	em.persist(taskList);
    }

    public void deleteTaskList(TaskList taskList) {
    	em.remove(taskList);
    }

    public void updateTaskList(TaskList taskList) {
    	em.merge(taskList);
    }

    public TaskList getTaskList(int id) {
    	Query query = em.createQuery("from TaskList as t where t.id = :id");
        query.setParameter("id", id);
        return (TaskList)query.getSingleResult();
    }

}
