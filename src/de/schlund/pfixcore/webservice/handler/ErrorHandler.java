/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.webservice.handler;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.ServiceCallContext;
import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.WebServiceServlet;
import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.fault.Fault;
import de.schlund.pfixcore.webservice.fault.FaultHandler;
import de.schlund.pfixcore.workflow.Context;

/**
 * @author mleidig@schlund.de
 */
public class ErrorHandler extends AbstractHandler {
        
    private static Logger LOG=Logger.getLogger(ErrorHandler.class);

    public ErrorHandler() {
        super();
    }

    public void invoke(MessageContext messageContext) throws AxisFault {
    }
    
    public void onFault(MessageContext msgCtx) {
    	String serviceName=msgCtx.getTargetService();
    	Configuration config=getServiceRuntime(msgCtx).getConfiguration();
    	ServiceConfig serviceConfig=config.getServiceConfig(serviceName);
    	FaultHandler faultHandler=serviceConfig.getFaultHandler();
    	if(faultHandler!=null) {
    	    ServiceCallContext callContext=ServiceCallContext.getCurrentContext();
    	    if(callContext!=null) {
    	        try {
                   Context context=callContext.getContext();
                   ServiceRequest srvReq=callContext.getServiceRequest();
                   ServiceResponse srvRes=callContext.getServiceResponse();
                   String reqMsg=((SOAPPart)msgCtx.getRequestMessage().getSOAPPart()).getAsString();
                   Fault fault=new Fault(serviceName,srvReq,srvRes,reqMsg,context);
                   WebServiceServlet.setCurrentFault(fault);
                   msgCtx.setResponseMessage(null);
    	        } catch(Exception x) {
    	            LOG.error("Error while processing fault.",x);
    	        }
    	    }
        }
    }
    
}
