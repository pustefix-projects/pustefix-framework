package de.schlund.pfixcore.example.bank.ihandler;

import de.schlund.pfixcore.example.bank.context.ContextAccount;
import de.schlund.pfixcore.example.bank.context.ContextCustomer;
import de.schlund.pfixcore.example.bank.iwrapper.SelectAccount;
import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class SelectAccountHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        SelectAccount selAccount=(SelectAccount)wrapper;
        ContextCustomer contextCustomer=context.getContextResourceManager().getResource(ContextCustomer.class);
        Customer customer=contextCustomer.getCustomer();
        Account account=customer.getAccountByNo(selAccount.getAccountNo());
        if(account==null) throw new IllegalArgumentException("Customer hasn't account: "+selAccount.getAccountNo());
        else {
        	ContextAccount contextAccount=context.getContextResourceManager().getResource(ContextAccount.class);
        	contextAccount.setAccount(account);
        }
    }

    public boolean isActive(Context context) throws Exception {
    	ContextCustomer contextCustomer=context.getContextResourceManager().getResource(ContextCustomer.class);
    	return contextCustomer.getCustomer()!=null;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
    	ContextAccount contextAccount=context.getContextResourceManager().getResource(ContextAccount.class);
    	return contextAccount.getAccount()==null;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {

    }

}
