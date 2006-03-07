package de.schlund.pfixcore.webservice.handler;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.Constants;

public class LoggingHandler extends AbstractHandler {
        
    private static Logger LOG=Logger.getLogger(LoggingHandler.class);

    public LoggingHandler() {
        super();
    }

    public void invoke(MessageContext msgCtx) throws AxisFault {
        if(getWebServiceContext(msgCtx).getConfiguration().getGlobalServiceConfig().getLoggingEnabled()) {
            if(!msgCtx.getPastPivot()) {
                logRequest(msgCtx);
            } else {
                logResponse(msgCtx);
            }
        }
    }
    
    private void logRequest(MessageContext msgCtx) {
        String target=msgCtx.getTargetService();
        msgCtx.setProperty(Constants.MSGCTX_PROP_MONITORSTART,new Long(System.currentTimeMillis()));
        LOG.info("Target: "+target);
        LOG.info("Request:");
        Message msg=msgCtx.getRequestMessage();
        try {
            if(msg!=null) LOG.info(((SOAPPart)msg.getSOAPPart()).getAsString());
        } catch(AxisFault af) {
            LOG.error("Can't log request message.",af);
        }
    }
    
    private void logResponse(MessageContext msgCtx) {
        String target=msgCtx.getTargetService();
        Long startTime=(Long)msgCtx.getProperty(Constants.MSGCTX_PROP_MONITORSTART);
        long time=System.currentTimeMillis()-startTime.longValue();
        LOG.info("Target: "+target);
        LOG.info("Time: "+time);
        LOG.info("Response:");
        Message msg=msgCtx.getResponseMessage();
        try {
            if(msg!=null) LOG.info(((SOAPPart)msg.getSOAPPart()).getAsString());
        } catch(AxisFault af) {
            LOG.error("Can't log response message.",af);
        }
    }
    
    public void onFault(MessageContext msgCtx) {
        if(getWebServiceContext(msgCtx).getConfiguration().getGlobalServiceConfig().getLoggingEnabled()) {
            if(!msgCtx.getPastPivot()) {
                logResponse(msgCtx);
            }
        }
    }

}
