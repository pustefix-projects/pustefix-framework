package org.pustefixframework.samples.taskmanager.model;

public class TaskList {
    
    private long id;
    private long user;
    private String name;
    private String description;
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
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
    public void setUser(long user) {
        this.user = user;
    }
    
    

}
