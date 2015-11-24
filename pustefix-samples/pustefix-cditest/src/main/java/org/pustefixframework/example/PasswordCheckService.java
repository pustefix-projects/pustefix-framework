package org.pustefixframework.example;

import java.io.Serializable;

public class PasswordCheckService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int strength;
    
    public boolean check(String password) {
        System.out.println("STRENGTH: " + strength);
        if(password.length() > 7) {
            return true;
        } else {
            return false;
        }
    }
    
    public void setStrength(int strength) {
        this.strength = strength;
    }

}
