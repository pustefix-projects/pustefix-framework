package org.pustefixframework.samples.moduletest.contextresources;

import org.pustefixframework.samples.moduletest.User;

public interface ContextUser {
    public void setUser(User user);
    public User getUser();
}
