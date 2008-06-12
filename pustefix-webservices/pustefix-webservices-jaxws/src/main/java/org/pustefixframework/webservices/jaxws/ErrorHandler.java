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

package org.pustefixframework.webservices.jaxws;

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

import org.pustefixframework.webservices.ServiceCallContext;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.pustefixframework.webservices.fault.Fault;
import org.pustefixframework.webservices.fault.FaultHandler;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles exceptions by calling the configured FaultHandler and
 * and removing error details (stacktraces) from the response.
 * 
 * @author mleidig@schlund.de
 */
public class ErrorHandler implements SOAPHandler<SOAPMessageContext> {
        
    private final static Logger LOG=Logger.getLogger(ErrorHandler.class.getName());
    
    /**
     * Do nothing.
     */
    public boolean handleMessage(SOAPMessageContext ctx) {
        return true;
    }
    
    /**
     * Process fault and remove error details (stacktraces) from response.
     */
    public boolean handleFault(SOAPMessageContext ctx) {
        boolean isOutbound = (Boolean) ctx.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if(isOutbound) {
            Throwable error=processFault(ctx);
            if(error==null) error=new Exception("No error information available.");
            SOAPMessage msg=ctx.getMessage();
            try {
                SOAPBody body=msg.getSOAPBody();
                if(body!=null) {
                    SOAPFault fault=body.getFault();
                    if(fault!=null) {
                        fault.setFaultString(error.getMessage());
                        Detail detail=fault.getDetail();
                        if(detail!=null) {
                            Iterator<?> it=detail.getChildElements();
                            while(it.hasNext()) {
                                SOAPElement elem=(SOAPElement)it.next();
                                elem.setAttribute("class",error.getClass().getName());
                                elem.removeContents();
                                elem.removeAttribute("note");
                            }
                        }
                    }
                }
            } catch(SOAPException x) {
                LOG.error("Error while removing error details from SOAPFault",x);
            } 
        }
        return true;
    }
    
    /**
     * Get Throwable from context and process it by FaultHandler.
     */
    private Throwable processFault(SOAPMessageContext ctx) {
        ServiceCallContext callContext=ServiceCallContext.getCurrentContext();
        JAXWSContext jaxwsContext=JAXWSContext.getCurrentContext();
        if(callContext!=null && jaxwsContext!=null) {
            Throwable error=jaxwsContext.getThrowable();
            String serviceName=callContext.getServiceRequest().getServiceName();
            Configuration config=callContext.getServiceRuntime().getConfiguration();
            ServiceConfig serviceConfig=config.getServiceConfig(serviceName);
            FaultHandler faultHandler=serviceConfig.getFaultHandler();
            if(faultHandler!=null) {
                try {
                   Context context=callContext.getContext();
                   ServiceRequest srvReq=callContext.getServiceRequest();
                   ServiceResponse srvRes=callContext.getServiceResponse();
                   Fault fault=new Fault(serviceName,srvReq,srvRes,null,context);
                   fault.setThrowable(error);
                   faultHandler.handleFault(fault);
                   error=fault.getThrowable();
                } catch(Exception x) {
                    LOG.error("Error while processing fault.",x);
                }
            }
            return error;
        }
        LOG.warn("Can't get Throwable from webservice call context.");
        return null;
    }
    
    public void close(MessageContext arg0) {}
    
    public Set<QName> getHeaders() {
        return null;
    }
   
}
