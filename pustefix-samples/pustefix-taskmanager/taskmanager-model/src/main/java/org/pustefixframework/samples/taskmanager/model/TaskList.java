package org.pustefixframework.samples.taskmanager.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
    
    

}
