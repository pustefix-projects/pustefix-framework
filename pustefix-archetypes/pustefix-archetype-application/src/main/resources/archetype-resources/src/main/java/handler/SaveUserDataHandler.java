#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.handler;

import ${package}.contextresources.ContextUser;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

public class SaveUserDataHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        System.out.println("Business logic to save data");
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return false;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);
        if (cUser.getUser() != null) {
            return true;
        }
        return false;
    }

    public void retrieveCurrentStatus(Context context, IWrapper arg1)
            throws Exception {
        // Nothing to be done here
    }
}
