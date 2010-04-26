package org.pustefixframework.samples.taskmanager.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Task {
    
    public enum Priority {LOW, NORMAL, HIGH};
    public enum State {OPEN, CLOSED};
    
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
	
	@Version
	@Column(name="version")
	private int version;
	
    private String summary;
    private String description;
    private Priority priority;
    private State state;
    
    @Column(name="creation_date")
    private Date creationDate;
    
    @Column(name="target_date")
    private Date targetDate;
    
    //TODO add support for task dependencies
    //private List<Task> dependentTasks;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tasklist", insertable=false, updatable=false, nullable=false)  
    private TaskList taskList;  
    
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
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        this.state = state;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    
    public Date getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
    }
    
    /**
    public List<Task> getDependentTasks() {
        return dependentTasks;
    }
    
    public void setDependentTasks(List<Task> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }
    */

}
