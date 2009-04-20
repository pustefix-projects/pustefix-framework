package org.pustefixframework.samples.moduletest.contextresources;

import org.pustefixframework.samples.moduletest.User;

import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextUser extends ContextResource {
    public void setUser(User user);
    public User getUser();
}
