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
package de.schlund.pfixcore.example.bank.ihandler;

import de.schlund.pfixcore.example.bank.context.ContextAccount;
import de.schlund.pfixcore.example.bank.context.ContextCustomer;
import de.schlund.pfixcore.example.bank.iwrapper.SelectAccount;
import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class SelectAccountHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        SelectAccount selAccount=(SelectAccount)wrapper;
        ContextCustomer contextCustomer=context.getContextResourceManager().getResource(ContextCustomer.class);
        Customer customer=contextCustomer.getCustomer();
        Account account=customer.getAccountByNo(selAccount.getAccountNo());
        if(account==null) throw new IllegalArgumentException("Customer hasn't account: "+selAccount.getAccountNo());
        else {
        	ContextAccount contextAccount=context.getContextResourceManager().getResource(ContextAccount.class);
        	contextAccount.setAccount(account);
        }
    }

    public boolean isActive(Context context) throws Exception {
       return true;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
    	ContextAccount contextAccount=context.getContextResourceManager().getResource(ContextAccount.class);
    	return contextAccount.getAccount()==null;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {

    }

}
