#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.handler;


import ${package}.context.User;
import ${package}.wrapper.Registration;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class RegistrationHandler implements IHandler {

    private User user;

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {

        Registration registration = (Registration)wrapper;
        user.setName(registration.getName());
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return user.getName() == null;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        if(user.getName() != null) {
            Registration registration = (Registration)wrapper;
            registration.setName(user.getName());
        }
    }

    @Autowired
    public void setUser(User user) {
        this.user = user;
    }

}
