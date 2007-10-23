package de.schlund.pfixcore.example.bank.context;

import java.net.URLEncoder;
import java.util.Collection;

import org.w3c.dom.Element;

import de.schlund.pfixcore.example.bank.AuthTokenManager;
import de.schlund.pfixcore.example.bank.BankApplication;
import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.example.bank.model.BankDAO;
import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextTestImpl implements ContextTest {
	
	public void init(Context context) throws Exception {
	}
	
	public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
		BankDAO bankDAO=BankApplication.getInstance().getBankDAO();
		Collection<Customer> customers=bankDAO.getCustomers();
		for(Customer customer:customers) {
			for(Account account:customer.getAccounts()) {
				Element e=resdoc.createSubNode(elem,"authtoken");
				String[] values=new String[] {String.valueOf(customer.getCustomerId()),
						String.valueOf(account.getAccountNo())};
				e.setAttribute("value",URLEncoder.encode(AuthTokenManager.createAuthToken(values),"UTF-8"));	
			}
		}
	}
	
}
