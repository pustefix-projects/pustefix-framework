/*
 * Created on 29.07.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.pustefixframework.webservices.fault;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;

import de.schlund.pfixcore.workflow.Context;

public class Fault {
	
	Logger LOG=Logger.getLogger(getClass().getName());
	
	String serviceName;
    ServiceRequest srvReq;
    ServiceResponse srvRes;
	String reqMsg;
    Context context;
    Throwable throwable;
    String faultString;
	
	public Fault(String serviceName,ServiceRequest srvReq,ServiceResponse srvRes,String reqMsg,Context context) {
		this.serviceName=serviceName;
        this.srvReq=srvReq;
        this.srvRes=srvRes;
        this.reqMsg=reqMsg;
        this.context=context;
	}
    
	public ServiceRequest getRequest() {
	    return srvReq;
	}
                                                                                                                                                      
	public ServiceResponse getResponse() {
	    return srvRes;
	}

    public Throwable getThrowable() {
        return throwable;
    }
    
    public void setThrowable(Throwable throwable) {
        this.throwable=throwable;
        faultString=null;
    }
    
    public String getStackTrace() {
        if(throwable==null) return null;
        StringBuffer sb=new StringBuffer();
        int maxDepth=10;
        int depth=0;
        Throwable cause=throwable;
        while(depth<maxDepth&&cause!=null) {
            if(depth>0) sb.append("Caused by ");
            sb.append(cause.toString());
            sb.append("\n");
            StackTraceElement[] elems=cause.getStackTrace();
            int maxLen=elems.length;
            if(depth>0 && elems.length>10) maxLen=10; 
            for(int i=0;i<maxLen;i++) {
                sb.append("\tat ");
                sb.append(elems[i].toString());
                sb.append("\n");
            }
            if(maxLen<elems.length) sb.append("\t... "+(elems.length-maxLen)+" more\n");
            cause=cause.getCause();
            depth++;
        }
        return sb.toString();
    }
    
	public String getName() {
	    if(throwable!=null) return throwable.getClass().getName();
        return null;
	}
	
	public String getMessage() {
        if(throwable!=null) return throwable.getMessage();
        return null;
	}
    
    public String getFaultString() {
        if(faultString!=null) return faultString;
        return getName()+": "+getMessage();
    }
	
	public void setName(String name) {
		String msg=getMessage();
		faultString=name+": "+msg;
	}
	
	public void setMessage(String msg) {
		String name=getName();
		faultString=name+": "+msg;
	}
    
	public String getRequestMessage() {
	    return reqMsg;
	}
	
	public String getServiceName() {
		return serviceName;
	}
     
    public Context getContext() {
        return context;
    }
	
}
