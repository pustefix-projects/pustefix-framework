/*
 * Created on 31.07.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Date;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mleidig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MonitorFilter implements Filter {

    private static org.apache.log4j.Logger LOG=org.apache.log4j.Logger.getLogger(MonitorFilter.class);
    private static boolean DEBUG=LOG.isDebugEnabled();

    private final static String PARAM_HSIZE="historysize";
    private final static int HSIZE_MAX=100;
    private final static int HSIZE_DEFAULT=5;
    
    private FilterConfig filterConfig;
    private int hsize;
    private Monitor monitor; 
    
     public void init(FilterConfig filterConfig) throws ServletException {
     	this.filterConfig = filterConfig;
        hsize=HSIZE_DEFAULT;
        String value=filterConfig.getInitParameter(PARAM_HSIZE);
        if(value!=null) {
        	int size=Integer.parseInt(value);
        	if(size>0|| size<=HSIZE_MAX) hsize=size;
        	else LOG.error("Invalid value for init parameter '"+PARAM_HSIZE+"': "+value);
        }
     	monitor=(Monitor)filterConfig.getServletContext().getAttribute(Monitor.class.getName());
        if(monitor==null) {
        	monitor=new Monitor(hsize);
            filterConfig.getServletContext().setAttribute(Monitor.class.getName(),monitor);
        }
     }

     public void destroy() {
        filterConfig.getServletContext().removeAttribute(Monitor.class.getName());
     	filterConfig=null;
     }
     
     public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException {
        if(filterConfig==null) return;
        if(!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) return;
        HttpServletRequest req=(HttpServletRequest)request;
        HttpServletResponse res=(HttpServletResponse)response;
        
        HttpSession session=req.getSession(false);
        String ip=req.getRemoteAddr();
        String method=req.getMethod();
        String uri=req.getRequestURI();
        Date date=new Date();
        
        ArrayList reqHeaders=new ArrayList();
        Enumeration enum=req.getHeaderNames();
        while(enum.hasMoreElements()) {
        	String name=(String)enum.nextElement();
            Enumeration hdrEnum=req.getHeaders(name);
            while(hdrEnum.hasMoreElements()) {
                String value=(String)hdrEnum.nextElement();
                HttpHeader header=new HttpHeader(name,value);
                reqHeaders.add(header);
            }
        }
        HttpHeader[] reqHdrs=new HttpHeader[reqHeaders.size()];
        for(int i=0;i<reqHeaders.size();i++) reqHdrs[i]=(HttpHeader)reqHeaders.get(i);
        
        MonitorRequestWrapper reqWrapper=new StreamingRequestWrapper((HttpServletRequest)request);
        MonitorResponseWrapper resWrapper=new StreamingResponseWrapper((HttpServletResponse)response);
        
        long t1=System.currentTimeMillis();
        chain.doFilter(reqWrapper,resWrapper);
        long t2=System.currentTimeMillis();
        
        HttpRequest httpReq=new HttpRequest(method,uri,date,(t2-t1),reqHdrs,reqWrapper.getBytes());
        HttpResponse httpRes=new HttpResponse(resWrapper.getHeaders(),resWrapper.getBytes());
        httpReq.setResponse(httpRes);
        
        if(session!=null) {
        	MonitorHistory hist=monitor.getMonitorHistory(session);
            hist.addEntry(httpReq);
        }
        MonitorHistory hist=monitor.getMonitorHistory(ip);
        hist.addEntry(httpReq);
        
        //Usage of PrefetchingRequestWrapper requires writing out prefetched bytes:
        /**
        OutputStream out=response.getOutputStream();
        out.write(resWrapper.getBytes());
        out.close();
        */
     }
     
}
