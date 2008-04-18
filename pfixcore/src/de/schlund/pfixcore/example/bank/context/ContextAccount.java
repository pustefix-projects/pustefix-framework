package de.schlund.pfixcore.example.bank.context;

import de.schlund.pfixcore.example.bank.model.Account;

public interface ContextAccount {
	
	public void setAccount(Account account);
	public Account getAccount();

}
