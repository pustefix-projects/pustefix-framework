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
       Query query = em.createQuery("from TaskList as t where t.user = :userId");
       query.setParameter("userId", userId);
       return (List<TaskList>)query.getResultList(); 
    }
    
    public TaskList getTaskListByUser(int userId, int taskListId) {
    	Query query = em.createQuery("from TaskList as t where t.user = :userId and t.id = :taskListId");
    	query.setParameter("userId", userId);
    	query.setParameter("taskListId", taskListId);
    	return (TaskList)query.getSingleResult();
    }

    public void addTaskList(TaskList taskList) {
    	em.persist(taskList);
    }

    public void deleteTaskList(TaskList taskList) {
    	em.remove(taskList);
    }

    public void updateTaskList(TaskList taskList) {
        Query query = em.createQuery("from TaskList as t where t.id = :taskListId");
        query.setParameter("taskListId", taskList.getId());
        if(query.getResultList().size()==0) throw new RuntimeException("Tasklist doesn't exist " + taskList.getId());
    	em.merge(taskList);
    }

}
