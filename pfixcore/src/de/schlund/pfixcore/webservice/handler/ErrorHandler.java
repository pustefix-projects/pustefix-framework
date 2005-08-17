package de.schlund.pfixcore.webservice.handler;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.WebServiceServlet;
import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.fault.Fault;
import de.schlund.pfixcore.webservice.fault.FaultHandler;
import de.schlund.pfixcore.workflow.Context;

public class ErrorHandler extends AbstractHandler {
        
    private static Logger LOG=Logger.getLogger(ErrorHandler.class);

    public ErrorHandler() {
        super();
    }

    public void invoke(MessageContext messageContext) throws AxisFault {
    }
    
    public void onFault(MessageContext msgCtx) {
    	String serviceName=msgCtx.getTargetService();
    	Configuration config=getWebServiceContext(msgCtx).getConfiguration();
    	ServiceConfig serviceConfig=config.getServiceConfig(serviceName);
    	FaultHandler faultHandler=serviceConfig.getFaultHandler();
    	if(faultHandler==null) {
    		GlobalServiceConfig globalConfig=config.getGlobalServiceConfig();
    		faultHandler=globalConfig.getFaultHandler();
    	}
    	if(faultHandler!=null) {
           try {
               Context context=(Context)msgCtx.getProperty(Constants.MSGCTX_PROP_CTX);
               HttpServletRequest srvReq=getServletRequest(msgCtx);
               String reqMsg=((SOAPPart)msgCtx.getRequestMessage().getSOAPPart()).getAsString();
               Fault fault=new Fault(serviceName,srvReq,reqMsg,context);
               WebServiceServlet.setCurrentFault(fault);
               msgCtx.setResponseMessage(null);
    		} catch(Exception x) {
    			LOG.error("Error while processing fault.",x);
    		}
    	}
    }
    
}
