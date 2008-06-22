package org.pustefixframework.tutorial.firstapp.contextresources;

import org.pustefixframework.tutorial.firstapp.User;
import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextUserImpl implements ContextUser {

    private User user;
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void init(Context context) throws Exception {
        // nothing to do here

    }

    public void insertStatus(ResultDocument document, Element element)
            throws Exception {
        // will be implemented later
    }
}
