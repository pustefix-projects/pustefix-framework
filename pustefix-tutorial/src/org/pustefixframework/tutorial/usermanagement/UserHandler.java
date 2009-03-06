package org.pustefixframework.tutorial.usermanagement;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.iwrpgen.BeanToIWrapper;
import de.schlund.pfixcore.generator.iwrpgen.IWrapperToBean;
import de.schlund.pfixcore.workflow.Context;

public class UserHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        UserList userList = context.getContextResourceManager().getResource(UserList.class);
        EditUser editUser = context.getContextResourceManager().getResource(EditUser.class);
        User user = IWrapperToBean.createBean(wrapper, User.class);
        if (editUser.getId() != null) {
            // replace existing user
            user.setId(editUser.getId());
            userList.replaceUser(user);
            editUser.setId(null);            
        } else {
            // add new user
            userList.addUser(user);    
        }
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return false;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        EditUser editUser = context.getContextResourceManager().getResource(EditUser.class);
        if (editUser.getId() != null) {
            UserList userList = context.getContextResourceManager().getResource(UserList.class);
            User user = userList.getUser(editUser.getId());
            BeanToIWrapper.populateIWrapper(user, wrapper);
        }
    }
}
