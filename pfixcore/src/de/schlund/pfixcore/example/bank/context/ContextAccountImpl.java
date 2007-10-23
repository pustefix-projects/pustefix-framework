package de.schlund.pfixcore.example.bank.context;

import java.text.SimpleDateFormat;

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
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Element accountElem=resdoc.createSubNode(elem,"account");
			accountElem.setAttribute("accountNo",String.valueOf(account.getAccountNo()));
			accountElem.setAttribute("debit",String.valueOf(account.getDebit()));
			accountElem.setAttribute("currency",account.getCurrency().getSymbol());
			accountElem.setAttribute("openingDate",dateFormat.format(account.getOpeningDate().getTime()));
		}
	}
	
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
}
