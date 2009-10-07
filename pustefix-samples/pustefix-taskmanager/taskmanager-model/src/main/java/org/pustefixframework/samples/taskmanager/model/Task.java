package org.pustefixframework.samples.taskmanager.model;

import java.util.Date;
import java.util.List;

public class Task {
    
    public enum Priority {LOW, NORMAL, HIGH};
    public enum State {OPEN, CLOSED};
    
    private long id;
    private String summary;
    private String description;
    private Priority priority;
    private State state;
    private Date creationDate;
    private Date targetDate;
    private List<Task> dependentTasks;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
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
    
    public List<Task> getDependentTasks() {
        return dependentTasks;
    }
    
    public void setDependentTasks(List<Task> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }

}
