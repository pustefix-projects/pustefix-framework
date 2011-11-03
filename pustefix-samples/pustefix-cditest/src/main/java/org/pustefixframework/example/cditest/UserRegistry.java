package org.pustefixframework.example.cditest;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.pustefixframework.example.PasswordCheckService;

@ApplicationScoped
public class UserRegistry {

    @Inject @Named("pwdcheck")
    private PasswordCheckService passwordCheck;
    
    private Map<String, String> users = new HashMap<String, String>();
    
    public UserRegistry() {
        users.put("admin", "password");
    }
    
    public boolean register(User user) {
        if(passwordCheck.check(user.getPassword())) {
            synchronized(users) {
                if(!users.containsKey(user.getName())) {
                    System.out.println("REGISTER " + user.getName());
                    users.put(user.getName(), user.getPassword());
                    return true;
                }
            }
        }
        return false;
    }
    
}
