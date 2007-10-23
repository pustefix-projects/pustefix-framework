package de.schlund.pfixcore.example.bank.model;

import java.util.List;

public class Customer {

	private long customerId;
	private String firstName;
	private String lastName;
	private String password;
	private List<Account> accounts;
	
	public Customer(long customerId, String firstName, String lastName, String password) {
		this.customerId = customerId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
	}
	
	public long getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public List<Account> getAccounts() {
		return accounts;
	}
	
	public Account getAccountByNo(long no) {
		for(Account account:accounts) {
			if(account.getAccountNo()==no) return account;
		}
		return null;
	}
	
	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}
	
}
