package de.schlund.pfixcore.example.bank.context;

import java.util.List;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixxml.ResultDocument;

public class ContextCustomerImpl implements ContextCustomer {

    private Customer customer;

    @InsertStatus
    public void insertStatus(Element elem) throws Exception {
        if (customer != null) {
            List<Account> accounts = customer.getAccounts();
            ResultDocument.addObject(elem, accounts);
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

	public float getTotalDebit() {
        if (customer != null) {
            List<Account> accounts = customer.getAccounts();
            float total = 0;
            for (Account account : accounts)
                total += account.getDebit();
            return total;
        }
        return 0;
    }
	
}
