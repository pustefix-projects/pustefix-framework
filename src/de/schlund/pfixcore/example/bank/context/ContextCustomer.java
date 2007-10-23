package de.schlund.pfixcore.example.bank.context;

import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextCustomer extends ContextResource {
	
	public void setCustomer(Customer customer);
	public Customer getCustomer();

}
