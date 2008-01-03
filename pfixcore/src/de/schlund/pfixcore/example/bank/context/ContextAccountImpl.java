package de.schlund.pfixcore.example.bank.context;

import org.w3c.dom.Element;

import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextAccountImpl implements ContextAccount {

	private Account account;
	
	public void init(Context context) throws Exception {
	}
	
	public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
		if(account!=null) {
			ResultDocument.addObject(elem,"account",account);
		}
	}
	
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
}
