package de.schlund.pfixcore.webservice.handler;

import javax.servlet.http.HttpSession;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.*;

public class MonitoringHandler extends AbstractHandler {
        
    private static Logger LOG=Logger.getLogger(LoggingHandler.class);

    public MonitoringHandler() {
        super();
    }
    
    public void invoke(MessageContext messageContext) throws AxisFault {
        if(getWebServiceContext(messageContext).getServiceConfiguration().getServiceGlobalConfig().monitoringEnabled()) {
            HttpSession session=getSession(messageContext);
            if(session!=null) {
                MonitoringCache cache=(MonitoringCache)getWebServiceContext(messageContext).getAttribute(MonitoringCache.class.getName());
                String target=messageContext.getTargetService();
                if(!messageContext.getPastPivot()) {
                    MonitoringCacheEntry entry=new MonitoringCacheEntry();
                    messageContext.setProperty(Constants.MSGCTX_PROP_MONITORENTRY,entry);
                    entry.setTarget(target);
                    Message msg=messageContext.getRequestMessage();
                    entry.setRequest(((SOAPPart)msg.getSOAPPart()).getAsString());
                    entry.setStart(System.currentTimeMillis());
                } else {
                    MonitoringCacheEntry entry=(MonitoringCacheEntry)messageContext.getProperty(Constants.MSGCTX_PROP_MONITORENTRY);
                    entry.setEnd(System.currentTimeMillis());
                    Message msg=messageContext.getResponseMessage();
                    entry.setResponse(((SOAPPart)msg.getSOAPPart()).getAsString());
                    cache.setLastEntry(session,entry);
                }
            }
        }
    }
    
}
