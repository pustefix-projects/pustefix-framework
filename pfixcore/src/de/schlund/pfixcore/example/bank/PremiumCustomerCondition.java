package de.schlund.pfixcore.example.bank;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.example.bank.context.ContextCustomer;
import de.schlund.pfixcore.workflow.Context;

public class PremiumCustomerCondition implements Condition {
    
    private float limit;
    
    public boolean evaluate(Context context) {
        ContextCustomer contextCustomer=context.getContextResourceManager().getResource(ContextCustomer.class);
        return contextCustomer.getTotalDebit() >= limit;
    }
    
    public void setLimit(float limit) {
        this.limit = limit;
    }
    
    public Element toXML(Document doc) {
        return doc.createElement("isPremiumCustomer");
    }

}
