package org.pustefixframework.tutorial.usermanagement;

import de.schlund.pfixcore.generator.annotation.IWrapper;
import de.schlund.pfixcore.generator.annotation.Param;

@IWrapper(name="EditUserWrapper", ihandler=EditUserHandler.class)
public class EditUser {
    private Integer id;

    @Param(name="id", mandatory=true)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    
}
