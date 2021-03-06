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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankInMemoryDAO implements BankDAO {

    private Map<Long, Customer> customers;

    public BankInMemoryDAO() {
        customers = new HashMap<Long, Customer>();
        Customer customer = new Customer(100000, "Mike", "Foo", "test");
        Currency currency = Currency.getInstance("EUR");
        Calendar date = Calendar.getInstance();
        date.set(2003, 9, 23, 8, 5);
        List<Account> accounts = new ArrayList<Account>();
        Account account = new Account(2000000, 3124.49f, currency, date);
        accounts.add(account);
        date = Calendar.getInstance();
        date.set(2003, 10, 4, 9, 15);
        account = new Account(2000123, 332.54f, currency, date);
        accounts.add(account);
        date = Calendar.getInstance();
        date.set(2004, 12, 13, 10, 10);
        account = new Account(2001405, 25123.11f, currency, date);
        accounts.add(account);
        customer.setAccounts(accounts);
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer getCustomerById(long id) {
        return customers.get(id);
    }

    public Collection<Customer> getCustomers() {
        return customers.values();
    }

    public static void main(String[] args) {
        new BankInMemoryDAO();
        Currency c = Currency.getInstance("EUR");
        System.out.println(c);
        System.out.println(c.getCurrencyCode());
        System.out.println(c.getSymbol());
    }

}
