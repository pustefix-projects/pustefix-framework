package de.schlund.pfixcore.example.bank.model;

import java.util.Collection;

public interface BankDAO {

	public Customer getCustomerById(long id);
	public Collection<Customer> getCustomers();
	
}
