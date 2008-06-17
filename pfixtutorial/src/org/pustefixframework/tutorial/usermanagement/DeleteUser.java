package org.pustefixframework.tutorial.usermanagement;

import de.schlund.pfixcore.generator.annotation.IWrapper;
import de.schlund.pfixcore.generator.annotation.Param;

@IWrapper(name="DeleteUserWrapper", ihandler=DeleteUserHandler.class)
public class DeleteUser {
    private int id;

    @Param(name="id", mandatory=true)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
}
