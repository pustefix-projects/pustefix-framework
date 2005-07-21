package de.schlund.pfixcore.webservice.handler;

import javax.servlet.http.HttpSession;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.*;
import de.schlund.pfixcore.webservice.monitor.*;

public class MonitoringHandler extends AbstractHandler {
        
    private static Logger LOG=Logger.getLogger(MonitoringHandler.class.getName());

    public MonitoringHandler() {
    }
    
    public void invoke(MessageContext messageContext) throws AxisFault {
        WebServiceContext wsContext=getWebServiceContext(messageContext);
        if(wsContext.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            Monitor monitor=(Monitor)getWebServiceContext(messageContext).getAttribute(Monitor.class.getName());
            if(!messageContext.getPastPivot()) {
            	MonitorRecord record=new MonitorRecord();
                messageContext.setProperty(MonitorRecord.class.getName(),record);
                record.setStartTime(System.currentTimeMillis());
                String target=messageContext.getTargetService();
                record.setTarget(target);
                Message msg=messageContext.getRequestMessage();
                record.setRequest(((SOAPPart)msg.getSOAPPart()).getAsString());
            } else {
            	MonitorRecord record=(MonitorRecord)messageContext.getProperty(MonitorRecord.class.getName());
                Message msg=messageContext.getResponseMessage();
                record.setEndTime(System.currentTimeMillis());
                record.setResponse(((SOAPPart)msg.getSOAPPart()).getAsString());
                HttpSession session=getSession(messageContext);
                if(session!=null) monitor.getMonitorHistory(session).addRecord(record);
                String ip=getServletRequest(messageContext).getRemoteAddr();
                monitor.getMonitorHistory(ip).addRecord(record);
            }
        }
    }
    
}
