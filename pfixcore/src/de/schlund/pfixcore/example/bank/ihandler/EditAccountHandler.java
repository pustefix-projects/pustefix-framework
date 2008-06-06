package de.schlund.pfixcore.example.bank.ihandler;

import de.schlund.pfixcore.example.bank.context.ContextAccount;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class EditAccountHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
    }

    public boolean isActive(Context context) throws Exception {
    	ContextAccount contextAccount=context.getContextResourceManager().getResource(ContextAccount.class);
    	return contextAccount.getAccount()!=null;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
    	return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {

    }

}
