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
