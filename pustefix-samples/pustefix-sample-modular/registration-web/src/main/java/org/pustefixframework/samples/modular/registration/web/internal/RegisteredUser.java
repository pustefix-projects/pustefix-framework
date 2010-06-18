package org.pustefixframework.samples.modular.registration.web.internal;

import org.pustefixframework.samples.modular.registration.User;

public class RegisteredUser implements User {

    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}