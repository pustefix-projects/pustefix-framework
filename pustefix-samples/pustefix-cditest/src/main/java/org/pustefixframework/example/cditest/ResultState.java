package org.pustefixframework.example.cditest;

import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.StaticState;
import de.schlund.pfixxml.PfixServletRequest;

public class ResultState extends StaticState {

    private User user;
  
    @Override
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return user.getName() != null;
    }
 
    @Autowired
    public void setUser(User user) {
        this.user = user;
    }
    
}
