package de.schlund.pfixcore.example.bank.model;

import java.util.Calendar;
import java.util.Currency;

public class Account {

	private long accountNo;
	private float debit;
	private Currency currency;
	private Calendar openingDate;
	
	public Account(long accountNo, float debit, Currency currency, Calendar openingDate) {
		this.accountNo = accountNo;
		this.debit = debit;
		this.currency = currency;
		this.openingDate = openingDate;
	}

	public long getAccountNo() {
		return accountNo;
	}
	
	public void setAccountNo(long accountNo) {
		this.accountNo = accountNo;
	}
	
	public float getDebit() {
		return debit;
	}
	
	public void setDebit(float debit) {
		this.debit = debit;
	}
	
	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Calendar getOpeningDate() {
		return openingDate;
	}
	
	public void setOpeningDate(Calendar openingDate) {
		this.openingDate = openingDate;
	}

}
