/*
 * de.schlund.pfixcore.webservice.monitor.MonitorResponseWrapper
 */
package de.schlund.pfixcore.webservice.monitor;

import java.util.*;
import java.text.SimpleDateFormat;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * MonitorResponseWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public abstract class MonitorResponseWrapper extends HttpServletResponseWrapper {
    
    private ArrayList headers;
    
    public MonitorResponseWrapper(HttpServletResponse res) {
        super(res);
        headers=new ArrayList();
    }
    
    public abstract byte[] getBytes();
 
    public void addCookie(Cookie cookie) {
    	super.addCookie(cookie);
      if(containsHeader("Set-Cookie")) headers.add(createHeader(cookie));
    }
    
    private HttpHeader createHeader(Cookie cookie) {
    	StringBuffer sb=new StringBuffer();
        sb.append(cookie.getName()+"="+cookie.getValue());
        if(cookie.getMaxAge()>0) {
        	SimpleDateFormat format=new SimpleDateFormat("EEE, MM-dd-yyyy HH:mm:ss z",Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String str=format.format(new Date(System.currentTimeMillis()+cookie.getMaxAge()*1000L));
        	sb.append("; expires="+str);
        }
        if(cookie.getPath()!=null) {
        	sb.append("; path="+cookie.getPath());
        }
        if(cookie.getDomain()!=null) {
        	sb.append("; domian="+cookie.getDomain());
        }
        if(cookie.getSecure()) {
        	sb.append("; secure");
        }
        return new HttpHeader("Set-Cookie",sb.toString());
    }
    
    public void addDateHeader(String name,long date) {
        super.addDateHeader(name,date);
        if(containsHeader(name)) headers.add(createHeader(name,date));
    }
    
    private HttpHeader createHeader(String name,long date) {
        SimpleDateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String str=format.format(new Date(date));
        return new HttpHeader(name,str);
    }
    
    public void addHeader(String name,String value) {
        super.addHeader(name,value);
        if(containsHeader(name)) headers.add(new HttpHeader(name,value));
    }
    
    public void addIntHeader(String name,int value) {
        super.addIntHeader(name,value);
        if(containsHeader(name)) headers.add(new HttpHeader(name,""+value));
    }
    
    private void setHeader(HttpHeader header) {
    	for(int i=0;i<headers.size();i++) {
    		HttpHeader current=(HttpHeader)headers.get(i);
            if(current.getName().equals(header.getName())) {
                headers.set(i,header);
                return;
            }
        }
        headers.add(header);
    }
    
    public void setDateHeader(String name,long date) {
    	super.setDateHeader(name,date);
        if(containsHeader(name)) setHeader(createHeader(name,date));
    }
    
    public void setHeader(String name,String value) {
        super.setHeader(name,value);
        if(containsHeader(name)) setHeader(new HttpHeader(name,value));
    }
    
    public void setIntHeader(String name,int value) {
    	super.setIntHeader(name,value);
        if(containsHeader(name)) setHeader(new HttpHeader(name,""+value));
    }
    
    public HttpHeader[] getHeaders() {
    	HttpHeader[] h=new HttpHeader[headers.size()];
        for(int i=0;i<headers.size();i++) h[i]=(HttpHeader)headers.get(i);
        return h;
    }
    
}
