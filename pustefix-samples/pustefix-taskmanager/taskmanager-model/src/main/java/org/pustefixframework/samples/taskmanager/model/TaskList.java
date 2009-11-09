package org.pustefixframework.samples.taskmanager.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TaskList {
    
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
	
	private int user;
    
	@Column(name="name")
	private String name;
	
	@Column(name="description")
    private String description;
    
	@OneToMany(mappedBy="taskList",fetch=FetchType.EAGER,cascade=CascadeType.ALL)
	private List<Task> tasks;
	
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
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
    
}
