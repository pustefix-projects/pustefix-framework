package org.pustefixframework.samples.taskmanager.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.pustefixframework.samples.taskmanager.model.Task.State;

@Entity
public class TaskList {
    
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
	@Version
	@Column(name="version")
	private int version;
	
	private int user;
    
	@Column(name="name")
	private String name;
	
	@Column(name="description")
    private String description;
    
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="taskList")
	private List<Task> tasks;
	
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getVersion() {
    	return version;
    }
    
    public void setVersion(int version) {
    	this.version = version;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setUser(int user) {
        this.user = user;
    }
    
    public List<Task> getTasks() {
    	return tasks;
    }
    
    public int getOpenTasks() {
        //TODO: fetch lazily
        int open = 0;
        for(Task task: getTasks()) {
            if(task.getState() == State.OPEN) {
                open++;
            }
        }
        return open;
    }
    
    public int getTotalTasks() {
        //TODO: fetch lazily
        return getTasks().size();
    }
    
}
