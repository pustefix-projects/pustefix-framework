/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.tutorial.usermanagement;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;

public class UserList {
    
    private List<User> users = new ArrayList<User>();
    private int id = 0;
    
    public synchronized void addUser(User user) {
        user.setId(id);
        users.add(user);
        id++;
    }
    
    public synchronized List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }
    
    public synchronized User getUser(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
    
    public synchronized void deleteUser(int id) {
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
    
    public synchronized void replaceUser(User user) {
        User userToReplace = null;
        for (User existingUser : users) {
            if (existingUser.getId() == user.getId()) {
                userToReplace = existingUser;
            }
        }
        if (userToReplace != null) {
            users.remove(userToReplace);
            users.add(user);
        }
    }
    
    @PostConstruct
    public void createSampleUsers() throws Exception {
        addUser(new User("Neo", "neo@pustefix-framework.org", new GregorianCalendar(1964, 8, 2).getTime(), true, new URL("http://pustefix-framework.org"), "m"));
        addUser(new User("Trinity", "trinity@pustefix-framework.org", new GregorianCalendar(1967, 7, 21).getTime(), true, new URL("http://pustefix-framework.org"), "f"));
        addUser(new User("Morpheus", "morpheus@pustefix-framework.org", new GregorianCalendar(1961, 6, 30).getTime(), true, new URL("http://pustefix-framework.org"), "m"));
    }

}
