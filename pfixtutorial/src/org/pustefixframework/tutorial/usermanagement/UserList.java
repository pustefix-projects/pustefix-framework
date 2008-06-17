package org.pustefixframework.tutorial.usermanagement;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.schlund.pfixcore.beans.InitResource;

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
    
    public void deleteUser(int id) {
        User userToDelete = null;
        for (User user: users) {
            if (user.getId() == id) {
                userToDelete = user; 
            }
        }
        if (userToDelete != null) {
            users.remove(userToDelete);
        }
    }
    
    @InitResource
    public void createSampleUsers() throws Exception {
        addUser(new User("Tobias Fehrenbach", "fehrenbach@schlund.de", new Date(), true, new URL("http://1und1.de"), "m"));
        addUser(new User("Tobias Fehrenbach2", "fehrenbach@schlund.de", new Date(), true, new URL("http://1und1.de"), "m"));
    }
}
