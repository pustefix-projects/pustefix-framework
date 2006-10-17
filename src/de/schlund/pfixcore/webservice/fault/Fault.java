/*
 * Created on 29.07.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.fault;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.workflow.Context;

public class Fault {
	
	Logger LOG=Logger.getLogger(getClass().getName());
	
	String serviceName;
	HttpServletRequest srvReq;
    HttpServletResponse srvRes;
    String requestURI;
	String reqMsg;
    String serverName;
    Context context;
    Throwable throwable;
    String faultString;
	
	public Fault(String serviceName,HttpServletRequest srvReq,HttpServletResponse srvRes,String reqMsg,Context context) {
		this.serviceName=serviceName;
        this.srvReq=srvReq;
        this.srvRes=srvRes;
		this.requestURI=getRequestURI(srvReq);
		this.serverName=getServerName(srvReq);
        this.reqMsg=reqMsg;
        this.context=context;
	}
    
	public HttpServletRequest getRequest() {
	    return srvReq;
    }
    
    public HttpServletResponse getResponse() {
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
	
    public String getRequestURI() {
        return requestURI;
    }
    
	private String getRequestURI(HttpServletRequest srvReq) {
		StringBuffer sb=new StringBuffer();
		sb.append(srvReq.getScheme());
		sb.append("://");
		sb.append(srvReq.getServerName());
		sb.append(":");
		sb.append(srvReq.getServerPort());
		sb.append(srvReq.getRequestURI());
		HttpSession session=srvReq.getSession(false);
		if(session!=null) {
			sb.append(Constants.SESSION_PREFIX);
			sb.append(session.getId());
		}
		String s=srvReq.getQueryString();
		if(s!=null&&!s.equals("")) {
			sb.append("?");
			sb.append(s);
		}
		return sb.toString();
	}
    
    public String getServerName() {
        return serverName;
    }
    
    private String getServerName(HttpServletRequest srvReq) {
        return srvReq.getServerName();
    }
    
    public Context getContext() {
        return context;
    }
	
}
