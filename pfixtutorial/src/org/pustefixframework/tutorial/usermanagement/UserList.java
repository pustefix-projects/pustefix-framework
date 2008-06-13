package org.pustefixframework.tutorial.usermanagement;

import java.util.ArrayList;
import java.util.List;

public class UserList {
    
    private List<User> users = new ArrayList<User>();
    private int id = 0;
    
    public void addUser(User user) {
        user.setId(id);
        users.add(user);
        id++;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public User getUser(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
}
