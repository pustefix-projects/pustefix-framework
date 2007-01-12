/*
 * Created on 29.07.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.fault;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.HttpServiceRequest;

public class LoggingHandler extends FaultHandler {

    private Logger LOG=Logger.getLogger(getClass().getName());
	
    public void init() {
        
    }
    
	public void handleFault(Fault fault) {
        HttpServiceRequest srvReq=(HttpServiceRequest)fault.getRequest();
        LOG.error("Request URI: "+srvReq.getRequestURI());
        LOG.error("Service name: "+fault.getServiceName());
        LOG.error("Fault string: "+fault.getFaultString());
        LOG.error("Exception name: "+fault.getName());
        LOG.error("Exception message: "+fault.getMessage());
        LOG.error("Request message: "+fault.getRequestMessage());
        LOG.error("Context: "+fault.getContext());
        LOG.error("Stacktrace: "+fault.getStackTrace());
	}
	
}
