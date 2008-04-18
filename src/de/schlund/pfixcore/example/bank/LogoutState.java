package de.schlund.pfixcore.example.bank;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.example.bank.context.ContextCustomer;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

public class LogoutState extends StateImpl {

    public ResultDocument getDocument(Context context, PfixServletRequest req) throws Exception {;
        Authentication auth=context.getAuthentication();
        auth.revokeRole("ACCOUNT");
        auth.revokeRole("UNRESTRICTED");
        ContextCustomer contextCustomer = context.getContextResourceManager().getResource(ContextCustomer.class);
        contextCustomer.setCustomer(null);
        return new ResultDocument();
    }
    
}
