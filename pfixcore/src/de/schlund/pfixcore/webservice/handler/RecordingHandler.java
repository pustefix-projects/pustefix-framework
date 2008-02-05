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

import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.ServiceRuntime;
import de.schlund.pfixcore.webservice.WebServiceServlet;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.utils.RecordingRequestWrapper;
import de.schlund.pfixcore.webservice.utils.RecordingResponseWrapper;
import de.schlund.pfixcore.webservice.utils.XMLFormatter;

/**
 * @author mleidig@schlund.de
 */
public class RecordingHandler extends AbstractHandler {
        
    private final static Logger LOG=Logger.getLogger(RecordingHandler.class.getName());

    public RecordingHandler() {
    }
    
    public void invoke(MessageContext msgCtx) throws AxisFault {
        ServiceRuntime runtime=getServiceRuntime(msgCtx);
        GlobalServiceConfig globConf=runtime.getConfiguration().getGlobalServiceConfig();
        if(globConf.getMonitoringEnabled()||globConf.getLoggingEnabled()) {
            if(!msgCtx.getPastPivot()) {
            	recordRequest(msgCtx);
            } else {
            	recordResponse(msgCtx);
            }
        }
    }
    
    private void recordRequest(MessageContext msgCtx) {
    	ServiceRequest serviceReq=WebServiceServlet.getCurrentRequest();
    	if(serviceReq instanceof RecordingRequestWrapper) {
    		String msg=null;
    		try {
    			msg=((SOAPPart)msgCtx.getRequestMessage().getSOAPPart()).getAsString();
    		} catch(AxisFault af) {
    			LOG.error("Can't get request message.",af);
    		}
    		((RecordingRequestWrapper)serviceReq).setRecordedMessage(XMLFormatter.format(msg));
    	}
    }
    
    private void recordResponse(MessageContext msgCtx) {
    	ServiceResponse serviceRes=WebServiceServlet.getCurrentResponse();
    	if(serviceRes instanceof RecordingResponseWrapper) {
    		String msg=null;
    		try {
    			msg=((SOAPPart)msgCtx.getResponseMessage().getSOAPPart()).getAsString();
    		 } catch(AxisFault af) {
    			 LOG.error("Can't get response message.",af);
    		 }
    		 ((RecordingResponseWrapper)serviceRes).setRecordedMessage(XMLFormatter.format(msg));
    	}
    }
    
    public void onFault(MessageContext msgCtx) {
        ServiceRuntime runtime=getServiceRuntime(msgCtx);
        GlobalServiceConfig globConf=runtime.getConfiguration().getGlobalServiceConfig();
        if(globConf.getMonitoringEnabled()||globConf.getLoggingEnabled()) {
            if(!msgCtx.getPastPivot()) {
                recordResponse(msgCtx);
            }
        }
    }
    
}
