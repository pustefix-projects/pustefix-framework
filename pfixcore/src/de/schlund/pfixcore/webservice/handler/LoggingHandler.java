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

    public void invoke(MessageContext messageContext) throws AxisFault {
        if(getWebServiceContext(messageContext).getConfiguration().getGlobalServiceConfig().getLoggingEnabled()) {
            String target=messageContext.getTargetService();
            if(!messageContext.getPastPivot()) {
                messageContext.setProperty(Constants.MSGCTX_PROP_MONITORSTART,new Long(System.currentTimeMillis()));
                System.out.println("LOG REQ");
                LOG.info("Target: "+target);
                LOG.info("Request:");
                Message msg=messageContext.getRequestMessage();
                if(msg!=null) LOG.info(((SOAPPart)msg.getSOAPPart()).getAsString());
            } else {
                Long startTime=(Long)messageContext.getProperty(Constants.MSGCTX_PROP_MONITORSTART);
                long time=System.currentTimeMillis()-startTime.longValue();
                LOG.info("Target: "+target);
                LOG.info("Time: "+time);
                LOG.info("Response:");
                Message msg=messageContext.getResponseMessage();
                if(msg!=null) LOG.info(((SOAPPart)msg.getSOAPPart()).getAsString());
            }
        }
    }

}
