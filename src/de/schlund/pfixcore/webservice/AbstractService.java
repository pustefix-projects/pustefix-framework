/*
 * de.schlund.pfixcore.webservice.AbstractService
 */
package de.schlund.pfixcore.webservice;

import org.apache.axis.MessageContext;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * AbstractService.java 
 * 
 * Created: 29.06.2004
 * 
 * @author mleidig
 */
public abstract class AbstractService {

    protected ContextResourceManager getContextResourceManager() {
        MessageContext msgContext=MessageContext.getCurrentContext();
        ContextResourceManager crm=(ContextResourceManager)msgContext.getProperty(Constants.MSGCTX_PROP_CTXRESMAN);
        return crm;
    }
    
    protected ContextResource getContextResource(String name) {
        ContextResourceManager crm=getContextResourceManager();
        if(crm!=null) return crm.getResource(name);
        return null;
    }
    
}


