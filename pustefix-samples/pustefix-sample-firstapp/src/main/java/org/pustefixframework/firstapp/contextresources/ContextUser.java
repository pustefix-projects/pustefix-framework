package org.pustefixframework.firstapp.contextresources;

import org.pustefixframework.firstapp.User;

import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextUser extends ContextResource {
    public void setUser(User user);
    public User getUser();
}
