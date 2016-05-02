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

import java.net.URLEncoder;
import java.util.Collection;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.example.bank.AuthTokenManager;
import de.schlund.pfixcore.example.bank.BankApplication;
import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.example.bank.model.BankDAO;
import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextTestImpl implements ContextTest {

    @InitResource
    public void init(Context context) {
        String prop = context.getProperties().getProperty("adminmode");
        if(prop != null && Boolean.parseBoolean(prop)) 
            context.getAuthentication().addRole("ADMIN");
    }
    
    @InsertStatus
    public void serialize(ResultDocument resdoc, Element elem) throws Exception {
        BankDAO bankDAO = BankApplication.getInstance().getBankDAO();
        Collection<Customer> customers = bankDAO.getCustomers();
        for (Customer customer : customers) {
            for (Account account : customer.getAccounts()) {
                Element e = elem.getOwnerDocument().createElement("authtoken");
                elem.appendChild(e);
                String[] values = new String[] { String.valueOf(customer.getCustomerId()), String.valueOf(account.getAccountNo()) };
                e.setAttribute("value", URLEncoder.encode(AuthTokenManager.createAuthToken(values), "UTF-8"));
            }
        }
    }

}
