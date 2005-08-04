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
    
    public void invoke(MessageContext msgCtx) throws AxisFault {
        WebServiceContext wsContext=getWebServiceContext(msgCtx);
        if(wsContext.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            if(!msgCtx.getPastPivot()) {
            	monitorRequest(msgCtx);
            } else {
            	monitorResponse(msgCtx);
            }
        }
    }
    
    private void monitorRequest(MessageContext msgCtx) {
        MonitorRecord record=new MonitorRecord();
        msgCtx.setProperty(MonitorRecord.class.getName(),record);
        record.setStartTime(System.currentTimeMillis());
        String target=msgCtx.getTargetService();
        record.setTarget(target);
        Message msg=msgCtx.getRequestMessage();
        try {
            record.setRequest(((SOAPPart)msg.getSOAPPart()).getAsString());
        } catch(AxisFault af) {
            record.setRequest("Not available");
            LOG.error("Can't get request message.",af);
        }
    }
    
    private void monitorResponse(MessageContext msgCtx) {
        Monitor monitor=(Monitor)getWebServiceContext(msgCtx).getAttribute(Monitor.class.getName());
        MonitorRecord record=(MonitorRecord)msgCtx.getProperty(MonitorRecord.class.getName());
        Message msg=msgCtx.getResponseMessage();
        record.setEndTime(System.currentTimeMillis());
        try {
            record.setResponse(((SOAPPart)msg.getSOAPPart()).getAsString());
        } catch(AxisFault af) {
            record.setResponse("Not available");
            LOG.error("Can't get response message.",af);
        }
        HttpSession session=getSession(msgCtx);
        if(session!=null) monitor.getMonitorHistory(session).addRecord(record);
        String ip=getServletRequest(msgCtx).getRemoteAddr();
        monitor.getMonitorHistory(ip).addRecord(record);
    }
    
    public void onFault(MessageContext msgCtx) {
        WebServiceContext wsContext=getWebServiceContext(msgCtx);
        if(wsContext.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            if(!msgCtx.getPastPivot()) {
                monitorResponse(msgCtx);
            }
        }
    }
    
}
