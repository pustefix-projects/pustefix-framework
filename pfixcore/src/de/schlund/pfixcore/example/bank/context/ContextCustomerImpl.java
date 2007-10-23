package de.schlund.pfixcore.example.bank.context;

import java.text.SimpleDateFormat;
import java.util.List;

import org.w3c.dom.Element;

import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextCustomerImpl implements ContextCustomer {

	private Customer customer;
	
	public void init(Context context) throws Exception {
	}
	
	public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
		if(customer!=null) {
			List<Account> accounts=customer.getAccounts();
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			for(Account account:accounts) {
				Element accountElem=resdoc.createSubNode(elem,"account");
				accountElem.setAttribute("accountNo",String.valueOf(account.getAccountNo()));
				accountElem.setAttribute("debit",String.valueOf(account.getDebit()));
				accountElem.setAttribute("currency",account.getCurrency().getSymbol());
				accountElem.setAttribute("openingDate",dateFormat.format(account.getOpeningDate().getTime()));
			}
		}
	}
	
	public Customer getCustomer() {
		return customer;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
}
