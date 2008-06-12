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

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.ServiceCallContext;
import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.ServiceRuntime;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.utils.RecordingRequestWrapper;
import de.schlund.pfixcore.webservice.utils.RecordingResponseWrapper;
import de.schlund.pfixcore.webservice.utils.XMLFormatter;

/**
 * Records SOAP requests and responses for monitoring (only appropriate to development mode).
 * 
 * @author mleidig@schlund.de
 */
public class RecordingHandler implements SOAPHandler<SOAPMessageContext> {
        
    private static Logger LOG=Logger.getLogger(RecordingHandler.class.getName());

    public RecordingHandler() {}
    
    /**
     * Record incoming requests and outgoing responses.
     */
    public boolean handleMessage(SOAPMessageContext ctx) {
        ServiceRuntime runtime=ServiceCallContext.getCurrentContext().getServiceRuntime();
        GlobalServiceConfig globConf=runtime.getConfiguration().getGlobalServiceConfig();
        if(globConf.getMonitoringEnabled()||globConf.getLoggingEnabled()) {
            boolean isOutbound = (Boolean) ctx.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if(isOutbound) recordResponse(ctx);
            else recordRequest(ctx);
        }
        return true;
    }
    
    /**
     * Record error requests/responses.
     */
    public boolean handleFault(SOAPMessageContext ctx) {
        handleMessage(ctx);   
        return true;
    }
    
    public void close(MessageContext arg0) {}
    
    public Set<QName> getHeaders() {
        return null;
    }
    
    /**
     * Record incoming request.
     */
    private void recordRequest(SOAPMessageContext ctx) {
        ServiceRequest serviceReq=ServiceCallContext.getCurrentContext().getServiceRequest();
        if(serviceReq instanceof RecordingRequestWrapper) {
            try {
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                ctx.getMessage().writeTo(out);
                String msg=out.toString("UTF-8");
                ((RecordingRequestWrapper)serviceReq).setRecordedMessage(XMLFormatter.format(msg));
            } catch(Exception x) {
                LOG.error("Error while recording request message",x);
            }
        }
    }
    
    /**
     * Record outgoing response.
     */
    private void recordResponse(SOAPMessageContext ctx) {
        ServiceResponse serviceRes=ServiceCallContext.getCurrentContext().getServiceResponse();
        if(serviceRes instanceof RecordingResponseWrapper) {
            try {
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                ctx.getMessage().writeTo(out);
                String msg=out.toString("UTF-8");
                ((RecordingResponseWrapper)serviceRes).setRecordedMessage(XMLFormatter.format(msg));
             } catch(Exception x) {
                 LOG.error("Error while recording response message",x);
             }
        }
    }
    
}
