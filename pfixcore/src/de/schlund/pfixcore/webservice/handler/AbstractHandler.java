/*
 * de.schlund.pfixcore.webservice.handler.AbstractHandler
 */
package de.schlund.pfixcore.webservice.handler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;

import de.schlund.pfixcore.webservice.WebServiceContext;

/**
 * AbstractHandler.java 
 * 
 * Created: 10.08.2004
 * 
 * @author mleidig
 */
public abstract class AbstractHandler extends BasicHandler {

    protected ServletContext getServletContext(MessageContext msgContext) {
        ServletContext context=null;
        HttpServlet servlet=(HttpServlet)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLET);
        if(servlet!=null) context=servlet.getServletContext();
        return context;
    }

    protected WebServiceContext getWebServiceContext(MessageContext msgContext) {
        WebServiceContext wsContext=null;
        ServletContext context=getServletContext(msgContext);
        if(context!=null) wsContext=(WebServiceContext)context.getAttribute(WebServiceContext.class.getName());
        return wsContext;
    }
    
    protected HttpServletRequest getServletRequest(MessageContext msgContext) {
    	 HttpServletRequest req=(HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
         return req;
    }
    
    protected HttpServletResponse getServletResponse(MessageContext msgContext) {
        HttpServletResponse res=(HttpServletResponse)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
        return res;
   }
    
    protected HttpSession getSession(MessageContext msgContext) {
        HttpSession session=null;
        HttpServletRequest req=getServletRequest(msgContext);
        if(req!=null) session=req.getSession(false);
        return session;
    }
    
}
