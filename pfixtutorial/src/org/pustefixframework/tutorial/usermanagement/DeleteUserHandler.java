package org.pustefixframework.tutorial.usermanagement;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.iwrpgen.IWrapperToBean;
import de.schlund.pfixcore.workflow.Context;

public class DeleteUserHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        DeleteUser deleteUser = IWrapperToBean.createBean(wrapper, DeleteUser.class);
        UserList userList = context.getContextResourceManager().getResource(UserList.class);
        userList.deleteUser(deleteUser.getId());
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
    }
    
}
