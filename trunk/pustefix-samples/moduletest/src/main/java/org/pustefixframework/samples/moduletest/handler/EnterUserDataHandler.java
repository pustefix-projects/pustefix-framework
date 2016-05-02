package org.pustefixframework.samples.moduletest.handler;

import org.pustefixframework.samples.moduletest.User;
import org.pustefixframework.samples.moduletest.contextresources.ContextUser;
import org.pustefixframework.samples.moduletest.wrapper.EnterUserDataWrapper;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

public class EnterUserDataHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);
        
        EnterUserDataWrapper euWrapper = (EnterUserDataWrapper)wrapper;
        User user = new User();
        user.setSex(euWrapper.getSex());
        user.setName(euWrapper.getName());
        if (euWrapper.getEmail() != null) {
            user.setEmail(euWrapper.getEmail());
        }
        if (euWrapper.getHomepage() != null) {
            user.setHomepage(euWrapper.getHomepage());
        }
        if (euWrapper.getBirthdate() != null) {
            user.setBirthday(euWrapper.getBirthdate());
        }
        euWrapper.setAdmin(euWrapper.getAdmin());
        cUser.setUser(user);
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);
        if (cUser.getUser() == null) {
            return true;
        }
        return false;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);
        User user = cUser.getUser();
        if (user == null) {
            return;
        }
        EnterUserDataWrapper euWrapper = (EnterUserDataWrapper)wrapper;

        euWrapper.setSex(user.getSex());
        euWrapper.setName(user.getName());
        euWrapper.setEmail(user.getEmail());
        euWrapper.setHomepage(user.getHomepage());
        euWrapper.setBirthdate(user.getBirthday());
        euWrapper.setAdmin(user.getAdmin());
    }
}
