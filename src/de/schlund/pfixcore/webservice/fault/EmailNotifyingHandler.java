/*
 * Created on 29.07.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.fault;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.util.email.EmailSender;
import de.schlund.pfixcore.util.email.EmailSenderException;
import de.schlund.pfixcore.webservice.HttpServiceRequest;

public class EmailNotifyingHandler extends FaultHandler {
    
    private Logger LOG=Logger.getLogger(getClass().getName());
    
    private final static String PARAM_SMTPHOST="smtphost";
    private final static String PARAM_SENDER="sender";
    private final static String PARAM_RECIPIENTS="recipients";
    
    private String smtpHost;
    private String sender;
    private String[] recipients;
    
    public void init() {
        smtpHost=getParam(PARAM_SMTPHOST);
        if(smtpHost==null) throw new IllegalArgumentException("Parameter '"+PARAM_SMTPHOST+"' is missing.");
        sender=getParam(PARAM_SENDER);
        if(sender==null) throw new IllegalArgumentException("Parameter '"+PARAM_SENDER+"' is missing.");
        String str=getParam(PARAM_RECIPIENTS);
        if(str==null) throw new IllegalArgumentException("Parameter '"+PARAM_RECIPIENTS+"' is missing.");
        ArrayList<String> al=new ArrayList<String>();
        StringTokenizer st=new StringTokenizer(str,",");
        while(st.hasMoreTokens()) {
            String s=st.nextToken();
            al.add(s.trim());
        }
        if(al.size()<1) throw new IllegalArgumentException("Parameter '"+PARAM_RECIPIENTS+"' has illegal value.");
        recipients=new String[al.size()];
        al.toArray(recipients);
    }
	
	public void handleFault(Fault fault) {
        if(isNotificationError(fault)) sendMail(fault);
        if(isInternalServerError(fault)) fault.setThrowable(new InternalServerError());
	}
    
    public void sendDirectMail(Fault fault) {
        try {
            String subject=createSubject(fault);
            String text=createText(fault);
            EmailSender.sendMail(subject,text,recipients,sender,smtpHost);
        } catch(EmailSenderException x) {
            LOG.error("Error while sending exception mail.",x);
        }
    }
    
    public void sendMail(Fault fault) {
        EmailNotifier notifier=EmailNotifier.getInstance();
        String subject=createSubject(fault);
        String text=createText(fault);
        notifier.sendMail(subject,text,recipients,sender,smtpHost);
    }
    
    public String createSubject(Fault fault) {
        StringBuffer sb=new StringBuffer();
        HttpServiceRequest srvReq=(HttpServiceRequest)fault.getRequest();
        sb.append(srvReq.getServerName());
        sb.append("|webservice|");
        sb.append(fault.getServiceName());
        sb.append("|");
        sb.append(fault.getFaultString());
        return sb.toString();
    }
    
    public String createText(Fault fault) {
        StringBuffer sb=new StringBuffer();
        HttpServiceRequest srvReq=(HttpServiceRequest)fault.getRequest();
        sb.append("Request: \t");
        sb.append(srvReq.getRequestURI());
        sb.append("\n");
        sb.append("Service: \t");
        sb.append(fault.getServiceName());
        sb.append("\n");
        sb.append("Exception: \t");
        sb.append(fault.getFaultString());
        sb.append("\n\n");
        sb.append("Context:\n");
        sb.append(fault.getContext().toString());
        sb.append("\n\n");
        sb.append("Request message:\n\n");
        sb.append(fault.getRequestMessage());
        sb.append("\n\n");
        sb.append("Stacktrace:\n\n");
        sb.append(fault.getStackTrace());
        sb.append("\n");
        return sb.toString();
    }
    
    public boolean isInternalServerError(Fault fault) {
        Throwable t=fault.getThrowable();
        if(t instanceof Error) return true;
        return false;
    }
    
    public boolean isNotificationError(Fault fault) {
        return true;
    }
	
}
