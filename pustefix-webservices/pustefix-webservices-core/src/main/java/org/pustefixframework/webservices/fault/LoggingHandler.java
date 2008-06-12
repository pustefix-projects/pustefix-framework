/*
 * Created on 29.07.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.pustefixframework.webservices.fault;

import org.apache.log4j.Logger;

public class LoggingHandler extends FaultHandler {

    private static final long serialVersionUID = -5523320091746362278L;
    
    private Logger LOG=Logger.getLogger(getClass().getName());
	
    public void init() {
        
    }
    
	public void handleFault(Fault fault) {
        LOG.error("Service name: "+fault.getServiceName());
        LOG.error("Exception name: "+fault.getName());
        LOG.error("Exception message: "+fault.getMessage());
        LOG.error("Request message: "+fault.getRequestMessage());
        LOG.error("Context: "+(fault.getContext()==null?"-":fault.getContext()));
        LOG.error("Stacktrace: "+fault.getStackTrace());
	}
	
}
