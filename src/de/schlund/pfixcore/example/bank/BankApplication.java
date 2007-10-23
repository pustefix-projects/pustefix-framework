package de.schlund.pfixcore.example.bank;

import de.schlund.pfixcore.example.bank.model.BankDAO;
import de.schlund.pfixcore.example.bank.model.BankInMemoryDAO;

public class BankApplication {

	private static BankApplication instance=new BankApplication();
	
	private BankDAO bankDAO;
	
	public BankApplication() {
		bankDAO=new BankInMemoryDAO();
	}
	
	public static BankApplication getInstance() {
		return instance;
	}
	
	public BankDAO getBankDAO() {
		return bankDAO;
	}
	
}
