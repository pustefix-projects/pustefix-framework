package org.pustefixframework.samples.taskmanager.login.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.pustefixframework.samples.taskmanager.dataaccess.dao.TaskListsDao;
import org.pustefixframework.samples.taskmanager.login.LoginStatusCodes;
import org.pustefixframework.samples.taskmanager.model.TaskList;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class LoginHandler implements IHandler {

    private DataSource dataSource;
    
    private TaskListsDao taskListsDao;
    
	public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
		Login login = (Login)wrapper;
		Connection con = null;
		try {
		    con = dataSource.getConnection();
		    //TODO: source out DB access
		    PreparedStatement stmt = con.prepareStatement("SELECT id, password FROM user WHERE name = ?");
		    stmt.setString(1, login.getUser());
		    ResultSet result = stmt.executeQuery();
		    if(result.next()) {
		        //TODO: set user credentials in session
		        int userId = result.getInt(1);
		        String password = result.getString(2);
		        List<TaskList> taskLists = taskListsDao.getTaskLists();
		        for(TaskList taskList: taskLists) {
		            System.out.println("!!!!!!!!!!!!!!!! TASKLIST !!!!!!!!!!!!!!!!!!!!!!!!!!! " + taskList);
		        }
		        if(login.getPassword().equals(password)) {
		            context.getAuthentication().addRole("AUTHORIZED");
		        } else {
		            login.addSCodeUser(LoginStatusCodes.LOGIN_FAILED);
		        }
		    } else {
		        login.addSCodeUser(LoginStatusCodes.LOGIN_FAILED);
		    }
		    stmt.close();
		} catch(SQLException x) {
		    //TODO: specialized exception
		    throw new Exception("Database error during login", x);
		} finally {
		    if(con != null) con.close();
		}
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
	public void setDataSource(DataSource dataSource) {
	    this.dataSource = dataSource;
	}
	
	@Autowired
	public void setTaskListsDao(TaskListsDao taskListsDao) {
	    System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT " + taskListsDao);
	    this.taskListsDao = taskListsDao;
	}
	
}
