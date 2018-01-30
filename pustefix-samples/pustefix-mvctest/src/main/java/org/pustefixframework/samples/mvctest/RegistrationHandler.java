package org.pustefixframework.samples.mvctest;

import org.pustefixframework.samples.mvctest.Registration;

import de.schlund.pfixcore.workflow.Context;
import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class RegistrationHandler implements InputHandler<Registration> {

    @Autowired
    Context context;
    @Autowired
    User user;

    public void handleSubmittedData(Registration registration) {
        user.setName(registration.getName());
    }

    public boolean isActive() {
        return true;
    }

    public boolean needsData() {
        return user.getName() == null;
    }

    public boolean prerequisitesMet() {
        return true;
    }

    public void retrieveCurrentStatus(Registration registration) {
        if(user.getName() != null) {
            registration.setName(user.getName());
        }
    }

}