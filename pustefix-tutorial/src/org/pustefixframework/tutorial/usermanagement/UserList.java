package org.pustefixframework.tutorial.usermanagement;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
    
    public void deleteUser(Integer id) {
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
    
    public void replaceUser(User user) {
        User userToReplace = null;
        for (User existingUser : users) {
            if (existingUser.getId().equals(user.getId())) {
                userToReplace = existingUser;
            }
        }
        if (userToReplace != null) {
            users.remove(userToReplace);
            users.add(user);
        }
    }
    
    @InitResource
    public void createSampleUsers() throws Exception {
        addUser(new User("Neo", "neo@pustefix-framework.org", new GregorianCalendar(1964, 8, 2).getTime(), true, new URL("http://pustefix-framework.org"), "m"));
        addUser(new User("Trinity", "trinity@pustefix-framework.org", new GregorianCalendar(1967, 7, 21).getTime(), true, new URL("http://pustefix-framework.org"), "f"));
        addUser(new User("Morpheus", "morpheus@pustefix-framework.org", new GregorianCalendar(1961, 6, 30).getTime(), true, new URL("http://pustefix-framework.org"), "m"));
    }
}
