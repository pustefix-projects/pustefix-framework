/*
 * de.schlund.pfixcore.webservice.handler.AbstractHandler
 */
package de.schlund.pfixcore.webservice.handler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
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
        HttpServlet srv=(HttpServlet)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLET);
        ServletContext context=srv.getServletContext();
        return context;
    }

    protected WebServiceContext getWebServiceContext(MessageContext msgContext) {
        return (WebServiceContext)getServletContext(msgContext).getAttribute(WebServiceContext.class.getName());
    }
    
    protected HttpSession getSession(MessageContext msgContext) {
        HttpServletRequest req=(HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession session=req.getSession(false);
        return session;
    }
    
}
