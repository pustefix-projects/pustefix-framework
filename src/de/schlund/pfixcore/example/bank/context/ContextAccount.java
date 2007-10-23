package de.schlund.pfixcore.example.bank.context;

import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextAccount extends ContextResource {
	
	public void setAccount(Account account);
	public Account getAccount();

}
