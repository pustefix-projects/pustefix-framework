/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.webservices.fault;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.HttpServiceRequest;

import de.schlund.pfixcore.util.email.EmailSender;
import de.schlund.pfixcore.util.email.EmailSenderException;

public class EmailNotifyingHandler extends FaultHandler {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8853473850594232489L;

    private Logger LOG=Logger.getLogger(getClass().getName());
    
    private final static String PARAM_SMTPHOST="smtphost";
    private final static String PARAM_SENDER="sender";
    private final static String PARAM_RECIPIENTS="recipients";
    
    private String smtpHost;
    private String sender;
    private String[] recipients;
    
    @Override
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
	
	@Override
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
        sb.append(fault.getContext()==null?"-":fault.getContext().toString());
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
