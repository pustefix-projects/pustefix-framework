package de.schlund.pfixcore.example.bank.context;

import de.schlund.pfixcore.example.bank.model.Customer;

public interface ContextCustomer {
	public void setCustomer(Customer customer);
	public Customer getCustomer();
}
