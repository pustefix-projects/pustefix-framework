/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.example.bank.model;

import java.util.Calendar;
import java.util.Currency;

import de.schlund.pfixcore.oxm.impl.annotation.DateSerializer;

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

	@DateSerializer("yyyy-MM-dd HH:mm:ss")
	public Calendar getOpeningDate() {
		return openingDate;
	}
	
	public void setOpeningDate(Calendar openingDate) {
		this.openingDate = openingDate;
	}

}
